package com.bagri.tools.vvm.ui;

import javax.swing.*;

import com.bagri.tools.vvm.event.ApplicationEvent;
import com.bagri.tools.vvm.model.NodeOption;
import com.bagri.tools.vvm.util.WindowUtil;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

public class EditNodeOptionDialog extends JDialog {
    private static final KeyStroke ESCAPE_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    private JTextField optionName;
    private JTextField optionValue;
    private ActionListener successListener;
    private enum Mode {
        ADD,
        EDIT
    }

    public EditNodeOptionDialog(NodeOption option, JComponent owner) {
        super(WindowUtil.getFrameForComponent(owner), true);
        Mode mode = option == null ? Mode.ADD: Mode.EDIT;
        setTitle((mode == Mode.ADD ? "Add": "Edit") + " Option");
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints cs = new GridBagConstraints();

        cs.fill = GridBagConstraints.HORIZONTAL;

        JLabel lbOptionName = new JLabel("Option name: ");
        cs.gridx = 0;
        cs.gridy = 0;
        cs.gridwidth = 1;
        panel.add(lbOptionName, cs);

        optionName = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 0;
        cs.gridwidth = 2;
        if (mode == Mode.EDIT) {
            optionName.setText(option.getOptionName());
        }
        panel.add(optionName, cs);

        JLabel lbOptionValue = new JLabel("OptionValue: ");
        cs.gridx = 0;
        cs.gridy = 1;
        cs.gridwidth = 1;
        panel.add(lbOptionValue, cs);

        optionValue = new JTextField(20);
        cs.gridx = 1;
        cs.gridy = 1;
        cs.gridwidth = 2;
        if (mode == Mode.EDIT) {
            optionValue.setText(option.getOptionValue());
        }
        panel.add(optionValue, cs);
        JButton editButton = new JButton(mode == Mode.ADD ? "Add" : "Update");
        editButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (null != successListener) {
                    successListener.actionPerformed(new ActionEvent(EditNodeOptionDialog.this, e.getID(), "editNodeOption"));
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
                        EditNodeOptionDialog.this, WindowEvent.WINDOW_CLOSING
                ));
            }
        };
        JRootPane root = getRootPane();
        root.setDefaultButton(editButton);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ESCAPE_STROKE, ApplicationEvent.DISPATCH_WINDOW_CLOSING_ACTION);
        root.getActionMap().put(ApplicationEvent.DISPATCH_WINDOW_CLOSING_ACTION, dispatchClosing );

    }

    public NodeOption getOption() {
        return new NodeOption(optionName.getText(), optionValue.getText());
    }

    public void setSuccessListener(ActionListener successListener) {
        this.successListener = successListener;
    }
}
