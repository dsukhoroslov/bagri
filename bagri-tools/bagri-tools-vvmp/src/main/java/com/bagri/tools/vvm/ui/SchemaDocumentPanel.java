package com.bagri.tools.vvm.ui;

import static com.bagri.tools.vvm.util.Icons.COLLECTION_ICON;
import static com.bagri.tools.vvm.util.Icons.DOCUMENT_ICON;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.border.TitledBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.bagri.tools.vvm.event.ApplicationEvent;
import com.bagri.tools.vvm.event.EventBus;
import com.bagri.tools.vvm.model.Collection;
import com.bagri.tools.vvm.model.Document;
import com.bagri.tools.vvm.model.Schema;
import com.bagri.tools.vvm.service.DocumentManagementService;
import com.bagri.tools.vvm.service.SchemaManagementService;
import com.bagri.tools.vvm.service.ServiceException;
import com.bagri.tools.vvm.util.ErrorUtil;

public class SchemaDocumentPanel extends JPanel {
	
    private static final Logger LOGGER = Logger.getLogger(SchemaDocumentPanel.class.getName());
    private static final String all_docs = "All Documents";
    
    private final DocumentManagementService docMgr;
    private final EventBus<ApplicationEvent> eventBus;
    private final Schema schema; 

    private JTree docTree;
    private JPanel clnPanel = null;
    private JPanel docPanel = null;
    private JPanel clnInfoPanel;
    private JPanel clnStatsPanel;
    private JPanel docInfoPanel;
    private JTextArea contentArea;
    private JSplitPane mgrSplitter;
    private String currentPath = null;
    private List<Collection> collections = null;
    
    public SchemaDocumentPanel(Schema schema, SchemaManagementService schemaService, EventBus<ApplicationEvent> eventBus) {
    	super(new BorderLayout());
        this.schema = schema;
        this.eventBus = eventBus;
        this.docMgr = schemaService.getDocumentManagement(schema.getSchemaName());

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
        addDocs.setToolTipText("Register bunch of documents from directory...");
        addDocs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddDocuments();
            }
        });
        docToolbar.add(addDocs);
        docToolbar.addSeparator();

        // "Delete Document" button
        JButton delDoc = new JButton("Delete Document");
        delDoc.setToolTipText("Delete selected document");
        delDoc.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDeleteDocument();
            }
        });
        docToolbar.add(delDoc);
        docToolbar.addSeparator();

        // "Add Collection" button
        JButton addCol = new JButton("Add Collection");
        addCol.setToolTipText("Register new documents Collection...");
        addCol.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddCollection();
            }
        });
        docToolbar.add(addCol);
        docToolbar.addSeparator();

        // "Delete Collection" button
        JButton delCol = new JButton("Delete Collection");
        delCol.setToolTipText("Delete selected Collection...");
        delCol.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDeleteCollection();
            }
        });
        docToolbar.add(delCol);
        docToolbar.addSeparator();

        // "Delete Documents" button
        JButton delDocs = new JButton("Delete Documents");
        delDocs.setToolTipText("Delete documents for selected Collection...");
        delDocs.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDeleteDocuments();
            }
        });
        docToolbar.add(delDocs);
        docToolbar.addSeparator();
    
        // "Add Document to Collections" button
        JButton addDocClns = new JButton("Add Document to Collections");
        addDocClns.setToolTipText("Add selected document to collections...");
        addDocClns.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAddDocumentToCollection();
            }
        });
        docToolbar.add(addDocClns);
        docToolbar.addSeparator();
    
        // "Remove Document from Collections" button
        JButton remDocClns = new JButton("Remove Document from Collections");
        remDocClns.setToolTipText("Remove selected document from collections...");
        remDocClns.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onRemoveDocumentFromCollection();
            }
        });
        docToolbar.add(remDocClns);
        docToolbar.addSeparator();
    
        docToolbar.setFloatable(false);
        add(docToolbar, BorderLayout.PAGE_START);
        mgrSplitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mgrSplitter.setDividerLocation(0.3);
        mgrSplitter.setLeftComponent(createDocumentTreePanel());
        mgrSplitter.setRightComponent(getCollectionManagementPanel());
        add(mgrSplitter, BorderLayout.CENTER);
    }
    
    private JPanel createDocumentTreePanel() {
    	JPanel treePanel = new JPanel(new BorderLayout());
    	
    	docTree = new JTree();
        docTree.setCellRenderer(new DocTreeCellRenderer());
        DefaultMutableTreeNode root = new DefaultMutableTreeNode(schema.getSchemaName());

        try {
			Collection allCln = docMgr.getCollection(all_docs);
	        DefaultMutableTreeNode all = new DefaultMutableTreeNode(allCln);
	        root.add(all);
	        // fill all docs tree...
	        fillCollectionDocuments(all, all_docs);
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
        docTree.setModel(new DefaultTreeModel(root, false));
        docTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        docTree.setExpandsSelectedPaths(true);
        docTree.setShowsRootHandles(true);
        docTree.setRootVisible(false);
        if (collections == null) {
        	docTree.expandRow(0);
        }
        docTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
            	DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
                if (node == null) return;

                int dividerLocation = mgrSplitter.getDividerLocation();
                Object nodeInfo = node.getUserObject();
                if (nodeInfo instanceof Document) {
                	String uri = ((Document) nodeInfo).getUri(); 
                	selectDocument(uri);
                //    splitPane.setRightComponent(getUserManagementView());
                } else if (nodeInfo instanceof Collection) {
                	selectCollection((Collection) nodeInfo);
                //    splitPane.setRightComponent(getClusterManagementPanel());
                }
                mgrSplitter.setDividerLocation(dividerLocation);
            }
        });
        
        treePanel.add(new JScrollPane(docTree));
    	return treePanel;
    }
    
    private void handleServiceException(Exception ex) {
        StringWriter sw = new StringWriter();
        PrintWriter printWriter = new PrintWriter(sw);
        ex.printStackTrace(printWriter);
        contentArea.setText(sw.toString());
    }
    
    private JPanel getCollectionManagementPanel() {
    	if (clnPanel == null) {
	    	clnPanel = new JPanel(new BorderLayout());
	        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	        clnInfoPanel = new JPanel(new GridLayout(3, 3));
	        clnInfoPanel.add(new JLabel("name:"));
	        clnInfoPanel.add(new JLabel("id:"));
	        clnInfoPanel.add(new JLabel("version:"));
	        clnInfoPanel.add(new JLabel("description:"));
	        clnInfoPanel.add(new JLabel("document type:"));
	        clnInfoPanel.add(new JLabel("enabled:"));
	        clnInfoPanel.add(new JLabel("created at:"));
	        clnInfoPanel.add(new JLabel("created by:"));
	        clnInfoPanel.add(new JLabel(""));
	        clnInfoPanel.setPreferredSize(new Dimension(500, 90));
	        clnInfoPanel.setMinimumSize(new Dimension(500, 90));
	        clnInfoPanel.setBorder(BorderFactory.createTitledBorder("collection: "));
	        splitPane.setTopComponent(clnInfoPanel);
	
	        JPanel panel = new JPanel(new BorderLayout());
	        clnStatsPanel = new JPanel(new GridLayout(2, 3));
	        clnStatsPanel.add(new JLabel("documents:"));
	        clnStatsPanel.add(new JLabel("fragments:"));
	        clnStatsPanel.add(new JLabel("elements:"));
	        clnStatsPanel.add(new JLabel("full size in bytes:"));
	        clnStatsPanel.add(new JLabel("avg doc size in bytes:"));
	        clnStatsPanel.add(new JLabel("avg doc size in elements:"));
	        clnStatsPanel.setPreferredSize(new Dimension(500, 70));
	        clnStatsPanel.setBorder(BorderFactory.createTitledBorder("statistics: "));
	        panel.add(clnStatsPanel, BorderLayout.NORTH);
	        splitPane.setBottomComponent(panel);
	        clnPanel.add(splitPane, BorderLayout.CENTER);
	        splitPane.setDividerLocation(0.2);
    	}
    	return clnPanel;
    }
    
    private void selectCollection(Collection cln) {
    	getCollectionManagementPanel();
    	//try {
			((TitledBorder) clnInfoPanel.getBorder()).setTitle("collection: " + cln.getName());
			Component[] labels = clnInfoPanel.getComponents();
			((JLabel) labels[0]).setText("name: " + cln.getName());
			((JLabel) labels[1]).setText("id: " + cln.getId());
			((JLabel) labels[2]).setText("version: " + cln.getVersion());
			((JLabel) labels[3]).setText("description: " + cln.getDescription());
			((JLabel) labels[4]).setText("document type: " + cln.getDocType());
			((JLabel) labels[5]).setText("enabled: " + cln.isEnabled());
			((JLabel) labels[6]).setText("created at: " + cln.getCreatedAt());
			((JLabel) labels[7]).setText("created by: " + cln.getCreatedBy());
			clnInfoPanel.repaint();

			((TitledBorder) clnStatsPanel.getBorder()).setTitle("statistics: " + cln.getName());
			labels = clnStatsPanel.getComponents();
			((JLabel) labels[0]).setText("documents: " + cln.getDocCount());
			((JLabel) labels[1]).setText("fragments: " + cln.getFraCount());
			((JLabel) labels[2]).setText("elements: " + cln.getEltCount());
			((JLabel) labels[3]).setText("full size in bytes: " + cln.getByteSize());
			((JLabel) labels[4]).setText("avg doc size in bytes: " + cln.getAvgByteSize());
			((JLabel) labels[5]).setText("avg doc size in elements: " + cln.getAvgEltSize());
    	//} catch (ServiceException ex) {
    	//	handleServiceException(ex);
    	//}
    	mgrSplitter.setRightComponent(clnPanel);
    }
    
    private JPanel getDocumentManagementPanel() {
    	if (docPanel == null) {
	    	docPanel = new JPanel(new BorderLayout());
	        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	        docInfoPanel = new JPanel(new GridLayout(5, 3));
			docInfoPanel.add(new JLabel("key:"));
			//docInfoPanel.add(new JLabel("id:"));
			docInfoPanel.add(new JLabel("version:"));
			docInfoPanel.add(new JLabel("uri:"));
			docInfoPanel.add(new JLabel("size:"));
			docInfoPanel.add(new JLabel("encoding:"));
			docInfoPanel.add(new JLabel("created at:"));
			docInfoPanel.add(new JLabel("created by:"));
			docInfoPanel.add(new JLabel("start tx:"));
			docInfoPanel.add(new JLabel("elements:"));
			docInfoPanel.add(new JLabel("fragments:"));
			docInfoPanel.add(new JLabel("collections:"));
			docInfoPanel.add(new JLabel("partition:"));
			docInfoPanel.add(new JLabel("owner:"));
			docInfoPanel.setPreferredSize(new Dimension(500, 120));
			docInfoPanel.setMinimumSize(new Dimension(500, 120));
			docInfoPanel.setBorder(BorderFactory.createTitledBorder("document: "));
	        splitPane.setTopComponent(docInfoPanel);
	
	        contentArea = new JTextArea();
	        contentArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
	        JScrollPane areaScrollPane = new JScrollPane(contentArea);
	        areaScrollPane.setPreferredSize(new Dimension(500, 500));
	        areaScrollPane.setMinimumSize(new Dimension(500, 500));
	        contentArea.setEditable(false);
	        contentArea.setCaretPosition(0);
	        splitPane.setBottomComponent(areaScrollPane);
	        docPanel.add(splitPane, BorderLayout.CENTER);
	        splitPane.setDividerLocation(0.2);
    	}
    	return docPanel;
    }

    private int fillCollectionDocuments(DefaultMutableTreeNode clNode, String clName) {
        try {
            List<Document> documents = docMgr.getDocuments(clName);
        	Collections.sort(documents);
        	int cnt = clNode.getChildCount(); 
            for (Document document: documents) {
                DefaultMutableTreeNode doc = new DefaultMutableTreeNode(document);
                clNode.add(doc);
            }
            cnt = clNode.getChildCount() - cnt;
            LOGGER.info("fillCollectionDocuments; added: " + cnt + "; returning: " + documents.size()); 
            return documents.size();
		} catch (Exception ex) {
            LOGGER.throwing(this.getClass().getName(), "fillCollectionDocuments", ex);
		}
        return 0;
    }
    
    private void selectDocument(String uri) {
    	getDocumentManagementPanel();
    	try {
			Map<String, Object> doc = docMgr.getDocumentInfo(uri);
			((TitledBorder) docInfoPanel.getBorder()).setTitle("document: " + doc.get("uri"));
			Component[] labels = docInfoPanel.getComponents();
			((JLabel) labels[0]).setText("key: " + doc.get("key"));
			//((JLabel) labels[1]).setText("id: " + doc.get("id"));
			((JLabel) labels[1]).setText("version: " + doc.get("version"));
			((JLabel) labels[2]).setText("uri: " + doc.get("uri"));
			((JLabel) labels[3]).setText("size: " + doc.get("bytes"));
			((JLabel) labels[4]).setText("encoding: " + doc.get("encoding"));
			((JLabel) labels[5]).setText("created at: " + doc.get("created at"));
			((JLabel) labels[6]).setText("created by: " + doc.get("created by"));
			((JLabel) labels[7]).setText("start tx: " + doc.get("txStart"));
			((JLabel) labels[8]).setText("elements: " + doc.get("elements"));
			((JLabel) labels[9]).setText("fragments: " + doc.get("fragments"));
			((JLabel) labels[10]).setText("collections: " + doc.get("collections"));
			((JLabel) labels[11]).setText("partition: " + doc.get("partition"));
			((JLabel) labels[12]).setText("owner: " + doc.get("owner"));
			docInfoPanel.repaint();

			String content = docMgr.getDocumentContent(uri);
	    	contentArea.setText(content);
	    	contentArea.setCaretPosition(0);
    	} catch (ServiceException ex) {
    		handleServiceException(ex);
    	}
    	mgrSplitter.setRightComponent(docPanel);
    }
    
    private DefaultMutableTreeNode getCollectionNode(String collection) {
    	Object root = docTree.getModel().getRoot();
    	int cnt = docTree.getModel().getChildCount(root);
    	for (int i=0; i < cnt; i++) {
    		DefaultMutableTreeNode node = (DefaultMutableTreeNode) docTree.getModel().getChild(root, i);
    		if (collection.equals(((Collection) node.getUserObject()).getName())) {
    			return node;
    		}
    	}
    	return null;
    }
    
    private void refreshCollection(DefaultMutableTreeNode clnNode, String collection) {
        try {
        	Collection cln = docMgr.getCollection(collection);
        	if (cln != null) {
        		clnNode.setUserObject(cln);
        		((DefaultTreeModel) docTree.getModel()).reload(clnNode);
        	}
        } catch (ServiceException ex) {
            ErrorUtil.showError(this.getParent(), ex);
        }
    }
    
    private void insertDocument(Document doc, String collection) {
    	DefaultMutableTreeNode parent = getCollectionNode(collection);
    	if (parent != null) {
	    	int idx = 0;
	    	DefaultMutableTreeNode node = null;
	    	Enumeration en = parent.children();
	    	while (en.hasMoreElements()) {
	    		DefaultMutableTreeNode child = (DefaultMutableTreeNode) en.nextElement();
	    		Document exDoc = (Document) child.getUserObject();
	    		int cmp = doc.compareTo(exDoc); 
	    		if (cmp < 0) {
	    			break;
	    		} else if (cmp == 0) {
	    			node = child;
	    			break;
	    		}
	    		idx++;
	    	}

	    	if (node == null) {
	    		node = new DefaultMutableTreeNode(doc);
	    		((DefaultTreeModel) docTree.getModel()).insertNodeInto(node, parent, idx);
	    		refreshCollection(parent, collection);
	    	} else {
	    		node.setUserObject(doc);
	    	}
	        docTree.setSelectionPaths(new TreePath[] {new TreePath(node.getPath())});
    	} else {
    		LOGGER.info("insertDocument; no node found for collection: " + collection);
    	}
    }
    
    
    private void insertDocument(Document doc, java.util.Collection<String> collections) {
    	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    	try {
	    	if (collections != null) {
	    		for (String collection: collections) {
	    			insertDocument(doc, collection);
	    		}
	    	}
	    	insertDocument(doc, all_docs);
    	} finally {
    		setCursor(Cursor.getDefaultCursor());
    	}
    }

	private void refreshDocuments(String collection) {
    	DefaultMutableTreeNode parent = getCollectionNode(collection);
    	if (parent != null) {
    		parent.removeAllChildren();
    		fillCollectionDocuments(parent, collection);
    		refreshCollection(parent, collection);
    	} else {
    		LOGGER.info("refreshDocuments; no node found for collection: " + collection);
    	}
	}

	private void refreshDocuments(java.util.Collection<String> collections) {
    	setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    	try {
	    	if (collections != null) {
	    		for (String collection: collections) {
	    			refreshDocuments(collection);
	    		}
	    	}
	    	refreshDocuments(all_docs);
    	} finally {
    		setCursor(Cursor.getDefaultCursor());
    	}
	}
	
	private void removeSelectedDocument() {
		DefaultMutableTreeNode nextNode;
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) docTree.getLastSelectedPathComponent();
		if (selectedNode.getNextSibling() != null) {
			nextNode = selectedNode.getNextSibling();
		} else if (selectedNode.getPreviousSibling() != null) {
			nextNode = selectedNode.getPreviousSibling();
		} else {
			nextNode = (DefaultMutableTreeNode) selectedNode.getParent();
		}
		((DefaultTreeModel) docTree.getModel()).removeNodeFromParent(selectedNode);
        docTree.setSelectionPaths(new TreePath[] {new TreePath(nextNode.getPath())});
	}
	
	private List<String> getCollectionNames() {
		List<String> names = new ArrayList<>(collections.size());
		for (Collection cln: collections) {
			names.add(cln.getName());
		}
		return names;
	}

    // --- Event Handlers --- //
    private void onAddDocument() {
    	JFileChooser chooser = new JFileChooser();
    	chooser.setDialogTitle("Select File");
    	String docType = schema.getDataFormat();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(docType + " Documents", docType.toLowerCase());
        chooser.setFileFilter(filter);
        if (currentPath != null) {
        	chooser.setCurrentDirectory(new File(currentPath));
        }
        CollectionPanel cp = null;
        if (collections != null) {
        	cp = new CollectionPanel(getCollectionNames());
        	chooser.setAccessory(cp);
        }
        int returnVal = chooser.showOpenDialog(this.getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	currentPath = chooser.getCurrentDirectory().getAbsolutePath();
    		java.util.Collection<String> clns = null;
    		if (cp != null) {
    			clns = cp.getSelectedCollections();
    		}
        	try {
				Document doc = docMgr.storeDocument(chooser.getSelectedFile().getAbsolutePath(), clns);
				insertDocument(doc, clns);
        	} catch (ServiceException ex) {
                ErrorUtil.showError(this.getParent(), ex);
			}
        }
    }

    private void onAddDocuments() {
    	JFileChooser chooser = new JFileChooser();
    	chooser.setDialogTitle("Select Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (currentPath != null) {
        	chooser.setCurrentDirectory(new File(currentPath));
        }
        CollectionPanel cp = null;
        if (collections != null) {
        	cp = new CollectionPanel(getCollectionNames());
        	chooser.setAccessory(cp);
        }
        int returnVal = chooser.showOpenDialog(this.getParent());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
        	currentPath = chooser.getCurrentDirectory().getAbsolutePath();
    		java.util.Collection<String> clns = null;
    		if (cp != null) {
    			clns = cp.getSelectedCollections();
    		}
        	try {
				if (docMgr.storeDocuments(chooser.getSelectedFile().getAbsolutePath(), clns)) {
					refreshDocuments(clns);
				}
        	} catch (Exception ex) {
                ErrorUtil.showError(this.getParent(), ex);
			}
        }
    }

    private void onDeleteDocument() {
    	if (docTree.getSelectionPath() != null) {
    		DefaultMutableTreeNode node = (DefaultMutableTreeNode) docTree.getSelectionPath().getLastPathComponent();
    		Document doc = (Document) node.getUserObject();
   			try {
    			docMgr.deleteDocument(doc.getUri());
    			// TODO: delete it from all collections!
    			removeSelectedDocument();
    		} catch (ServiceException ex) {
                ErrorUtil.showError(this.getParent(), ex);
    		}
    	}
    }

    private void onAddCollection() {
    	//
    }

    private void onDeleteCollection() {
    	//
    }
    
    private void onDeleteDocuments() {
    	//
    }
    
    private void onAddDocumentToCollection() {
    	//
    }
    
    private void onRemoveDocumentFromCollection() {
    	//
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
