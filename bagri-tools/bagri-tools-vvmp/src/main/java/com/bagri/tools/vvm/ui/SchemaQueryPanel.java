package com.bagri.tools.vvm.ui;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
import javax.xml.transform.OutputKeys;

import com.bagri.tools.vvm.event.ApplicationEvent;
import com.bagri.tools.vvm.event.EventBus;
import com.bagri.tools.vvm.model.Schema;
import com.bagri.tools.vvm.model.TypedValue;
import com.bagri.tools.vvm.service.SchemaManagementService;
import com.bagri.tools.vvm.service.ServiceException;
import com.bagri.tools.vvm.util.FileUtil;

public class SchemaQueryPanel extends JPanel {
	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOGGER = Logger.getLogger(SchemaManagementPanel.class.getName());
    private final SchemaManagementService schemaService;
    private final EventBus<ApplicationEvent> eventBus;
    private final Schema schema;
    //private JToolBar toolBar;
    private JTextArea queryText;
    private JTextArea queryResult;
    private JRadioButton bXDM;
    private JButton runQuery;
    private JButton cancelQuery;
    private JButton queryProps;
    private JButton clearResults;
    private JLabel lbTime;
    private Properties properties;
    private Map<String, TypedValue> bindings = new HashMap<>(); 
    private long parseTime;
    private Thread qRunner;
	
	public SchemaQueryPanel(Schema schema, SchemaManagementService schemaService, EventBus<ApplicationEvent> eventBus) {
		super(new BorderLayout());
		this.schema = schema;
		this.schemaService = schemaService;
		this.eventBus = eventBus;
		
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
        runQuery = new JButton("Run Query");
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
        queryProps = new JButton("Query Properties");
        queryProps.setToolTipText("Set query processing properties");
        queryProps.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onQueryProperties();
            }
    	});
        queryToolbar.add(queryProps);
        queryToolbar.addSeparator();

        // "Clear Results" button
        clearResults = new JButton("Clear Results");
        clearResults.setToolTipText("Clear results panel");
        clearResults.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	queryResult.setText(null);
            }
    	});
        queryToolbar.add(clearResults);
        queryToolbar.addSeparator();

        // "Cancel Query" button
        cancelQuery = new JButton("Cancel Query");
        cancelQuery.setToolTipText("Cancel currently running query");
        cancelQuery.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	onCancelQuery();
            }
    	});
        cancelQuery.setEnabled(false);
        queryToolbar.add(cancelQuery);
        queryToolbar.addSeparator();
        
        lbTime = new JLabel();
        lbTime.setVisible(false);
        queryToolbar.add(lbTime);
        //queryToolbar.addSeparator();
        
        queryToolbar.setFloatable(false);
        add(queryToolbar, BorderLayout.PAGE_START);
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(200);
        splitPane.setTopComponent(createQueryEditorPanel());
        splitPane.setBottomComponent(createQueryResultsPanel());
        add(splitPane, BorderLayout.CENTER);

        try {
        	properties = schemaService.getQueryProperties(schema.getSchemaName());
        } catch (ServiceException ex) {
        	properties = new Properties();
        	properties.setProperty("bdb.client.fetchSize", "1000");
        	properties.setProperty("xqj.schema.queryTimeout", "0");
        }

        String prop = properties.getProperty(OutputKeys.INDENT);
        if (prop == null) {
        	properties.setProperty(OutputKeys.INDENT, "yes");
        }
        prop = properties.getProperty(OutputKeys.METHOD);
        if (prop == null) {
        	properties.setProperty(OutputKeys.METHOD, "xml");
        }
        prop = properties.getProperty(OutputKeys.OMIT_XML_DECLARATION);
        if (prop == null) {
        	properties.setProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
	}

    private JScrollPane createQueryEditorPanel() {
        //Create a text area.
        queryText = new JTextArea();
        queryText.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        queryText.addMouseListener(new MouseAdapter() {
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
                    menu.show(queryText, e.getX(), e.getY());
                }
             }
        });
        JScrollPane areaScrollPane = new JScrollPane(queryText);
        areaScrollPane.setPreferredSize(new Dimension(500, 150));
        areaScrollPane.setMinimumSize(new Dimension(500, 150));
        queryText.setCaretPosition(0);
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
		final String qry = queryText.getText();
		if (qry != null && qry.trim().length() > 0) {
			lbTime.setVisible(false);
			long stamp = System.currentTimeMillis();
			try {
	    		java.util.List<String> vars = schemaService.parseQuery(schema.getSchemaName(), qry);
            	parseTime = System.currentTimeMillis() - stamp;
	    		if (vars.size() > 0) {
		    		final BindQueryVarsDialog dlg = new BindQueryVarsDialog(vars, bindings, SchemaQueryPanel.this);
		    		dlg.setSuccessListener(new ActionListener() {
		    			@Override
			            public void actionPerformed(ActionEvent e) {
			                SwingUtilities.invokeLater(new Runnable() {
			                    @Override
			                    public void run() {
			                    	Map<String, TypedValue> typedValues = dlg.getBindings();
			                    	runQuery(typedValues);
			                    	storeBindings(typedValues);
			                    }
			                });
			            }
			        });
			        dlg.setVisible(true);
	    		} else {
	    			runQuery(null);
	    		}
			} catch (ServiceException ex) {
				handleServiceException(ex);
			}
		}
    }
    
    private void runQuery(Map<String, TypedValue> values) {
    	
    	String query = queryText.getText();
    	queryResult.setText(null);
    	runQuery.setEnabled(false);
    	queryProps.setEnabled(false);
    	clearResults.setEnabled(false);
        cancelQuery.setEnabled(true);
    	//setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    	try {
	    	Map<String, Object> params = null;
	    	if (values != null) {
	        	params = convertBindings(values);
	    	}
    	
        	qRunner = new QueryRunner(query, bXDM.isSelected(), params, properties);
	    	//SwingUtilities.invokeLater(qRunner);
        	qRunner.start();
    	} catch (Exception ex) {
    		onDoneQuery(ex, 0);
    	}
    }
    
    private void handleServiceException(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter printWriter = new PrintWriter(sw);
        ex.printStackTrace(printWriter);
        queryResult.setText(sw.toString());
    }
    
    private Map<String, Object> convertBindings(Map<String, TypedValue> typedValues) throws IOException {
    	Map<String, Object> result = new HashMap<>(typedValues.size());
    	for (Map.Entry<String, TypedValue> e: typedValues.entrySet()) {
    		if ("file".equals(e.getValue().getType())) {
   				String text = FileUtil.readTextFile(e.getValue().getValue().toString());
   				result.put(e.getKey(), text);
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
		final EditPropertiesDialog dlg = new EditPropertiesDialog(properties, SchemaQueryPanel.this);
		dlg.setSuccessListener(new ActionListener() {
			@Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                    	properties.clear();
                    	properties.putAll(dlg.getProperties());
                    }
                });
			}
		});
        dlg.setVisible(true);
    }

    private void onCancelQuery() {
    	// implement it...
    	LOGGER.info("cancelling query...");
    	try {
    		schemaService.cancelQuery(schema.getSchemaName());
    		//qRunner.interrupt();
    		//cancelQuery.setEnabled(false);
    	} catch (Exception ex) {
    		handleServiceException(ex);
    	}
    }

    private void onDoneQuery(Object result, long runTime) {

    	runQuery.setEnabled(true);
    	queryProps.setEnabled(true);
    	clearResults.setEnabled(true);
		cancelQuery.setEnabled(false);
    	//setCursor(Cursor.getDefaultCursor());
    	if (result instanceof Exception) {
    		handleServiceException((Exception) result);
    	} else {
    		queryResult.setText((result == null) ? "Null" : result.toString());
        	lbTime.setText("Parse time: " + parseTime + " ms; Query time: " + runTime + " ms");
    		lbTime.setVisible(true);
    	}
    }
    

    private /*static*/ class QueryRunner extends Thread {
    	
    	private String query;
    	private boolean useXDM = false;
    	private Map<String, Object> params;
    	private Properties props;
    	
    	QueryRunner(String query, boolean useXDM, Map<String, Object> params, Properties props) {
    		this.query = query;
    		this.useXDM = useXDM;
    		this.params = params;
    		this.props = props;
    	}
    	
        public void run() {
        	Object result;
        	long runTime = System.currentTimeMillis();
            try {
           		result = schemaService.runQuery(schema.getSchemaName(), useXDM, query, params, props);
               	onDoneQuery(result, System.currentTimeMillis() - runTime);
            } catch (Exception ex) {
            	onDoneQuery(ex, 0);
            }
        }
    }
    
}
