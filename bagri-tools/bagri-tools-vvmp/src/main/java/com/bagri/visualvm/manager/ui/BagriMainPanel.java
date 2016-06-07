package com.bagri.visualvm.manager.ui;

import com.bagri.visualvm.manager.event.*;
import com.bagri.visualvm.manager.model.*;
import com.bagri.visualvm.manager.service.*;
import com.bagri.visualvm.manager.util.ErrorUtil;
import com.bagri.visualvm.manager.util.Icons;
import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import com.sun.tools.attach.spi.AttachProvider;
import com.sun.tools.visualvm.tools.jmx.JmxModel;

import javax.management.*;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;

public class BagriMainPanel extends JPanel implements NotificationListener, PropertyChangeListener {

	private static final Logger LOGGER = Logger.getLogger(BagriMainPanel.class.getName());

    private final MainTreePanel mainTree;
    private final JSplitPane splitPane;
    private final UserManagementService userManagementService;
    private final ClusterManagementService clusterManagementService;
    private final SchemaManagementService schemaManagementService;
    private final EventBus<ApplicationEvent> eventBus = new EventBus<ApplicationEvent>();
    private UserManagementPanel userManagementPanel;
    private ClusterManagementPanel clusterManagementPanel;
    private BagriManagementPanel bagriManagementPanel;
    private SchemaManagementPanel schemaManagementPanel;
    // TODO: Remove cache entry if schema is deleted.
    private HashMap<String, SchemaPanel> schemaCache = new HashMap<String, SchemaPanel>();

    public BagriMainPanel(BagriServiceProvider serviceProvider) {
        // Services
        userManagementService = serviceProvider.getUserManagement();
        clusterManagementService = serviceProvider.getClusterManagement();
        schemaManagementService = serviceProvider.getSchemaManagement();

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        // Main tree
        mainTree = createTree();
        Dimension minimumSize = new Dimension(150, 100);//TODO: Move to constants
        JScrollPane treeScrollPane = new JScrollPane(mainTree);
        treeScrollPane.setMinimumSize(minimumSize);

        JPanel emptyPanel = new JPanel();
        JScrollPane rightScrollPane = new JScrollPane(emptyPanel);

        //Create a split pane with the two scroll panes in it.
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                treeScrollPane, rightScrollPane);
        splitPane.setDividerLocation(200);//TODO: Move to constants
        add(splitPane);
        setOpaque(true);

        eventBus.addEventHandler(new EventHandler<ApplicationEvent>() {
            @Override
            public void handleEvent(ApplicationEvent e) {
                if (ClusterManagement.CLUSTER_STATE_CHANGED.equals(e.getCommand())) {
                    try {
                        java.util.List<Node> nodes = clusterManagementService.getNodes();
                        mainTree.setNodes(nodes);
                    } catch (ServiceException e1) {
                        ErrorUtil.showError(BagriMainPanel.this, e1);
                    }
                }
                if (SchemaManagement.SCHEMA_STATE_CHANGED.equals(e.getCommand())) {
                    try {
                        java.util.List<Schema> schemas = schemaManagementService.getSchemas();
                        mainTree.setSchemas(schemas);
                    } catch (ServiceException e1) {
                        ErrorUtil.showError(BagriMainPanel.this, e1);
                    }
                }
                if (BagriManager.MANAGEMENT_SECTION_SELECTED.equals(e.getCommand())) {
                    TreePath tp = mainTree.findTreePath(e.getSource().toString());
                    if (null != tp) {
                        mainTree.setSelectionPath(tp);
                    }
                }
            }
        });
    }

    protected JComponent makeTextPanel(String text) {
        JPanel panel = new JPanel(false);
        JLabel filler = new JLabel(text);
        filler.setHorizontalAlignment(JLabel.CENTER);
        panel.setLayout(new GridLayout(1, 1));
        panel.add(filler);
        return panel;
    }

    private MainTreePanel createTree() {
        final MainTreePanel tree = new MainTreePanel();
        java.util.List<Node> nodes = null;
        try {
            nodes = clusterManagementService.getNodes();
            tree.setNodes(nodes);
        } catch (ServiceException e) {
            LOGGER.throwing(BagriMainPanel.class.getName(), "createTree", e);
            ErrorUtil.showError(BagriMainPanel.this, e);
        }
        
        java.util.List<Schema> schemas = null;
        try {
            schemas = schemaManagementService.getSchemas();
            tree.setSchemas(schemas);
        } catch (ServiceException e) {
            LOGGER.throwing(BagriMainPanel.class.getName(), "createTree", e);
            ErrorUtil.showError(BagriMainPanel.this, e);
        }
        
        tree.addMouseListener ( new MouseAdapter() {
            public void mousePressed ( MouseEvent e ) {
                if ( SwingUtilities.isRightMouseButton ( e ) ) {
                    int row = tree.getClosestRowForLocation(e.getX(), e.getY());
                    tree.setSelectionRow(row);
                    TreePath path = tree.getPathForLocation ( e.getX (), e.getY () );
                    Rectangle pathBounds = tree.getPathBounds(path);
                    if ( pathBounds != null && pathBounds.contains ( e.getX (), e.getY ()) && null != path ){
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                        final Object o = node.getUserObject();
//                        if (o instanceof User) {
//                            JPopupMenu menu = new JPopupMenu ();
//                            JMenuItem menuItem = new JMenuItem ( "Remove User" );
//                            menuItem.addActionListener(new ActionListener() {
//                                @Override
//                                public void actionPerformed(ActionEvent e) {
//                                    try {
//                                        userManagementService.deleteUser(((User) o).getUserName());
//                                    } catch (ServiceException e1) {
//                                        LOGGER.throwing(BagriMainPanel.class.getName(), "onRemoveUser", e1);
//                                    }
//                                }
//                            });
//                            menu.add ( menuItem );
//                            menu.show ( tree, e.getX(), e.getY() );
//                        }
                        if (o instanceof UserManagement) {
                            JPopupMenu menu = new JPopupMenu ();
                            JMenuItem menuItem = new JMenuItem ( "Add User" );
                            menuItem.setIcon(Icons.ADD_ICON);
                            menuItem.addActionListener(new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    AddUserDialog dlg = new AddUserDialog(BagriMainPanel.this);
                                    dlg.setSuccessListener(new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            final AddUserDialog src = (AddUserDialog) e.getSource();
                                            SwingUtilities.invokeLater(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        userManagementService.addUser(src.getUsername(), src.getPassword());
                                                        eventBus.fireEvent(new ApplicationEvent(src, "AddUser"));
                                                    } catch (ServiceException e1) {
                                                        LOGGER.throwing(BagriMainPanel.class.getName(), "onAddUser", e1);
                                                        ErrorUtil.showError(BagriMainPanel.this, e1);
                                                    }
                                                }
                                            });
                                        }
                                    });
                                    dlg.setVisible(true);
                                }
                            });
                            menu.add ( menuItem );
                            menu.show ( tree, e.getX(), e.getY() );
                        }
                    }
                }
            }
        } );
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                        tree.getLastSelectedPathComponent();

                if (node == null) return;

                int dividerLocation = splitPane.getDividerLocation();
                Object nodeInfo = node.getUserObject();
                if (nodeInfo instanceof UserManagement) {
                    splitPane.setRightComponent(getUserManagementView());
                } else if (nodeInfo instanceof ClusterManagement) {
                    splitPane.setRightComponent(getClusterManagementPanel());
                } else if (nodeInfo instanceof Node) {
                    splitPane.setRightComponent(new NodeManagementPanel(BagriMainPanel.this.clusterManagementService, (Node)nodeInfo));
                } else if (nodeInfo instanceof SchemaManagement) {
                    splitPane.setRightComponent(getSchemaManagementPanel());
                } else if (nodeInfo instanceof Schema) {
                    Schema s = (Schema) nodeInfo;
                    SchemaPanel panel = schemaCache.get(s.getSchemaName());
                    if (null == panel) {
                        panel = new SchemaPanel(schemaManagementService, eventBus, s);
                        schemaCache.put(s.getSchemaName(), panel);
                    }
                    splitPane.setRightComponent(panel);
                } else if (nodeInfo instanceof BagriManager) {
                    splitPane.setRightComponent(getBagriManagementPanel());
                } else {
                    JTabbedPane tabbedPane = new JTabbedPane();
                    tabbedPane.addTab(nodeInfo.toString(), makeTextPanel(nodeInfo.toString() + " placeholder"));
                    splitPane.setRightComponent(tabbedPane);
                }
                splitPane.setDividerLocation(dividerLocation);
            }
        });
        return tree;
    }

    private UserManagementPanel getUserManagementView() {
        if (null == userManagementPanel) {
            userManagementPanel = new UserManagementPanel(userManagementService, eventBus);
        }
        return userManagementPanel;
    }

    private ClusterManagementPanel getClusterManagementPanel() {
        if (null == clusterManagementPanel) {
            clusterManagementPanel = new ClusterManagementPanel(clusterManagementService, eventBus);
        }
        return clusterManagementPanel;
    }

    private SchemaManagementPanel getSchemaManagementPanel() {
        if (null == schemaManagementPanel) {
            schemaManagementPanel = new SchemaManagementPanel(schemaManagementService, eventBus);
        }
        return schemaManagementPanel;
    }

    private BagriManagementPanel getBagriManagementPanel() {
        if (null == bagriManagementPanel) {
            bagriManagementPanel = new BagriManagementPanel(eventBus);
        }
        return bagriManagementPanel;
    }

/*
    private String[] getSchemas(MBeanServerConnection connection) {
        try {
            Object res = connection.invoke(new ObjectName("com.bagri.xdm:type=Management,name=SchemaManagement"), "getSchemaNames", null, null);
            return (String[]) res;
        } catch (Exception e) {
            LOGGER.throwing(this.getClass().getName(), "getSchemaNames", e);
            return new String[]{};
        }
    }
*/


    /* notification listener:  handleNotification */
    @Override
    public void handleNotification(final Notification notification, Object handback) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (notification instanceof JMXConnectionNotification) {
                    if (JMXConnectionNotification.FAILED.equals(notification.getType()) || JMXConnectionNotification.CLOSED.equals(notification.getType())) {
                        dispose();
                    }
                }
                if (notification instanceof MBeanServerNotification) {
                    ObjectName mbean =
                            ((MBeanServerNotification) notification).getMBeanName();
                    if (notification.getType().equals(
                            MBeanServerNotification.REGISTRATION_NOTIFICATION)) {
                        if ("User".equals(mbean.getKeyProperty("type"))) {
                            eventBus.fireEvent(new ApplicationEvent(this, UserManagement.USER_STATE_CHANGED));
                        }
                        if ("Node".equals(mbean.getKeyProperty("type"))) {
                            eventBus.fireEvent(new ApplicationEvent(this, ClusterManagement.CLUSTER_STATE_CHANGED));
                        }
                        if ("Schema".equals(mbean.getKeyProperty("type"))) {
                            eventBus.fireEvent(new ApplicationEvent(this, SchemaManagement.SCHEMA_STATE_CHANGED));
                        }
                    } else if (notification.getType().equals(
                            MBeanServerNotification.UNREGISTRATION_NOTIFICATION)) {
                        if ("User".equals(mbean.getKeyProperty("type"))) {
                            eventBus.fireEvent(new ApplicationEvent(this, UserManagement.USER_STATE_CHANGED));
                        }
                        if ("Node".equals(mbean.getKeyProperty("type"))) {
                            eventBus.fireEvent(new ApplicationEvent(this, ClusterManagement.CLUSTER_STATE_CHANGED));
                        }
                        if ("Schema".equals(mbean.getKeyProperty("type"))) {
                            eventBus.fireEvent(new ApplicationEvent(this, SchemaManagement.SCHEMA_STATE_CHANGED));
                        }
                    }
                }
            }
        });

    }

    public void dispose() {
        removePropertyChangeListener(this);
        mainTree.setEnabled(false);
        splitPane.getRightComponent().setEnabled(false);
        splitPane.setEnabled(false);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (JmxModel.CONNECTION_STATE_PROPERTY.equals(evt.getPropertyName())) {
            JmxModel.ConnectionState newState = (JmxModel.ConnectionState) evt.getNewValue();
            switch (newState) {
                case DISCONNECTED:
                    dispose();
                    break;
            }
        }
    }

//--------------------------- For testing and debugging only ---------------------------------------------------------//
    public static void main(String[] args) throws Exception {
        // Look and feel
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

        MBeanServerConnection mbsc = getMBeanServerConnection();
        Object o = null;
        try {
            o = mbsc.getObjectInstance(new ObjectName("com.bagri.xdm:type=Management,name=ClusterManagement"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (o != null) {
        	BagriServiceProvider bsp = DefaultServiceProvider.getInstance(mbsc);
            final BagriMainPanel panel = new BagriMainPanel(bsp);
            WindowListener windowAdapter = new WindowAdapter() {
                public void windowClosing(WindowEvent e) {System.exit(0);}
            };
            JFrame frame = new JFrame("Bagri Manager");
            frame.addWindowListener(windowAdapter);
            frame.getContentPane().add("Center", panel);
            frame.pack();
            frame.setSize(new Dimension(800, 500));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }
    }

    private static MBeanServerConnection getMBeanServerConnection() throws Exception {
        final AttachProvider attachProvider = AttachProvider.providers().get(0);

        VirtualMachineDescriptor descriptor = null;
        for (VirtualMachineDescriptor virtualMachineDescriptor : attachProvider.listVirtualMachines()) {
            if (pickThisOne(virtualMachineDescriptor)) {
                descriptor = virtualMachineDescriptor;
                final VirtualMachine virtualMachine = attachProvider.attachVirtualMachine(descriptor);

                final JMXServiceURL target = getURLForVM(virtualMachine);
                final JMXConnector connector = JMXConnectorFactory.connect(target);
                final MBeanServerConnection remote = connector.getMBeanServerConnection();
                try {
                    Object o = remote.getObjectInstance(new ObjectName("com.bagri.xdm:type=Management,name=ClusterManagement"));
                    if (null != o) {
                        break;
                    }
                } catch (Exception e) {
                    // Swallow for now
                }
            }
        }

        if (descriptor == null) throw new RuntimeException("Bagri VM not found");

        final VirtualMachine virtualMachine = attachProvider.attachVirtualMachine(descriptor);

        final JMXServiceURL target = getURLForVM(virtualMachine);
        final JMXConnector connector = JMXConnectorFactory.connect(target);
        final MBeanServerConnection remote = connector.getMBeanServerConnection();
        return  remote;

    }

    private static boolean pickThisOne(VirtualMachineDescriptor virtualMachineDescriptor) {
        if ("com.bagri.xdm.cache.hazelcast.XDMCacheServer".equals(virtualMachineDescriptor.displayName())) {
            return true;
        }
        return false;
    }


    private static JMXServiceURL getURLForVM(VirtualMachine vm) throws Exception {
        final String CONNECTOR_ADDRESS =
                "com.sun.management.jmxremote.localConnectorAddress";
        // attach to the target application
//        final VirtualMachine vm = VirtualMachine.attach(pid);

        // get the connector address
        String connectorAddress =
                vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);

        // no connector address, so we start the JMX agent
        if (connectorAddress == null) {

            String agent = vm.getSystemProperties().getProperty("java.home") +
                    File.separator + "lib" + File.separator + "management-agent.jar";

            vm.loadAgent(agent);

            // agent is started, get the connector address
            connectorAddress =
                    vm.getAgentProperties().getProperty(CONNECTOR_ADDRESS);

            System.out.println("Starting up a new JMX agent: " + agent);

            assert connectorAddress != null;
        }
        return new JMXServiceURL(connectorAddress);
    }
}

