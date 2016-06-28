package com.bagri.tools.vvm.ui;

import javax.swing.*;

import com.bagri.tools.vvm.event.ApplicationEvent;
import com.bagri.tools.vvm.util.WindowUtil;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

public class AddUserDialog extends JDialog {
    private static final KeyStroke ESCAPE_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    private JTextField tfUsername;
    private JPasswordField pfPassword;
    private ActionListener successListener;

    public AddUserDialog(JComponent owner) {
        super(WindowUtil.getFrameForComponent(owner), "Add User", true);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbUsername = new JLabel("Username: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbUsername, cs);

        tfUsername = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(tfUsername, cs);

        JLabel lbPassword = new JLabel("Password: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lbPassword, cs);

        pfPassword = new JPasswordField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(pfPassword, cs);
//        panel.setBorder(new LineBorder(Color.GRAY));

        JButton addButton = new JButton("Add User");

        addButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (null != successListener) {
                    successListener.actionPerformed(new ActionEvent(AddUserDialog.this, e.getID(), "addUser"));
                }
                dispose();
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        JPanel bp = new JPanel();
        bp.add(addButton);
        bp.add(cancelButton);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);

        Action dispatchClosing = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                dispatchEvent(new WindowEvent(
                        AddUserDialog.this, WindowEvent.WINDOW_CLOSING
                ));
            }
        };
        JRootPane root = getRootPane();
        root.setDefaultButton(addButton);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ESCAPE_STROKE, ApplicationEvent.DISPATCH_WINDOW_CLOSING_ACTION);
        root.getActionMap().put(ApplicationEvent.DISPATCH_WINDOW_CLOSING_ACTION, dispatchClosing );
    }

    public void setSuccessListener(ActionListener successListener) {
        this.successListener = successListener;
    }

    public String getUsername() {
        return tfUsername.getText().trim();
    }

    public String getPassword() {
        return new String(pfPassword.getPassword());
    }

    public static void main(String[] args) {
        AddUserDialog dlg = new AddUserDialog(null) {
            @Override
            public void dispose() {
                super.dispose();
                System.exit(0);
            }
        };
        dlg.setSuccessListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                AddUserDialog src = (AddUserDialog) e.getSource();
                System.out.println("UserName =" + src.getUsername());
                System.out.println("Password =" + src.getPassword());
            }
        });
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.setVisible(true);
    }
}
