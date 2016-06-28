package com.bagri.tools.vvm.ui;

import javax.swing.*;

import com.bagri.tools.vvm.event.*;
import com.bagri.tools.vvm.model.*;
import com.bagri.tools.vvm.service.ServiceException;
import com.bagri.tools.vvm.service.UserManagementService;
import com.bagri.tools.vvm.util.ErrorUtil;

import static com.bagri.tools.vvm.util.Icons.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.logging.Logger;

public class UserManagementPanel extends JPanel {
    private static final Logger LOGGER = Logger.getLogger(UserManagementPanel.class.getName());
    private final UserManagementService userService;
    private final EventBus<ApplicationEvent> eventBus;
    private XTable grid;
    private JToolBar toolBar;
    private JTabbedPane tabbedPane;

    public UserManagementPanel(UserManagementService service, EventBus<ApplicationEvent> bus) {
        super(new GridLayout(1, 1));
        this.userService = service;
        this.eventBus = bus;

        tabbedPane = new JTabbedPane();
        tabbedPane.addTab(UserManagement.USER_MANAGEMENT, createMainPanel());
        add(tabbedPane);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder());
        setBorder(BorderFactory.createEmptyBorder());
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        toolBar = new JToolBar();
        // "Add User" button
        JButton addUser = new JButton("Add");
        addUser.setToolTipText("Adds new user");
        addUser.setIcon(ADD_ICON);
        addUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAdd();
            }
        });
        toolBar.add(addUser);
        toolBar.addSeparator();
        // "Delete User" button
        JButton deleteUser = new JButton("Delete");
        deleteUser.setToolTipText("Deletes selected user");
        deleteUser.setIcon(DELETE_ICON);
        deleteUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onDelete();
            }
        });
        toolBar.add(deleteUser);
        toolBar.addSeparator();
        // "Activate User" button
        JButton activateUser = new JButton("Activate");
        activateUser.setToolTipText("Activates/Deactivates user");
        activateUser.setIcon(EDIT_ICON);
        activateUser.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onActivate();
            }
        });
        toolBar.add(activateUser);
        toolBar.addSeparator();
        // "Change User Password" button
        JButton changePass = new JButton("Change Password");
        changePass.setToolTipText("Change user password");
        changePass.setIcon(EDIT_ICON);
        changePass.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onChangePass();
            }
        });
        toolBar.add(changePass);
        toolBar.setRollover(true);
        toolBar.addSeparator();
        // disable dragging
        toolBar.setFloatable(false);
        panel.add(toolBar, BorderLayout.PAGE_START);
        // Column configs
        ArrayList<ColumnConfig> configs = new ArrayList<ColumnConfig>();
        ColumnConfig c = new ColumnConfig();
        c.setHeader("User name");
        c.setColumnClass(String.class);
        c.setWidth(40);
        c.setResizable(true);
        configs.add(c);
        c = new ColumnConfig();
        c.setHeader("Active");
        c.setColumnClass(Boolean.class);
        c.setFixedWidth(45);
        configs.add(c);
        grid = new XTable(configs, new GridDataLoader() {
            @Override
            public List<GridRow> loadData() {
                List<User> users;
                try {
                    users = userService.getUsers();
                } catch (ServiceException e) {
                    ErrorUtil.showError(UserManagementPanel.this, e);
                    return null;
                }
                List<GridRow> rows = new ArrayList<GridRow>();
                for (User u : users) {
                    rows.add(new DefaultGridRow(u.getUserName(), new Object[]{u.getUserName(), u.isActive()}));
                }
                return rows;
            }
        });
        panel.add(new JScrollPane(grid), BorderLayout.CENTER);
        eventBus.addEventHandler(new EventHandler<ApplicationEvent>() {
            @Override
            public void handleEvent(ApplicationEvent e) {
                if (UserManagement.USER_STATE_CHANGED.equals(e.getCommand())) {
                    grid.reload();
                }
            }
        });
        return panel;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (!enabled) {
            grid.clearSelection();
            grid.getTableHeader().setEnabled(false);
            grid.setEnabled(false);
            for (Component c : toolBar.getComponents()) {
                c.setEnabled(false);
            }
            toolBar.setEnabled(false);
            for (Component c : tabbedPane.getComponents()) {
                c.setEnabled(false);
            }
            tabbedPane.setEnabled(false);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (!grid.isLoaded()) {
            grid.reload();
        }
    }

    // --- Event Handlers --- //
    private void onAdd() {
        final AddUserDialog dlg = new AddUserDialog(UserManagementPanel.this);
        dlg.setSuccessListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            userService.addUser(dlg.getUsername(), dlg.getPassword());
                        } catch (ServiceException e1) {
                            LOGGER.throwing(UserManagementPanel.class.getName(), "onAddUser", e1);
                            ErrorUtil.showError(UserManagementPanel.this, e1);
                        }
                        eventBus.fireEvent(new ApplicationEvent(dlg, UserManagement.USER_STATE_CHANGED));
                    }
                });
            }
        });
        dlg.setVisible(true);
    }

    private void onDelete() {
        int selectedIndex = grid.getSelectionModel().getLeadSelectionIndex();
        if (selectedIndex >= 0 && selectedIndex < grid.getModel().getRowCount()) {
            final GridRow row = ((GridTableModel) grid.getModel()).getRow(selectedIndex);
            int n = JOptionPane.showConfirmDialog(
                    UserManagementPanel.this,
                    "Are you sure you want to delete selected user \"" + row.getId().toString() + "\"?",
                    "Confirm deletion",
                    JOptionPane.YES_NO_OPTION);
            if (JOptionPane.YES_OPTION == n) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            userService.deleteUser(row.getId().toString());
                        } catch (ServiceException e1) {
                            LOGGER.throwing(UserManagementPanel.class.getName(), "onDeleteUser", e1);
                            ErrorUtil.showError(UserManagementPanel.this, e1);
                        }
                        eventBus.fireEvent(new ApplicationEvent(UserManagementPanel.this, UserManagement.USER_STATE_CHANGED));
                    }
                });
            }
        }
    }

    private void onActivate() {
        int selectedIndex = grid.getSelectionModel().getLeadSelectionIndex();
        if (selectedIndex >= 0 && selectedIndex < grid.getModel().getRowCount()) {
            final GridRow row = ((GridTableModel) grid.getModel()).getRow(selectedIndex);
            int n = JOptionPane.showConfirmDialog(
                    UserManagementPanel.this,
                    "Are you sure you want to Activate selected user \"" + row.getId().toString() + "\"?",
                    "Confirm activation",
                    JOptionPane.YES_NO_OPTION);
            if (JOptionPane.YES_OPTION == n) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            userService.activateUser(row.getId().toString(), true);
                        } catch (ServiceException e1) {
                            LOGGER.throwing(UserManagementPanel.class.getName(), "onActivateUser", e1);
                            ErrorUtil.showError(UserManagementPanel.this, e1);
                        }
                        eventBus.fireEvent(new ApplicationEvent(UserManagementPanel.this, UserManagement.USER_STATE_CHANGED));
                    }
                });
            }
        }
    }

    private void onChangePass() {
        int selectedIndex = grid.getSelectionModel().getLeadSelectionIndex();
        if (selectedIndex >= 0 && selectedIndex < grid.getModel().getRowCount()) {
            final GridRow row = ((GridTableModel) grid.getModel()).getRow(selectedIndex);
            final ChangePasswordDialog dlg = new ChangePasswordDialog(UserManagementPanel.this);
            dlg.setSuccessListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                userService.changePassword(row.getId().toString(), dlg.getNewPassword());
                            } catch (ServiceException e1) {
                                LOGGER.throwing(UserManagementPanel.class.getName(), "onChangePassword", e1);
                                ErrorUtil.showError(UserManagementPanel.this, e1);
                            }
                            eventBus.fireEvent(new ApplicationEvent(dlg, UserManagement.USER_STATE_CHANGED));
                        }
                    });
                }
            });
            dlg.setVisible(true);
        }
    }

    // --- FOR TESTING AND DEBUGGING PURPOSES ONLY --- //
    public static void main(String[] args) throws Exception {
        // Look and feel
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

        WindowListener windowAdapter = new WindowAdapter() {
            public void windowClosing(WindowEvent e) {System.exit(0);}
        };
        UserManagementPanel panel = new UserManagementPanel(new UserManagementService() {
            @Override
            public List<User> getUsers() throws ServiceException {
                ArrayList<User> users = new ArrayList<User>();
                users.add(new User("Test 1"));
                users.add(new User("Test 2"));
                users.add(new User("Test 3"));
                return users;
            }

            @Override
            public boolean addUser(String user, String password) throws ServiceException {
                return false;
            }

            @Override
            public boolean deleteUser(String user) throws ServiceException {
                return false;
            }

            @Override
            public boolean activateUser(String user, boolean activate) throws ServiceException {
                return false;
            }

            @Override
            public boolean changePassword(String user, String password) throws ServiceException {
                return false;
            }
        }, new EventBus<ApplicationEvent>());
        JFrame frame = new JFrame("User management");
        frame.addWindowListener(windowAdapter);
        frame.getContentPane().add("Center", panel);
        frame.pack();
        frame.setSize(new Dimension(400, 500));
        frame.setVisible(true);
    }
}
