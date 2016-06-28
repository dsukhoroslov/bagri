package com.bagri.tools.vvm.ui;

import javax.swing.*;

import com.bagri.tools.vvm.event.ApplicationEvent;
import com.bagri.tools.vvm.util.WindowUtil;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.Arrays;

public class ChangePasswordDialog  extends JDialog {
    private static final KeyStroke ESCAPE_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    private JPasswordField currentPassword;
    private JPasswordField newPassword;
    private JPasswordField retypePassword;
    private ActionListener successListener;

    public ChangePasswordDialog(JComponent owner) {
        super(WindowUtil.getFrameForComponent(owner), "Change password", true);
        //
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbCurrentPassword = new JLabel("Current password: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbCurrentPassword, cs);

        currentPassword = new JPasswordField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        panel.add(currentPassword, cs);

        JLabel lbNewPassword = new JLabel("Type new password: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lbNewPassword, cs);

        newPassword = new JPasswordField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        panel.add(newPassword, cs);

        JLabel lbRetypePassword = new JLabel("Re-type new password: ");
        cs.gridx = 0;
        cs.gridy = 2;
        cs.gridwidth = 1;
        panel.add(lbRetypePassword, cs);

        retypePassword = new JPasswordField(20);
        cs.gridx = 1;
        cs.gridy = 2;
        cs.gridwidth = 2;
        panel.add(retypePassword, cs);
//        panel.setBorder(new LineBorder(Color.GRAY));

        JButton changeButton = new JButton("Change");

        changeButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (!Arrays.equals(newPassword.getPassword(), retypePassword.getPassword())) {
                    JOptionPane.showMessageDialog(ChangePasswordDialog.this,
                            "Passwords do not match.",
                            "New password error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (null != successListener) {
                    successListener.actionPerformed(new ActionEvent(ChangePasswordDialog.this, e.getID(), "changePassword"));
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
        bp.add(changeButton);
        bp.add(cancelButton);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);

        Action dispatchClosing = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                dispatchEvent(new WindowEvent(
                        ChangePasswordDialog.this, WindowEvent.WINDOW_CLOSING
                ));
            }
        };
        JRootPane root = getRootPane();
        root.setDefaultButton(changeButton);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ESCAPE_STROKE, ApplicationEvent.DISPATCH_WINDOW_CLOSING_ACTION);
        root.getActionMap().put(ApplicationEvent.DISPATCH_WINDOW_CLOSING_ACTION, dispatchClosing);
    }

    public void setSuccessListener(ActionListener successListener) {
        this.successListener = successListener;
    }

    public String getCurrentPassword() {
        return new String(currentPassword.getPassword());
    }

    public String getNewPassword() {
        return new String(newPassword.getPassword());
    }

    public String getRetypePassword() {
        return new String(retypePassword.getPassword());
    }

    public static void main(String[] args) {
        ChangePasswordDialog dlg = new ChangePasswordDialog(null) {
            @Override
            public void dispose() {
                super.dispose();
                System.exit(0);
            }
        };
        dlg.setSuccessListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ChangePasswordDialog src = (ChangePasswordDialog) e.getSource();
                System.out.println("current =" + src.getCurrentPassword());
                System.out.println("new =" + src.getNewPassword());
                System.out.println("retype =" + src.getRetypePassword());
            }
        });
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dlg.setVisible(true);
    }

}
