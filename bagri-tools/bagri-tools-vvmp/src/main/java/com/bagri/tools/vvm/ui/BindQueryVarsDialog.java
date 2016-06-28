package com.bagri.tools.vvm.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import com.bagri.tools.vvm.event.ApplicationEvent;
import com.bagri.tools.vvm.model.TypedValue;
import com.bagri.tools.vvm.util.WindowUtil;

public class BindQueryVarsDialog extends JDialog {

	private static final String[] types = {"boolean", "byte", "date", "dateTime", "double", "file", "float", "int", "long", "short", "string"};
	
	private static final KeyStroke ESCAPE_STROKE = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
	private ActionListener successListener;
	private JPanel ctrlPanel;
	

	public BindQueryVarsDialog(List<String> variables, Map<String, TypedValue> bindings, JComponent owner) {
		super(WindowUtil.getFrameForComponent(owner), true);
		setTitle("Bind Query Variables");
		ctrlPanel = new JPanel(new GridBagLayout());
		GridBagConstraints cs = new GridBagConstraints();

		cs.fill = GridBagConstraints.HORIZONTAL;

		JLabel lbVarName = new JLabel("Name");
		cs.gridx = 0;
		cs.gridy = 0;
		lbVarName.setHorizontalAlignment(JLabel.CENTER);
		ctrlPanel.add(lbVarName, cs);

		JLabel lbVarType = new JLabel("Type");
		cs.gridx = 1;
		cs.gridy = 0;
		lbVarType.setHorizontalAlignment(JLabel.CENTER);
		ctrlPanel.add(lbVarType, cs);
		
		JLabel lbVarValue = new JLabel("Value");
		cs.gridx = 2;
		cs.gridy = 0;
		lbVarValue.setHorizontalAlignment(JLabel.CENTER);
		ctrlPanel.add(lbVarValue, cs);

		Collections.sort(variables);
		for (int i=0; i < variables.size(); i++) {
			String var = variables.get(i);
			TypedValue oldVal = bindings.get(var);

			JLabel lbVar = new JLabel(var + ": ");
			cs.gridx = 0;
			cs.gridy = i+1;
			lbVar.setToolTipText(var);
			lbVar.setHorizontalAlignment(JLabel.RIGHT);
			ctrlPanel.add(lbVar, cs);
			
			JComboBox<String> cbType = new JComboBox<>(types);
			cs.gridx = 1;
			cs.gridy = i+1;
			ctrlPanel.add(cbType, cs);
			
			JTextField tfValue = new JTextField(15);
			cs.gridx = 2;
			cs.gridy = i+1;
			ctrlPanel.add(tfValue, cs);

			if (oldVal == null) { 
				cbType.setSelectedItem("string");
			} else {
				cbType.setSelectedItem(oldVal.getType());
				tfValue.setText(oldVal.getValue().toString());
			}
		}
		
		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (null != successListener) {
					successListener
							.actionPerformed(new ActionEvent(BindQueryVarsDialog.this, e.getID(), "bindQueryVars"));
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

		getContentPane().add(ctrlPanel, BorderLayout.CENTER);
		getContentPane().add(bp, BorderLayout.PAGE_END);

		pack();
		setResizable(false);
		setLocationRelativeTo(owner);

		Action dispatchClosing = new AbstractAction() {
			public void actionPerformed(ActionEvent event) {
				dispatchEvent(new WindowEvent(BindQueryVarsDialog.this, WindowEvent.WINDOW_CLOSING));
			}
		};
		JRootPane root = getRootPane();
		root.setDefaultButton(okButton);
		root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ESCAPE_STROKE,
				ApplicationEvent.DISPATCH_WINDOW_CLOSING_ACTION);
		root.getActionMap().put(ApplicationEvent.DISPATCH_WINDOW_CLOSING_ACTION, dispatchClosing);

	}
	
	public Map<String, TypedValue> getBindings() { //throws Exception {
		Component[] comps = ctrlPanel.getComponents();
		Map<String, TypedValue> result = new HashMap<>();
		for (int i=3; i < comps.length; i++) {
			String name = ((JLabel) comps[i]).getToolTipText();
			i++;
			String type = (String) ((JComboBox) comps[i]).getSelectedItem();
			i++;
			String value = ((JTextField) comps[i]).getText();
			Class cls = type2Class(type);
			if (cls.equals(String.class)) {
				result.put(name, new TypedValue(type, value));
			//} else if (cls.equals(java.util.Date.class)) {
			//	new java.util.Date()
			} else {
				result.put(name, new TypedValue(type, getValue(cls, value)));
			}
		}
		return result;
	}

	public void setSuccessListener(ActionListener successListener) {
		this.successListener = successListener;
	}
	
	private static Class type2Class(String type) {
		switch (type) {
			case "boolean": return Boolean.class; 
			case "byte": return Byte.class;
			case "date":
			case "dateTime": return java.util.Date.class; 
			case "double": return Double.class; 
			case "float": return Float.class; 
			case "int": return Integer.class; 
			case "long": return Long.class; 
			case "short": return Short.class;
			default: return String.class;
		}
	}

	private static Object getValue(Class<?> cls, String value) { //throws Exception {
		try {
			Constructor<?> c = cls.getConstructor(String.class);
			return c.newInstance(value);
		} catch (Exception ex) {
			// log ex..
			return value;
		}
	}
	
}
