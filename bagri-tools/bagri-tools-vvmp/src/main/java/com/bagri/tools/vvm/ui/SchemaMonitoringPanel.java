package com.bagri.tools.vvm.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.bagri.tools.vvm.event.ApplicationEvent;
import com.bagri.tools.vvm.event.EventBus;
import com.bagri.tools.vvm.model.SchemaManagement;
import com.bagri.tools.vvm.service.SchemaManagementService;
import com.sun.tools.visualvm.charts.ChartFactory;
import com.sun.tools.visualvm.charts.SimpleXYChartDescriptor;
import com.sun.tools.visualvm.charts.SimpleXYChartSupport;

public class SchemaMonitoringPanel extends JPanel {
	
    private static final Logger LOGGER = Logger.getLogger(SchemaManagementPanel.class.getName());
    private static final long SLEEP_TIME = 1000;
    private static final int VALUES_LIMIT = 150;
    
    private final SchemaManagementService schemaService;
    private final EventBus<ApplicationEvent> eventBus;
    private final String schemaName; 
    private SimpleXYChartSupport chart;

    public SchemaMonitoringPanel(String schemaName, SchemaManagementService schemaService, EventBus<ApplicationEvent> eventBus) {
        super(new GridLayout(1, 1));
        this.schemaName = schemaName;
        this.schemaService = schemaService;
        this.eventBus = eventBus;
        createChart();
        setLayout(new BorderLayout());
        add(chart.getChart(), BorderLayout.CENTER);        
    }

    private void createChart() {
        SimpleXYChartDescriptor descriptor = SimpleXYChartDescriptor.decimal(0, true, VALUES_LIMIT);

        descriptor.addLineFillItems("Number of documents");
        descriptor.addLineFillItems("Number of elements");
        //descriptor.addLineFillItems("Consumed size");
        descriptor.addLineFillItems("Open transactions");
        
        //descriptor.setDetailsItems(new String[]{"Detail 1", "Detail 2", "Detail 3"});
        descriptor.setChartTitle("<html><font size='+1'><b>" + schemaName + " Statistics</b></font></html>");
        descriptor.setXAxisDescription("time");
        descriptor.setYAxisDescription("units");

        chart = ChartFactory.createSimpleXYChart(descriptor);

        new VolumeStatsGenerator(chart, schemaService, schemaName).start();
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
                    long[] vValues = service.getSchemaVolumeStatistics(schema);
                    long[] tValues = service.getSchemaTransactionStatistics(schema);
                    long[] stats = new long[] {vValues[0], vValues[1], tValues[1]};
                    chart.addValues(System.currentTimeMillis(), stats);
                    //chart.updateDetails(new String[]{1000 * Math.random() + "",
                    //            1000 * Math.random() + "",
                    //            1000 * Math.random() + ""});
                    Thread.sleep(SLEEP_TIME);
                } catch (Exception ex) {
                    LOGGER.severe(ex.getMessage());
                }
            }
        }

    }
}
