package com.bagri.visualvm.manager.ui;

import com.bagri.visualvm.manager.model.*;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import static com.bagri.visualvm.manager.util.Icons.*;

public class MainTreePanel extends JPanel {
    private final DefaultMutableTreeNode clusterManagement;
    private final DefaultMutableTreeNode schemaManagement;
    private final DefaultMutableTreeNode userManagement;
    private JTree tree;

    public MainTreePanel() {
        super(new GridLayout(1,1));
        tree = new JTree();
        tree.setCellRenderer(new TreeCellRenderer());
        DefaultMutableTreeNode top = new DefaultMutableTreeNode(new BagriManager());

        this.clusterManagement = new DefaultMutableTreeNode(new ClusterManagement());
        top.add(clusterManagement);
        this.schemaManagement = new DefaultMutableTreeNode(new SchemaManagement());
        top.add(schemaManagement);
        this.userManagement = new DefaultMutableTreeNode(new UserManagement());
        top.add(userManagement);
        tree.setModel(new DefaultTreeModel(top, false));
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
        add(tree);
//        tree.setSelectionRow(tree.getRowForPath(new TreePath(clusterManagement.getPath())));
//        tree.setSelection
    }

    public TreePath findTreePath(String s) {
        Enumeration<DefaultMutableTreeNode> e = ((DefaultMutableTreeNode) tree.getModel().getRoot()).depthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = e.nextElement();
            if (node.toString().equalsIgnoreCase(s)) {
                return new TreePath(node.getPath());
            }
        }
        return null;
    }

    public void setSelectionPath(TreePath path) {
        tree.setSelectionPath(path);
    }

    public int getClosestRowForLocation(int x, int y) {
        return tree.getClosestRowForLocation(x, y);
    }

    public void setSelectionRow(int row) {
        tree.setSelectionRow(row);
    }

    public Object getLastSelectedPathComponent() {
        return tree.getLastSelectedPathComponent();
    }

    public TreePath getPathForLocation(int x, int y) {
        return tree.getPathForLocation(x, y);
    }

    public Rectangle getPathBounds(TreePath path) {
        return tree.getUI().getPathBounds(tree, path);
    }

    @Override
    public synchronized void addMouseListener(MouseListener l) {
        tree.addMouseListener(l);
    }

    public void addTreeSelectionListener(TreeSelectionListener listener) {
        tree.addTreeSelectionListener(listener);
    }

    public void setNodes(java.util.List<Node> nodes) {
        //Enumeration children = clusterManagement.children();
        // create cached collection
        //ArrayList<DefaultMutableTreeNode> cached = new ArrayList<DefaultMutableTreeNode>();
        //while (children.hasMoreElements()) {
        //    DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement();
        //    cached.add(child);
        //}
        //for (DefaultMutableTreeNode node :cached) {
        //    ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(node);
        //}
    	clusterManagement.removeAllChildren();
    	
    	Collections.sort(nodes);
        for (int i=0; i < nodes.size(); i++) {
        	Node node = nodes.get(i);
            //((DefaultTreeModel) tree.getModel()).insertNodeInto(new DefaultMutableTreeNode(node), clusterManagement, i);
        	clusterManagement.add(new DefaultMutableTreeNode(node));
        }
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    public void setSchemas(java.util.List<Schema> schemas) {
        //Enumeration children = schemaManagement.children();
        // create cached collection
        //ArrayList<DefaultMutableTreeNode> cached = new ArrayList<DefaultMutableTreeNode>();
        //while (children.hasMoreElements()) {
        //    DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
        //    cached.add(child);
        //}
        //for (DefaultMutableTreeNode node : cached) {
        //    ((DefaultTreeModel) tree.getModel()).removeNodeFromParent(node);
        //}
    	schemaManagement.removeAllChildren();
    	
    	Collections.sort(schemas);
        for (int i=0; i < schemas.size(); i++) {
        	Schema schema = schemas.get(i);
            //((DefaultTreeModel) tree.getModel()).insertNodeInto(new DefaultMutableTreeNode(schema), schemaManagement, i);
        	schemaManagement.add(new DefaultMutableTreeNode(schema));
        }
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
    }

    public void setUsers(String[] users) {
    	// add them to the tree too??
        for (String userName: users) {
            userManagement.add(new DefaultMutableTreeNode(new User(userName)));
        }
    }

    public void addUser(String user) {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.insertNodeInto(new DefaultMutableTreeNode(new User(user)), userManagement, userManagement.getChildCount());
    }

    public void removeUser(String user) {
        Enumeration children = userManagement.children();
        while (children.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement();
            User u = (User) child.getUserObject();
            if (user.equals(u.getUserName())) {
                DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
                model.removeNodeFromParent(child);
                break;
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            tree.clearSelection();
        }
    }


    private class TreeCellRenderer extends DefaultTreeCellRenderer {

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
            DefaultMutableTreeNode treeNode =
                    (DefaultMutableTreeNode)value;
            Object valueObject = treeNode.getUserObject();
            if (valueObject instanceof BagriManager) {
                return MAIN_ICON;
            }
            if (valueObject instanceof ClusterManagement) {
                return CLUSTER_MANAGEMENT_ICON;
            }
            if (valueObject instanceof SchemaManagement) {
                return SCHEMA_MANAGEMENT_ICON;
            }
            if (valueObject instanceof UserManagement) {
                return USER_MANAGEMENT_ICON;
            }
            if (valueObject instanceof Node) {
                return NODE_ICON;
            }
            if (valueObject instanceof Schema) {
                return SCHEMA_ICON;
            }
            return null;
        }
    }
}
