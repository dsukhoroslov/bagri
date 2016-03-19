package com.bagri.visualvm.manager.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;

import com.bagri.visualvm.manager.event.ApplicationEvent;

public class EditPropertiesDialog extends JDialog {
	
    private static final KeyStroke ESCAPE_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	private Properties properties;
    private ActionListener successListener;
	
	public EditPropertiesDialog(Properties props, final JComponent owner) {
		this.properties = new Properties(properties);
		
        JPanel propertiesPanel = new PropertyManagementPanel(properties, owner);
        
        JButton okButton = new JButton("Ok");
        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                if (null != successListener) {
                    successListener.actionPerformed(new ActionEvent(EditPropertiesDialog.this, e.getID(), "editProperties"));
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
        bp.add(okButton);
        bp.add(cancelButton);

        getContentPane().add(propertiesPanel, BorderLayout.PAGE_START);
        getContentPane().add(bp, BorderLayout.PAGE_END);
        setPreferredSize(new Dimension(280, 300));
        setMinimumSize(new Dimension(280, 300));

        pack();
        setResizable(false);
        setLocationRelativeTo(owner);

        Action dispatchClosing = new AbstractAction() {
            public void actionPerformed(ActionEvent event) {
                dispatchEvent(new WindowEvent(
                        EditPropertiesDialog.this, WindowEvent.WINDOW_CLOSING
                ));
            }
        };
        JRootPane root = getRootPane();
        root.setDefaultButton(okButton);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ESCAPE_STROKE, ApplicationEvent.DISPATCH_WINDOW_CLOSING_ACTION);
        root.getActionMap().put(ApplicationEvent.DISPATCH_WINDOW_CLOSING_ACTION, dispatchClosing );
	}
	
	public Properties getProperties() {
		return properties;
	}

    public void setSuccessListener(ActionListener successListener) {
        this.successListener = successListener;
    }
}
