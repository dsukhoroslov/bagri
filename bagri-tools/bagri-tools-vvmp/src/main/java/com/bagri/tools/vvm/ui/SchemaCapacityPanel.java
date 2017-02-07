package com.bagri.tools.vvm.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.bagri.tools.vvm.event.ApplicationEvent;
import com.bagri.tools.vvm.event.EventBus;
import com.bagri.tools.vvm.model.Schema;
import com.bagri.tools.vvm.service.SchemaManagementService;
import com.sun.tools.visualvm.charts.ChartFactory;
import com.sun.tools.visualvm.charts.SimpleXYChartDescriptor;
import com.sun.tools.visualvm.charts.SimpleXYChartSupport;

public class SchemaCapacityPanel extends JPanel {
	
    private static final Logger LOGGER = Logger.getLogger(SchemaCapacityPanel.class.getName());
    
    //private static final long SLEEP_TIME = 1000;
    private static final int VALUES_LIMIT = 271; // number of partitions..?
    
    private final SchemaManagementService schemaService;
    private final EventBus<ApplicationEvent> eventBus;
    private final String schemaName; 
    private JPanel header;
    private SimpleXYChartSupport chart;

    public SchemaCapacityPanel(String schemaName, SchemaManagementService schemaService, EventBus<ApplicationEvent> eventBus) {
        super(new BorderLayout());
        this.schemaName = schemaName;
        this.schemaService = schemaService;
        this.eventBus = eventBus;

        createChart();
        
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
        panel.setBackground(chart.getChart().getBackground());
        panel.add(refresh, BorderLayout.EAST); //CENTER);
        header.add(panel); 
        
        header.add(new JLabel("Total indices cost:"));
        header.add(new JLabel("Total documents cost:"));
        header.add(new JLabel("Overall cost:"));
        
        header.setPreferredSize(new Dimension(500, 50));
        header.setBackground(chart.getChart().getBackground());
        add(header, BorderLayout.PAGE_START);
        add(chart.getChart(), BorderLayout.CENTER);
    }

    private void createChart() {
        SimpleXYChartDescriptor descriptor = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        descriptor.addLineFillItems("Elements cost");
        descriptor.addLineFillItems("Content cost");
        descriptor.addLineFillItems("Results cost");
        descriptor.addLineFillItems("Indexes cost");
        descriptor.addLineFillItems("Documents cost");
        
        descriptor.setChartTitle("<html><font size='+1'><b>" + schemaName + " Capacity</b></font></html>");
        descriptor.setXAxisDescription("partitions");
        descriptor.setYAxisDescription("cost (Kb)");
        chart = ChartFactory.createSimpleXYChart(descriptor);
        //chart.setZoomingEnabled(true);
        
        onRefresh();
    }    

    private void onRefresh() {
   		new CapacityStatsProvider(chart, schemaService, schemaName, header).start();
    }

    private static class CapacityStatsProvider extends Thread {

    	private String schema;
    	private JPanel header;
        private SimpleXYChartSupport chart;
        private SchemaManagementService service;

        private CapacityStatsProvider(SimpleXYChartSupport chart, SchemaManagementService service, String schema, JPanel header) {
            this.chart = chart;
            this.service = service;
            this.schema = schema;
            this.header = header;
        }    
        
        public void run() {
        	// do it once..
            //while (true) {
                try {
        	    	Schema s = service.getSchema(schema);
        	    	if (s != null && s.isActive()) {
                        long totalECost = 0;
                        long totalCCost = 0;
                        long totalRCost = 0;
                        long totalICost = 0;
                        long totalDCost = 0;
        	    		TabularData data = service.getSchemaPartitionStatistics(schema);
        	    		Set<List> keys = (Set<List>) data.keySet();
        	        	for (List key: keys) {
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
            //}
        }
    }
    
}
