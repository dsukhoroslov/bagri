package com.bagri.tools.vvm.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;

import com.bagri.tools.vvm.event.ApplicationEvent;
import com.bagri.tools.vvm.event.EventBus;
import com.bagri.tools.vvm.model.Schema;
import com.bagri.tools.vvm.model.SchemaManagement;
import com.bagri.tools.vvm.service.SchemaManagementService;
import com.bagri.tools.vvm.service.ServiceException;
import com.sun.tools.visualvm.charts.ChartFactory;
import com.sun.tools.visualvm.charts.SimpleXYChartDescriptor;
import com.sun.tools.visualvm.charts.SimpleXYChartSupport;

public class SchemaMonitoringPanel extends JPanel {
	
    private static final Logger LOGGER = Logger.getLogger(SchemaMonitoringPanel.class.getName());
    private static final long SLEEP_TIME = 1000;
    private static final int VALUES_LIMIT = 150;
    
    private final SchemaManagementService schemaService;
    private final EventBus<ApplicationEvent> eventBus;
    private final String schemaName; 

    private int partSize;
    private JPanel header;
    private SimpleXYChartSupport capChart;
    private SimpleXYChartSupport volChart;
    
    public SchemaMonitoringPanel(String schemaName, SchemaManagementService schemaService, EventBus<ApplicationEvent> eventBus) {
        //super(new GridLayout(1, 1));
        super(new BorderLayout());
        this.schemaName = schemaName;
        this.schemaService = schemaService;
        this.eventBus = eventBus;
        createVolumesChart();
        //setLayout(new BorderLayout());
        //add(volChart.getChart(), BorderLayout.WEST); //CENTER);        
        
        createCapacityChart();
        
        header = new JPanel(new GridLayout(2, 4));
        header.add(new JLabel("Total elements cost:"));
        header.add(new JLabel("Total content cost:"));
        header.add(new JLabel("Total results cost:"));

        JPanel panel = new JPanel(new BorderLayout()); // new GridLayout(1, 3));
        JButton refresh = new JButton("Refresh");
        refresh.setToolTipText("Reload chart...");
        refresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRefresh();
            }
        });
        refresh.setPreferredSize(new Dimension(100, 22));
        panel.setBackground(capChart.getChart().getBackground());
        panel.add(refresh, BorderLayout.EAST); //CENTER);
        header.add(panel); 

        header.add(new JLabel("Total indices cost:"));
        header.add(new JLabel("Total documents cost:"));
        header.add(new JLabel("Overall cost:"));

        panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0)); //new BorderLayout()); //new GridLayout(1, 2));
        panel.setBackground(capChart.getChart().getBackground());
        ButtonGroup bGroup = new ButtonGroup();
        JToggleButton parts = new JToggleButton("Partitions", true);
        parts.setPreferredSize(new Dimension(100, 22));
        JToggleButton nodes = new JToggleButton("Nodes");
        nodes.setPreferredSize(new Dimension(100, 22));
        bGroup.add(parts);
        bGroup.add(nodes);
        panel.add(parts); //, BorderLayout.EAST);
        panel.add(nodes); //, BorderLayout.EAST);
        header.add(panel);
        
        header.setPreferredSize(new Dimension(500, 50));
        header.setBackground(capChart.getChart().getBackground());
        add(header, BorderLayout.NORTH); //PAGE_START);
        //add(capChart.getChart(), BorderLayout.CENTER);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, volChart.getChart(), capChart.getChart());
		//splitPane.setResizeWeight(0.5);
		add(splitPane, BorderLayout.CENTER);        

        onRefresh();
    }

    private void createCapacityChart() {
    	partSize = 0;
    	try {
    		TabularData data = schemaService.getSchemaPartitionStatistics(schemaName);
    		partSize = data.size();
    	} catch (ServiceException ex) {
    		LOGGER.severe("createChart.error: " + ex.getMessage());
    	}

    	SimpleXYChartDescriptor descriptor = SimpleXYChartDescriptor.decimal(0, true, partSize);

        descriptor.addLineFillItems("Elements cost");
        descriptor.addLineFillItems("Content cost");
        descriptor.addLineFillItems("Results cost");
        descriptor.addLineFillItems("Indexes cost");
        descriptor.addLineFillItems("Documents cost");
        
        descriptor.setChartTitle("<html><font size='+1'><b>" + schemaName + " Capacity</b></font></html>");
        descriptor.setXAxisDescription("partitions");
        descriptor.setYAxisDescription("cost (Kb)");
        capChart = ChartFactory.createSimpleXYChart(descriptor);
        //chart.setZoomingEnabled(true);
    	
    	//CategoryDataset dataset = null;
    	//chart = ChartFactory.createBarChart(
        //        "Bar Chart Demo",         // chart title
        //        "Category",               // domain axis label
        //        "Value",                  // range axis label
        //        dataset,                  // data
        //        PlotOrientation.VERTICAL, // orientation
        //        true,                     // include legend
        //        true,                     // tooltips?
        //        false                     // URLs?
        //    );
    	
    	//chart = new BarChart();
    }    

    private void createVolumesChart() {
        SimpleXYChartDescriptor descriptor = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        descriptor.addLineFillItems("Number of documents");
        descriptor.addLineFillItems("Number of elements");
        descriptor.addLineFillItems("Open transactions");
        
        descriptor.setChartTitle("<html><font size='+1'><b>" + schemaName + " Statistics</b></font></html>");
        descriptor.setXAxisDescription("time");
        descriptor.setYAxisDescription("units");
        volChart = ChartFactory.createSimpleXYChart(descriptor);

   		new VolumeStatsGenerator(volChart, schemaService, schemaName).start();
    }    
    
    private void onRefresh() {
    	if (partSize == 0) {
        	try {
        		TabularData data = schemaService.getSchemaPartitionStatistics(schemaName);
        		partSize = data.size();
        	} catch (ServiceException ex) {
        		//LOGGER.severe("onRefresh.error: " + ex.getMessage());
        	}
    	}
    	if (partSize > 0) {
    		new CapacityStatsProvider(schemaService, schemaName, header, capChart).start();
    	}
    }

    private static class CapacityStatsProvider extends Thread implements Comparator<List<Integer>> {

    	private String schema;
    	private JPanel header;
        private SimpleXYChartSupport chart;
        private SchemaManagementService service;

        private CapacityStatsProvider(SchemaManagementService service, String schema, JPanel header, SimpleXYChartSupport chart) {
            this.service = service;
            this.schema = schema;
            this.header = header;
            this.chart = chart;
        }    

		@Override
		public int compare(List<Integer> o1, List<Integer> o2) {
			return o1.get(0).compareTo(o2.get(0));
		}

        public void run() {
            try {
      	    	Schema s = service.getSchema(schema);
       	    	if (s != null && s.isActive()) {
                    long totalECost = 0;
                    long totalCCost = 0;
                    long totalRCost = 0;
                    long totalICost = 0;
                    long totalDCost = 0;
      	    		TabularData data = service.getSchemaPartitionStatistics(schema);
       	    		Set<List<Integer>> keys = (Set<List<Integer>>) data.keySet();
       	    		Set<List<Integer>> sorted = new TreeSet<List<Integer>>(this);
       	    		sorted.addAll(keys);
       	        	for (List key: sorted) {
       	        		Object[] index = key.toArray();
       	    			CompositeData cd = data.get(index);
       	    			int partition = (Integer) cd.get("partition");
       	    			long eCost = (Long) cd.get("element cost");
       	    			eCost = new java.math.BigDecimal(eCost).movePointLeft(3).longValue();
       	    			totalECost += eCost;
       	    			long cCost = (Long) cd.get("content cost");
       	    			cCost = new java.math.BigDecimal(cCost).movePointLeft(3).longValue();
       	    			totalCCost += cCost;
       	    			long rCost = (Long) cd.get("result cost");
       	    			rCost = new java.math.BigDecimal(rCost).movePointLeft(3).longValue();
       	    			totalRCost += rCost;
       	    			long iCost = (Long) cd.get("index cost");
       	    			iCost = new java.math.BigDecimal(iCost).movePointLeft(3).longValue();
       	    			totalICost += iCost;
       	    			long dCost = (Long) cd.get("document cost");
       	    			dCost = new java.math.BigDecimal(dCost).movePointLeft(3).longValue();
       	    			totalDCost += dCost;
       		    		chart.addValues(partition, new long[] {eCost, cCost, rCost, iCost, dCost});
       	    		}
       	        	long overallCost = totalECost + totalCCost + totalRCost + totalICost + totalDCost;
       				Component[] labels = header.getComponents();
       				((JLabel) labels[0]).setText("Total elements cost: " + chart.formatDecimal(totalECost));
       				((JLabel) labels[1]).setText("Total content cost: " + chart.formatDecimal(totalCCost));
       				((JLabel) labels[2]).setText("Total results cost: " + chart.formatDecimal(totalRCost));
       				((JLabel) labels[4]).setText("Total indices cost: " + chart.formatDecimal(totalICost));
       				((JLabel) labels[5]).setText("Total documents cost: " + chart.formatDecimal(totalDCost));
       				((JLabel) labels[6]).setText("Overall cost: " + chart.formatDecimal(overallCost));
       	    	}
            } catch (Exception ex) {
                LOGGER.severe(ex.getMessage());
            }
        }

    }
    
    private static class VolumeStatsGenerator extends Thread {

    	private String schema;
        private SimpleXYChartSupport chart;
        private SchemaManagementService service;

        private VolumeStatsGenerator(SimpleXYChartSupport chart, SchemaManagementService service, String schema) {
            this.chart = chart;
            this.service = service;
            this.schema = schema;
        }    
        
        public void run() {
            while (true) {
                try {
        	    	Schema s = service.getSchema(schema);
        	    	if (s != null && s.isActive()) {
        	    		long[] vValues = service.getSchemaVolumeStatistics(schema);
        	    		long[] tValues = service.getSchemaTransactionStatistics(schema);
        	    		long[] stats = new long[] {vValues[0], vValues[1], tValues[1]};
        	    		chart.addValues(System.currentTimeMillis(), stats);
        	    	}
                    Thread.sleep(SLEEP_TIME);
                } catch (Exception ex) {
                    //LOGGER.severe(ex.getMessage());
                }
            }
        }

    }
}
