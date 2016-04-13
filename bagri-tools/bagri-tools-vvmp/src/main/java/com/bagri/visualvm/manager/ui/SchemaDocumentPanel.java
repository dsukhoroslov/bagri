package com.bagri.visualvm.manager.ui;

import static com.bagri.visualvm.manager.util.Icons.COLLECTION_ICON;
import static com.bagri.visualvm.manager.util.Icons.DOCUMENT_ICON;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import com.bagri.visualvm.manager.event.ApplicationEvent;
import com.bagri.visualvm.manager.event.EventBus;
import com.bagri.visualvm.manager.model.Collection;
import com.bagri.visualvm.manager.model.Document;
import com.bagri.visualvm.manager.model.TypedValue;
import com.bagri.visualvm.manager.service.DocumentManagementService;
import com.bagri.visualvm.manager.service.SchemaManagementService;
import com.bagri.visualvm.manager.service.ServiceException;

public class SchemaDocumentPanel extends JPanel {
	
    private static final Logger LOGGER = Logger.getLogger(SchemaDocumentPanel.class.getName());
    
    private final DocumentManagementService docMgr;
    private final EventBus<ApplicationEvent> eventBus;
    private final String schemaName; 

    private JPanel infoPanel;
    private JTextArea contentArea;
    
    public SchemaDocumentPanel(String schemaName, SchemaManagementService schemaService, EventBus<ApplicationEvent> eventBus) {
    	super(new BorderLayout());
        this.schemaName = schemaName;
        this.eventBus = eventBus;
        this.docMgr = schemaService.getDocumentManagement(schemaName);

        JToolBar docToolbar = new JToolBar();

        // "Add Document" button
        JButton addDoc = new JButton("Add Document");
        addDoc.setToolTipText("Register new document from disk...");
        addDoc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddDocument();
            }
        });
        docToolbar.add(addDoc);
        docToolbar.addSeparator();

        // "Add Documents" button
        JButton addDocs = new JButton("Add Documents");
        addDoc.setToolTipText("Register bunch of document from directory...");
        addDoc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddDocument();
            }
        });
        docToolbar.add(addDocs);
        docToolbar.addSeparator();

        // "Delete Document" button
        JButton delDoc = new JButton("Delete Document");
        addDoc.setToolTipText("Delete selected document");
        addDoc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddDocument();
            }
        });
        docToolbar.add(delDoc);
        docToolbar.addSeparator();

        // "Add Collection" button
        JButton addCol = new JButton("Add Collection");
        addDoc.setToolTipText("Register new documents Collection...");
        addDoc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddDocument();
            }
        });
        docToolbar.add(addCol);
        docToolbar.addSeparator();

        // "Delete Collection" button
        JButton delCol = new JButton("Delete Collection");
        addDoc.setToolTipText("Delete selected Collection...");
        addDoc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddDocument();
            }
        });
        docToolbar.add(delCol);
        docToolbar.addSeparator();

        // "Delete Documents" button
        JButton delDocs = new JButton("Delete Documents");
        addDoc.setToolTipText("Delete documents for selected Collection...");
        addDoc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddDocument();
            }
        });
        docToolbar.add(delDocs);
        docToolbar.addSeparator();
    
        // "Add Document to Collections" button
        JButton addDocClns = new JButton("Add Document to Collections");
        addDoc.setToolTipText("Add selected document to collections...");
        addDoc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddDocument();
            }
        });
        docToolbar.add(addDocClns);
        docToolbar.addSeparator();
    
        // "Remove Document from Collections" button
        JButton remDocClns = new JButton("Remove Document from Collections");
        addDoc.setToolTipText("Remove selected document from collections...");
        addDoc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddDocument();
            }
        });
        docToolbar.add(remDocClns);
        docToolbar.addSeparator();
    
        docToolbar.setFloatable(false);
        add(docToolbar, BorderLayout.PAGE_START);
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(200);
        splitPane.setLeftComponent(createDocumentTreePanel());
        splitPane.setRightComponent(createDocumentManagmenetPanel());
        add(splitPane, BorderLayout.CENTER);
    }
    
    private JPanel createDocumentTreePanel() {
    	JPanel treePanel = new JPanel(new BorderLayout());
    	
    	JTree tree = new JTree();
        tree.setCellRenderer(new DocTreeCellRenderer());
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(schemaName);
        DefaultMutableTreeNode all = new DefaultMutableTreeNode("All Documents");
        root.add(all);
        // fill all docs tree...
        fillCollectionDocuments(all, null);

        List<Collection> collections = null;
        try {
			collections = docMgr.getCollections();
		} catch (ServiceException ex) {
            LOGGER.throwing(this.getClass().getName(), "createDocumentTreePanel", ex);
		}
        
        if (collections != null) {
        	Collections.sort(collections);
        	for (Collection collection: collections) {
                DefaultMutableTreeNode cln = new DefaultMutableTreeNode(collection);
                root.add(cln);
                fillCollectionDocuments(cln, collection.getName());
        	}
        }
        tree.setModel(new DefaultTreeModel(root, false));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        if (collections == null) {
        	tree.expandRow(0);
        }
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
            	DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
                if (node == null) return;

                //int dividerLocation = splitPane.getDividerLocation();
                Object nodeInfo = node.getUserObject();
                if (nodeInfo instanceof Document) {
                	String uri = ((Document) nodeInfo).getUri(); 
                	selectDocument(uri);
                //    splitPane.setRightComponent(getUserManagementView());
                //} else if (nodeInfo instanceof ClusterManagement) {
                //    splitPane.setRightComponent(getClusterManagementPanel());
                //} else if (nodeInfo instanceof Node) {
                }
                //splitPane.setDividerLocation(dividerLocation);
            }
        });
        
        treePanel.add(new JScrollPane(tree));
    	return treePanel;
    }
    
    private void handleServiceException(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter printWriter = new PrintWriter(sw);
        ex.printStackTrace(printWriter);
        contentArea.setText(sw.toString());
    }
    
    private void selectDocument(String uri) {
    	
    	try {
			Map<String, Object> doc = docMgr.getDocumentInfo(uri);
			Component[] labels = infoPanel.getComponents();
			((JLabel) labels[0]).setText("key: " + doc.get("key"));
			((JLabel) labels[1]).setText("id: " + doc.get("id"));
			((JLabel) labels[2]).setText("version: " + doc.get("version"));

			((JLabel) labels[3]).setText("uri: " + doc.get("uri"));
			((JLabel) labels[4]).setText("type: " + doc.get("type"));
			((JLabel) labels[5]).setText("encoding: " + doc.get("encoding"));
			
			((JLabel) labels[6]).setText("created at: " + doc.get("created at"));
			((JLabel) labels[7]).setText("created by: " + doc.get("created by"));
			((JLabel) labels[8]).setText("start tx: " + doc.get("txStart"));

			((JLabel) labels[9]).setText("elements: " + "NA"); //doc.get("created at"));
			((JLabel) labels[10]).setText("fragments: " + doc.get("fragments"));
			((JLabel) labels[11]).setText("collections: " + doc.get("collections"));

			String content = docMgr.getDocumentContent(uri);
	    	contentArea.setText(content);
	    	contentArea.setCaretPosition(0);
    	} catch (ServiceException ex) {
    		handleServiceException(ex);
    	}
    }
    
    private void fillCollectionDocuments(DefaultMutableTreeNode clNode, String clName) {
    	
        LOGGER.info("fillCollectionDocuments; going to populate collection: " + clName);
        try {
            List<Document> documents = docMgr.getDocuments(clName);
            LOGGER.info("fillCollectionDocuments; got documents: " + documents);
        	Collections.sort(documents);
            for (Document document: documents) {
                DefaultMutableTreeNode doc = new DefaultMutableTreeNode(document);
                clNode.add(doc);
            }
		} catch (ServiceException ex) {
            LOGGER.throwing(this.getClass().getName(), "fillCollectionDocuments", ex);
		}
    }
    
    private JPanel createDocumentManagmenetPanel() {
    	JPanel mgrPanel = new JPanel(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setDividerLocation(300);
        infoPanel = new JPanel(new GridBagLayout());
		GridBagConstraints cs = new GridBagConstraints();
		cs.fill = GridBagConstraints.HORIZONTAL;

		JLabel label = new JLabel("key:");
		cs.gridx = 0;
		cs.gridy = 0;
		label.setHorizontalAlignment(JLabel.CENTER);
		infoPanel.add(label, cs);

		label = new JLabel("id:");
		cs.gridx = 1;
		cs.gridy = 0;
		label.setHorizontalAlignment(JLabel.CENTER);
		infoPanel.add(label, cs);
		
		label = new JLabel("version:");
		cs.gridx = 2;
		cs.gridy = 0;
		label.setHorizontalAlignment(JLabel.CENTER);
		infoPanel.add(label, cs);

		label = new JLabel("uri:");
		cs.gridx = 0;
		cs.gridy = 1;
		label.setHorizontalAlignment(JLabel.CENTER);
		infoPanel.add(label, cs);

		label = new JLabel("type:");
		cs.gridx = 1;
		cs.gridy = 1;
		label.setHorizontalAlignment(JLabel.CENTER);
		infoPanel.add(label, cs);
		
		label = new JLabel("encoding:");
		cs.gridx = 2;
		cs.gridy = 1;
		label.setHorizontalAlignment(JLabel.CENTER);
		infoPanel.add(label, cs);

		label = new JLabel("created at:");
		cs.gridx = 0;
		cs.gridy = 2;
		label.setHorizontalAlignment(JLabel.CENTER);
		infoPanel.add(label, cs);

		label = new JLabel("created by:");
		cs.gridx = 1;
		cs.gridy = 2;
		label.setHorizontalAlignment(JLabel.CENTER);
		infoPanel.add(label, cs);
		
		label = new JLabel("start tx:");
		cs.gridx = 2;
		cs.gridy = 2;
		label.setHorizontalAlignment(JLabel.CENTER);
		infoPanel.add(label, cs);

		label = new JLabel("elements:");
		cs.gridx = 0;
		cs.gridy = 3;
		label.setHorizontalAlignment(JLabel.CENTER);
		infoPanel.add(label, cs);
		
		label = new JLabel("fragments:");
		cs.gridx = 1;
		cs.gridy = 3;
		label.setHorizontalAlignment(JLabel.CENTER);
		infoPanel.add(label, cs);
		
		label = new JLabel("collections:");
		cs.gridx = 3;
		cs.gridy = 3;
		label.setHorizontalAlignment(JLabel.CENTER);
		infoPanel.add(label, cs);
        
        infoPanel.setPreferredSize(new Dimension(500, 100));
        infoPanel.setMinimumSize(new Dimension(500, 100));
        infoPanel.setBorder(BorderFactory.createTitledBorder("document"));
        splitPane.setTopComponent(infoPanel);

        contentArea = new JTextArea();
        contentArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane areaScrollPane = new JScrollPane(contentArea);
        areaScrollPane.setPreferredSize(new Dimension(500, 500));
        areaScrollPane.setMinimumSize(new Dimension(500, 500));
        contentArea.setEditable(false);
        contentArea.setCaretPosition(0);
        splitPane.setBottomComponent(areaScrollPane);
        mgrPanel.add(splitPane, BorderLayout.CENTER);
    	return mgrPanel;
    }

    // --- Event Handlers --- //
    private void onAddDocument() {
    	// ...
    }

    
    private class DocTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            ImageIcon icon = getTreeIcon(value);
            if (null != icon) {
                setIcon(icon);
            }
            return this;
        }

        private ImageIcon getTreeIcon(Object value) {
            DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
            Object valueObject = treeNode.getUserObject();
            if (valueObject instanceof Collection) {
                return COLLECTION_ICON;
            }
            if (valueObject instanceof Document) {
                return DOCUMENT_ICON;
            }
            return null;
        }
    }
    
}
