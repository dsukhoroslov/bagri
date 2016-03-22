package com.bagri.visualvm.manager.ui;

import com.bagri.visualvm.manager.event.ApplicationEvent;
import com.bagri.visualvm.manager.event.EventBus;
import com.bagri.visualvm.manager.model.Schema;
import com.bagri.visualvm.manager.model.SchemaManagement;
import com.bagri.visualvm.manager.model.TypedValue;
import com.bagri.visualvm.manager.service.SchemaManagementService;
import com.bagri.visualvm.manager.service.ServiceException;
import com.bagri.visualvm.manager.util.ErrorUtil;

import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

public class SchemaPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(SchemaManagementPanel.class.getName());
    private final SchemaManagementService schemaService;
    private final EventBus<ApplicationEvent> eventBus;
    private final Schema schema;
    private JTabbedPane tabbedPane;
    private JToolBar toolBar;
    private XTable grid;
    private JTextArea query;
    private JTextArea queryResult;
    private JRadioButton bXDM;
    private JLabel lbTime;
    private Properties queryProps;
    private Map<String, TypedValue> bindings = new HashMap<>(); 

    public SchemaPanel(SchemaManagementService schemaService, EventBus<ApplicationEvent> eventBus, Schema schema) {
        super(new GridLayout(1, 1));
        this.schema = schema;
        this.schemaService = schemaService;
        this.eventBus = eventBus;
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(schema.getSchemaName() + SchemaManagement.SCHEMA_DETAILS, createSchemaInfoPanel());
        tabbedPane.addTab(SchemaManagement.DOCUMENT_MANAGEMENT, createSchemaDocumentsPanel());
        tabbedPane.addTab(SchemaManagement.QUERY_MANAGEMENT, createSchemaQueryPanel());
        tabbedPane.addTab(SchemaManagement.SCHEMA_MONITORING, createSchemaMonitoringPanel());
        add(tabbedPane);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder());
        setBorder(BorderFactory.createEmptyBorder());

        try {
        	queryProps = schemaService.getQueryProperties(schema.getSchemaName());
        } catch (ServiceException ex) {
        	queryProps = new Properties();
        	queryProps.setProperty("xdm.client.fetchSize", "1000");
        	queryProps.setProperty("xqj.schema.queryTimeout", "0");
        }
    }

    private JPanel createSchemaInfoPanel() {
        JPanel panel = new JPanel();
        return panel;
    }

    private JPanel createSchemaDocumentsPanel() {
        JPanel panel = new JPanel();
        return panel;
    }

    private JPanel createSchemaMonitoringPanel() {
    	JPanel panel = new SchemaMonitoringPanel(schema.getSchemaName(), schemaService, eventBus);
        return panel;
    }

    private JPanel createSchemaQueryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JToolBar queryToolbar = new JToolBar();
        
        // "Query type" switch
        ButtonGroup queryGroup = new ButtonGroup();
        bXDM = new JRadioButton("XDM");
        bXDM.setSelected(true);
        JRadioButton bXQJ = new JRadioButton("XQJ");
        queryGroup.add(bXDM);
        queryGroup.add(bXQJ);
        queryToolbar.add(bXDM);
        queryToolbar.add(bXQJ);
        queryToolbar.addSeparator();

        // "Run Query" button
        JButton runQuery = new JButton("Run Query");
        runQuery.setToolTipText("Bind params, execute query...");
        runQuery.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRunQuery();
            }
        });
        queryToolbar.add(runQuery);
        queryToolbar.addSeparator();
        
        // "Query Properties" button
        JButton queryProps = new JButton("Query Properties");
        queryProps.setToolTipText("Set query processing properties");
        queryProps.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onQueryProperties();
            }
    	});
        queryToolbar.add(queryProps);
        queryToolbar.addSeparator();

        lbTime = new JLabel();
        lbTime.setVisible(false);
        queryToolbar.add(lbTime);
        //queryToolbar.addSeparator();
        
        queryToolbar.setFloatable(false);
        panel.add(queryToolbar, BorderLayout.PAGE_START);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(200);
        splitPane.setTopComponent(createQueryEditorPanel());
        splitPane.setBottomComponent(createQueryResultsPanel());
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JScrollPane createQueryEditorPanel() {
        //Create a text area.
        query = new JTextArea();
        query.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        query.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                if ( SwingUtilities.isRightMouseButton ( e ) ) {
                    // TODO: add Icons
                    // TODO: display "Cut" and "Copy" as grayed text, if text is not selected
                    JPopupMenu menu = new JPopupMenu ();
                    JMenuItem menuItem = new JMenuItem(new DefaultEditorKit.CutAction());
                    menuItem.setText("Cut");
                    menuItem.setMnemonic(KeyEvent.VK_T);
                    menu.add(menuItem);

                    menuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
                    menuItem.setText("Copy");
                    menuItem.setMnemonic(KeyEvent.VK_C);
                    menu.add(menuItem);

                    // TODO: display "paste" as grayed text if clipboard is empty.
                    menuItem = new JMenuItem(new DefaultEditorKit.PasteAction());
                    menuItem.setText("Paste");
                    menuItem.setMnemonic(KeyEvent.VK_P);
                    menu.add(menuItem);
                    menu.show(query, e.getX(), e.getY());
                }
             }
        });
        JScrollPane areaScrollPane = new JScrollPane(query);
        areaScrollPane.setPreferredSize(new Dimension(500, 150));
        areaScrollPane.setMinimumSize(new Dimension(500, 150));
        query.setCaretPosition(0);
        return areaScrollPane;
    }

    private JScrollPane createQueryResultsPanel() {
        //Create a text area.
        queryResult = new JTextArea();
        queryResult.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane areaScrollPane = new JScrollPane(queryResult);
        areaScrollPane.setPreferredSize(new Dimension(500, 150));
        areaScrollPane.setMinimumSize(new Dimension(500, 150));
        queryResult.setEditable(false);
        queryResult.setCaretPosition(0);
        return areaScrollPane;
    }

    // --- Event Handlers --- //
    private void onRunQuery() {
		final String qry = query.getText();
		if (qry != null && qry.trim().length() > 0) {
			lbTime.setVisible(false);
			long stamp = System.currentTimeMillis();
			try {
	    		java.util.List<String> vars = schemaService.parseQuery(schema, qry);
            	final long pTime = System.currentTimeMillis() - stamp;
	    		if (vars.size() > 0) {
		    		final BindQueryVarsDialog dlg = new BindQueryVarsDialog(vars, bindings, SchemaPanel.this);
		    		dlg.setSuccessListener(new ActionListener() {
		    			@Override
			            public void actionPerformed(ActionEvent e) {
			                SwingUtilities.invokeLater(new Runnable() {
			                    @Override
			                    public void run() {
			                    	Map<String, TypedValue> typedValues = dlg.getBindings();
			                    	runQuery(pTime, typedValues);
			                    	storeBindings(typedValues);
			                    }
			                });
			            }
			        });
			        dlg.setVisible(true);
	    		} else {
	    			runQuery(pTime, null);
	    		}
			} catch (ServiceException ex) {
				handleSErviceException(ex);
			}
		}
    }
    
    private void runQuery(long pTime, Map<String, TypedValue> values) {
    	
    	Object result;
    	long rTime = System.currentTimeMillis();
    	String qry = query.getText();
        try {
        	if (values == null) {
        		result = schemaService.runQuery(schema, bXDM.isSelected(), qry, queryProps);
        	} else {
            	Map<String, Object> params = converBindings(values);
        		result = schemaService.runQueryWithParams(schema, bXDM.isSelected(), qry, params, queryProps);
        	}
        	rTime = System.currentTimeMillis() - rTime;
        	lbTime.setText("Parse time: " + pTime + " ms; Query time: " + rTime + " ms");
    		lbTime.setVisible(true);
    		queryResult.setText((null != result) ? result.toString() : "Null");
        } catch (ServiceException ex) {
        	handleSErviceException(ex);
        }
    }
    
    private void handleSErviceException(ServiceException ex) {
        StringWriter sw = new StringWriter();
        PrintWriter printWriter = new PrintWriter(sw);
        ex.printStackTrace(printWriter);
        queryResult.setText(sw.toString());
    }
    
    private Map<String, Object> converBindings(Map<String, TypedValue> typedValues) throws ServiceException {
    	Map<String, Object> result = new HashMap<>(typedValues.size());
    	for (Map.Entry<String, TypedValue> e: typedValues.entrySet()) {
    		if ("file".equals(e.getValue().getType())) {
    			try {
    				String text = readTextFile(e.getValue().getValue().toString());
    				result.put(e.getKey(), text);
    			} catch (IOException ex) {
    				throw new ServiceException(ex);
    			}
    		} else {
    			result.put(e.getKey(), e.getValue().getValue());
    		}
    	}
    	return result;
    }
    
    private void storeBindings(Map<String, TypedValue> typedValues) {
    	bindings.putAll(typedValues);
    }

    private void onQueryProperties() {
    	//
		final EditPropertiesDialog dlg = new EditPropertiesDialog(queryProps, SchemaPanel.this);
		dlg.setSuccessListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                    	queryProps.clear();
                    	queryProps.putAll(dlg.getProperties());
                    }
                });
			}
		});
        dlg.setVisible(true);
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
//        if (!enabled) {
//            grid.clearSelection();
//            grid.getTableHeader().setEnabled(false);
//            grid.setEnabled(false);
//            for (Component c : toolBar.getComponents()) {
//                c.setEnabled(false);
//            }
//            toolBar.setEnabled(false);
//            for (Component c : tabbedPane.getComponents()) {
//                c.setEnabled(false);
//            }
//            tabbedPane.setEnabled(false);
//        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
//        if (!grid.isLoaded()) {
//            grid.reload();
//        }
    }

	private static String readTextFile(String fileName) throws IOException {
	    Path path = Paths.get(fileName);
	    StringBuilder text = new StringBuilder();
	    try (Scanner scanner = new Scanner(path, "utf-8")) {
	    	while (scanner.hasNextLine()) {
	    		text.append(scanner.nextLine()).append("\n");
	    	}      
	   	}
	    return text.toString();
	}
	
    
}
