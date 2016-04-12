package com.bagri.visualvm.manager.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import com.bagri.visualvm.manager.event.ApplicationEvent;
import com.bagri.visualvm.manager.event.EventBus;
import com.bagri.visualvm.manager.model.Collection;
import com.bagri.visualvm.manager.service.DocumentManagementService;
import com.bagri.visualvm.manager.service.SchemaManagementService;
import com.bagri.visualvm.manager.service.ServiceException;

public class SchemaDocumentPanel extends JPanel {
	
    private static final Logger LOGGER = Logger.getLogger(SchemaDocumentPanel.class.getName());
    
    private final DocumentManagementService docMgr;
    private final EventBus<ApplicationEvent> eventBus;
    private final String schemaName; 

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
        //tree.setCellRenderer(new TreeCellRenderer());
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(schemaName);
        DefaultMutableTreeNode all = new DefaultMutableTreeNode("All Documents");
        root.add(all);
        // fill all docs tree...

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
                //LOGGER.info("Collection: " + collection);
        	}
        }
        tree.setModel(new DefaultTreeModel(root, false));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setRootVisible(false);
        tree.expandRow(0);
        if (collections == null) {
        	tree.expandRow(1);
    	}
        treePanel.add(add(new JScrollPane(tree)));
    	return treePanel;
    }
    
    private JPanel createDocumentManagmenetPanel() {
    	return new JPanel(new BorderLayout());
    }

    // --- Event Handlers --- //
    private void onAddDocument() {
    	// ...
    }

}
