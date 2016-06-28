package com.bagri.tools.vvm.ui;

import javax.swing.*;

import com.bagri.tools.vvm.event.ApplicationEvent;
import com.bagri.tools.vvm.model.Property;
import com.bagri.tools.vvm.util.WindowUtil;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

public class EditPropertyDialog extends JDialog {
    private static final KeyStroke ESCAPE_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    private JTextField propertyName;
    private JTextField propertyValue;
    private ActionListener successListener;
    private enum Mode {
        ADD,
        EDIT
    }

    public EditPropertyDialog(Property property, String caption, JComponent owner) {
        super(WindowUtil.getFrameForComponent(owner), true);
        Mode mode = property == null ? Mode.ADD: Mode.EDIT;
        setTitle((mode == Mode.ADD ? "Add": "Edit") + " " + caption);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbPropertyName = new JLabel("Property Name: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbPropertyName, cs);

        propertyName = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        if (mode == Mode.EDIT) {
            propertyName.setText(property.getPropertyName());
        }
        panel.add(propertyName, cs);

        JLabel lbPropertyValue = new JLabel("Property Value: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lbPropertyValue, cs);

        propertyValue = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        if (mode == Mode.EDIT) {
            propertyValue.setText(property.getPropertyValue());
        }
        panel.add(propertyValue, cs);
        JButton editButton = new JButton(mode == Mode.ADD ? "Add" : "Update");
        editButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (null != successListener) {
                    successListener.actionPerformed(new ActionEvent(EditPropertyDialog.this, e.getID(), "editProperty"));
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
        bp.add(editButton);
        bp.add(cancelButton);

        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(bp, BorderLayout.PAGE_END);

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);

        Action dispatchClosing = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                dispatchEvent(new WindowEvent(
                        EditPropertyDialog.this, WindowEvent.WINDOW_CLOSING
                ));
            }
        };
        JRootPane root = getRootPane();
        root.setDefaultButton(editButton);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ESCAPE_STROKE, ApplicationEvent.DISPATCH_WINDOW_CLOSING_ACTION);
        root.getActionMap().put(ApplicationEvent.DISPATCH_WINDOW_CLOSING_ACTION, dispatchClosing );
    }

    public Property getProperty() {
        return new Property(propertyName.getText(), propertyValue.getText());
    }


    public void setSuccessListener(ActionListener successListener) {
        this.successListener = successListener;
    }
}
