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

public class SimpleChartPanel extends JPanel {
	
    private static final Logger LOGGER = Logger.getLogger(SimpleChartPanel.class.getName());
    
    private final SchemaManagementService schemaService;
    private final EventBus<ApplicationEvent> eventBus;
    
    private static final long SLEEP_TIME = 500;
    private static final int VALUES_LIMIT = 150;
    private static final int ITEMS_COUNT = 8;
    
    private SimpleXYChartSupport chart;

    public SimpleChartPanel(SchemaManagementService schemaService, EventBus<ApplicationEvent> eventBus) {
        super(new GridLayout(1, 1));
        this.schemaService= schemaService;
        this.eventBus = eventBus;
        createChart();
        setLayout(new BorderLayout());
        add(chart.getChart(), BorderLayout.CENTER);        
    }

    private void createChart() {
        SimpleXYChartDescriptor descriptor =
                SimpleXYChartDescriptor.decimal(0, 1000, 1000, 1d, true, VALUES_LIMIT);

        for (int i = 0; i < ITEMS_COUNT; i++) {
            descriptor.addLineFillItems("Item " + i);
        }

        descriptor.setDetailsItems(new String[]{"Detail 1", "Detail 2", "Detail 3"});
        descriptor.setChartTitle("<html><font size='+1'><b>Demo Chart</b></font></html>");
        descriptor.setXAxisDescription("<html>X Axis <i>[time]</i></html>");
        descriptor.setYAxisDescription("<html>Y Axis <i>[units]</i></html>");

        chart = ChartFactory.createSimpleXYChart(descriptor);

        new Generator(chart).start();
    }    
    
    private static class Generator extends Thread {

        private SimpleXYChartSupport chart;

        public void run() {
            while (true) {
                try {
                    long[] values = new long[ITEMS_COUNT];
                    for (int i = 0; i < values.length; i++) {
                        values[i] = (long) (1000 * Math.random());
                    }
                    chart.addValues(System.currentTimeMillis(), values);
                    chart.updateDetails(new String[]{1000 * Math.random() + "",
                                1000 * Math.random() + "",
                                1000 * Math.random() + ""});
                    Thread.sleep(SLEEP_TIME);
                } catch (Exception ex) {
                    LOGGER.severe(ex.getMessage());
                }
            }
        }

        private Generator(SimpleXYChartSupport chart) {
            this.chart = chart;
        }    
    }
}
