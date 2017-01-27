package com.bagri.tools.vvm.ui;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.management.openmbean.CompositeData;
import javax.management.openmbean.TabularData;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.bagri.tools.vvm.event.ApplicationEvent;
import com.bagri.tools.vvm.event.EventBus;
import com.bagri.tools.vvm.model.Schema;
import com.bagri.tools.vvm.service.SchemaManagementService;
import com.sun.tools.visualvm.charts.ChartFactory;
import com.sun.tools.visualvm.charts.SimpleXYChartDescriptor;
import com.sun.tools.visualvm.charts.SimpleXYChartSupport;
import com.sun.tools.visualvm.charts.xy.SimpleXYChartUtils;

public class SchemaCapacityPanel extends JPanel {
	
    private static final Logger LOGGER = Logger.getLogger(SchemaCapacityPanel.class.getName());
    
    //private static final long SLEEP_TIME = 1000;
    private static final int VALUES_LIMIT = 150;
    
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
        SimpleXYChartDescriptor descriptor = SimpleXYChartDescriptor.decimal(0, true, 271); //VALUES_LIMIT);

        descriptor.addLineFillItems("Number of documents");
        descriptor.addLineFillItems("Heap Cost");
        descriptor.addLineFillItems("Documents in queue");
        
        //descriptor.setDetailsItems(new String[]{"Detail 1", "Detail 2", "Detail 3"});
        descriptor.setChartTitle("<html><font size='+1'><b>" + schemaName + " Capacity</b></font></html>");
        descriptor.setXAxisDescription("partitions");
        descriptor.setYAxisDescription("units");
        chart = ChartFactory.createSimpleXYChart(descriptor);

        int left = 50;
        int bottom = 50;
        int width = 10;
        Graphics graph = chart.getChart().getGraphics();
   		//new VolumeStatsGenerator(chart, schemaService, schemaName).start();
        try {
	    	Schema s = schemaService.getSchema(schemaName);
	    	if (s != null && s.isActive()) {
	    		TabularData data = schemaService.getSchemaPartitionStatistics(schemaName);
	    		Set<List> keys = (Set<List>) data.keySet();
	        	for (List key: keys) {
	        		Object[] index = key.toArray();
	    			CompositeData cd = data.get(index);
	    			int partition = (Integer) cd.get("partition");
	    			int count = (Integer) cd.get("active count");
	    			long cost = (Long) cd.get("content cost");
		    		long[] stats = new long[] {count, cost, 0};
		    		chart.addValues(partition, stats);
		    		//graph.drawRect(left + partition*width, bottom, left + (partition + 1)*width, bottom + count);
	    		}
	    	}
        } catch (Exception ex) {
            LOGGER.severe(ex.getMessage());
        }
    }    


}
