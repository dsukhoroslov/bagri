package com.bagri.tools.vvm.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

public class CollectionPanel extends JPanel {
	
	public CollectionPanel(Collection<String> collections) {
		super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		for (String cln: collections) {
			JCheckBox cb = new JCheckBox(cln);
			cb.setAlignmentY(LEFT_ALIGNMENT);
			add(cb);
		}
		setPreferredSize(new Dimension(150, 300));
        setBorder(BorderFactory.createTitledBorder("choose collections to add documents into: "));
	}
	
	public Collection<String> getSelectedCollections() {
		List<String> result = new ArrayList<>();
		for (Component comp: getComponents()) {
			JCheckBox cb = (JCheckBox) comp;
			if (cb.isSelected()) {
				result.add(cb.getText());
			}
		}
		return result;
	}

}
