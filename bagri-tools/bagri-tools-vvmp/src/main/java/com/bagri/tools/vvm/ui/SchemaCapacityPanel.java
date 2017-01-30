package com.bagri.tools.vvm.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
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
    private SimpleXYChartSupport chart;

    public SchemaCapacityPanel(String schemaName, SchemaManagementService schemaService, EventBus<ApplicationEvent> eventBus) {
        super(new GridLayout(1, 1));
        this.schemaName = schemaName;
        this.schemaService = schemaService;
        this.eventBus = eventBus;
        setLayout(new BorderLayout());
        createChart();
        add(chart.getChart(), BorderLayout.CENTER);
    }

    private void createChart() {
        SimpleXYChartDescriptor descriptor = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        descriptor.addLineFillItems("Heap Cost");
        descriptor.addLineFillItems("Number of documents");
        descriptor.addLineFillItems("Documents in queue");
        
        descriptor.setChartTitle("<html><font size='+1'><b>" + schemaName + " Capacity</b></font></html>");
        descriptor.setXAxisDescription("partitions");
        descriptor.setYAxisDescription("cost (Kb)/count (docs)");
        descriptor.setDetailsItems(new String[] {"Total Cost", "Total Count", "Total in Queue"});
        chart = ChartFactory.createSimpleXYChart(descriptor);
        //chart.setZoomingEnabled(true);

   		new CapacityStatsProvider(chart, schemaService, schemaName).start();
    }    


    private static class CapacityStatsProvider extends Thread {

    	private String schema;
        private SimpleXYChartSupport chart;
        private SchemaManagementService service;

        private CapacityStatsProvider(SimpleXYChartSupport chart, SchemaManagementService service, String schema) {
            this.chart = chart;
            this.service = service;
            this.schema = schema;
        }    
        
        public void run() {
        	// do it once..
            //while (true) {
                int totalCount = 0;
                long totalCost = 0;
                int totalQueue = 0;
                try {
        	    	Schema s = service.getSchema(schema);
        	    	if (s != null && s.isActive()) {
        	    		TabularData data = service.getSchemaPartitionStatistics(schema);
        	    		Set<List> keys = (Set<List>) data.keySet();
        	        	for (List key: keys) {
        	        		Object[] index = key.toArray();
        	    			CompositeData cd = data.get(index);
        	    			int partition = (Integer) cd.get("partition");
        	    			long cost = (Long) cd.get("content cost");
        	    			cost = new java.math.BigDecimal(cost).movePointLeft(3).longValue();
        	    			totalCost += cost;
        	    			int count = (Integer) cd.get("active count");
        	    			totalCount += count;
        	    			int queue = (Integer) cd.get("in queue");
        	    			totalQueue += queue;
        		    		long[] stats = new long[] {cost, count, queue};
        		    		chart.addValues(partition, stats);
        	    		}
        		    	chart.updateDetails(new String[] {chart.formatDecimal(totalCost), chart.formatDecimal(totalCount), chart.formatDecimal(totalQueue)});
        	    	}
                } catch (Exception ex) {
                    LOGGER.severe(ex.getMessage());
                }
            //}
        }

    }
    
}
