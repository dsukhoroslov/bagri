package com.bagri.tools.vvm.ui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.List;

import javax.swing.*;

public class DualListBox<E> extends JPanel {

    private static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);

    private static final String ADD_ALL_BUTTON_LABEL = ">>";
    private static final String ADD_BUTTON_LABEL = ">";

    private static final String REMOVE_ALL_BUTTON_LABEL = "<<";
    private static final String REMOVE_BUTTON_LABEL = "<";

    private static final String DEFAULT_SOURCE_CHOICE_LABEL = "Available Choices";

    private static final String DEFAULT_DEST_CHOICE_LABEL = "Your Choices";

    private JLabel sourceLabel;

    private JList<E> sourceList;

    private SortedListModel<E> sourceListModel;

    private JList<E> destList;

    private SortedListModel<E> destListModel;

    private JLabel destLabel;

    private JButton addButton;
    private JButton addAllButton;

    private JButton removeButton;
    private JButton removeAllButton;

    public DualListBox() {
        initScreen();
    }

    public String getSourceChoicesTitle() {
        return sourceLabel.getText();
    }

    public void setSourceChoicesTitle(String newValue) {
        sourceLabel.setText(newValue);
    }

    public String getDestinationChoicesTitle() {
        return destLabel.getText();
    }

    public void setDestinationChoicesTitle(String newValue) {
        destLabel.setText(newValue);
    }

    public void clearSourceListModel() {
        sourceListModel.clear();
    }

    public void clearDestinationListModel() {
        destListModel.clear();
    }

    public void addSourceElements(ListModel<E> newValue) {
        fillListModel(sourceListModel, newValue);
    }

    public void setSourceElements(ListModel<E> newValue) {
        clearSourceListModel();
        addSourceElements(newValue);
    }

    public void addDestinationElements(ListModel<E> newValue) {
        fillListModel(destListModel, newValue);
    }

    private void fillListModel(SortedListModel<E> model, ListModel<E> newValues) {
        int size = newValues.getSize();
        for (int i = 0; i < size; i++) {
            model.add(newValues.getElementAt(i));
        }
    }

    public void addSourceElements(List<E> newValues) {
        fillListModel(sourceListModel, newValues);
    }

    public void setSourceElements(List<E> newValues) {
        clearSourceListModel();
        addSourceElements(newValues);
    }

    public void addDestinationElements(List<E> values) {
        fillListModel(destListModel, values);
    }

    public void setDestinationElements(List<E> values) {
        setDestinationElements(values, true);
    }

    public void setDestinationElements(List<E> values, boolean subtractFromSource) {
        clearDestinationListModel();
        addDestinationElements(values);
        if (subtractFromSource) {
            ListModel<E> sourceModel = sourceList.getModel();
            List<E> sourceElements = new ArrayList<E>(sourceModel.getSize());
            for (int i = 0; i < sourceModel.getSize(); i++) {
                sourceElements.add(sourceModel.getElementAt(i));
            }
            sourceElements.removeAll(values);
            setSourceElements(sourceElements);
        }
    }

    private void fillListModel(SortedListModel<E> model, List<E> newValues) {
        model.addAll(newValues);
    }

    public Iterator sourceIterator() {
        return sourceListModel.iterator();
    }

    public Iterator destinationIterator() {
        return destListModel.iterator();
    }

    public void setSourceCellRenderer(ListCellRenderer<E> newValue) {
        sourceList.setCellRenderer(newValue);
    }

    public ListCellRenderer getSourceCellRenderer() {
        return sourceList.getCellRenderer();
    }

    public void setDestinationCellRenderer(ListCellRenderer<E> newValue) {
        destList.setCellRenderer(newValue);
    }

    public ListCellRenderer getDestinationCellRenderer() {
        return destList.getCellRenderer();
    }

    public void setVisibleRowCount(int newValue) {
        sourceList.setVisibleRowCount(newValue);
        destList.setVisibleRowCount(newValue);
    }

    public int getVisibleRowCount() {
        return sourceList.getVisibleRowCount();
    }

    public void setSelectionBackground(Color newValue) {
        sourceList.setSelectionBackground(newValue);
        destList.setSelectionBackground(newValue);
    }

    public Color getSelectionBackground() {
        return sourceList.getSelectionBackground();
    }

    public void setSelectionForeground(Color newValue) {
        sourceList.setSelectionForeground(newValue);
        destList.setSelectionForeground(newValue);
    }

    public Color getSelectionForeground() {
        return sourceList.getSelectionForeground();
    }

    private void clearSourceSelected() {
//        Object selected[] = sourceList.getSelectedValues();
//        for (int i = selected.length - 1; i >= 0; --i) {
//            sourceListModel.removeElement(selected[i]);
//        }
        List<E> selected = sourceList.getSelectedValuesList();
        for (E sel : selected) {
            sourceListModel.removeElement(sel);
        }
        sourceList.getSelectionModel().clearSelection();
    }

    private void clearDestinationSelected() {
//        Object selected[] = destList.getSelectedValues();
//        for (int i = selected.length - 1; i >= 0; --i) {
//            destListModel.removeElement(selected[i]);
//        }
        List<E> selected = destList.getSelectedValuesList();
        for (E sel : selected) {
            destListModel.removeElement(sel);
        }
        destList.getSelectionModel().clearSelection();
    }

    private void initScreen() {
        setBorder(BorderFactory.createEtchedBorder());
        setLayout(new GridBagLayout());
        sourceLabel = new JLabel(DEFAULT_SOURCE_CHOICE_LABEL);
        sourceListModel = new SortedListModel<E>();
        sourceList = new JList<E>(sourceListModel);
        sourceList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()>=2) {
                    addSelected();
                }
            }
        });

        add(sourceLabel, new GridBagConstraints(0, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                EMPTY_INSETS, 0, 0));
        add(new JScrollPane(sourceList), new GridBagConstraints(0, 1, 1, 9, .5,
                1, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                EMPTY_INSETS, 0, 0));


        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));
        Dimension buttonsDimensions = new Dimension(50,26);

        addAllButton = new JButton(ADD_ALL_BUTTON_LABEL);
        addAllButton.addActionListener(new AddAllListener());
        addAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addAllButton.setMinimumSize(buttonsDimensions);
        addAllButton.setPreferredSize(buttonsDimensions);
        addAllButton.setMaximumSize(buttonsDimensions);
        buttonPanel.add(addAllButton);

        addButton = new JButton(ADD_BUTTON_LABEL);
        addButton.addActionListener(new AddListener());
        addButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        addButton.setMinimumSize(buttonsDimensions);
        addButton.setPreferredSize(buttonsDimensions);
        addButton.setMaximumSize(buttonsDimensions);
        buttonPanel.add(addButton);

        buttonPanel.add(new Box.Filler(new Dimension(0,20), new Dimension(0,20), new Dimension(0,20)));

        removeButton = new JButton(REMOVE_BUTTON_LABEL);
        removeButton.addActionListener(new RemoveListener());
        removeButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeButton.setMinimumSize(buttonsDimensions);
        removeButton.setPreferredSize(buttonsDimensions);
        removeButton.setMaximumSize(buttonsDimensions);
        buttonPanel.add(removeButton);

        removeAllButton = new JButton(REMOVE_ALL_BUTTON_LABEL);
        removeAllButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        removeAllButton.setMinimumSize(buttonsDimensions);
        removeAllButton.setPreferredSize(buttonsDimensions);
        removeAllButton.setMaximumSize  (buttonsDimensions);
        removeAllButton.addActionListener(new RemoveAllListener());
        buttonPanel.add(removeAllButton);

/*
        addButton = new JButton(ADD_BUTTON_LABEL);
        add(addButton, new GridBagConstraints(1, 2, 1, 1, 0, .25,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                EMPTY_INSETS, 0, 0));
        addButton.addActionListener(new AddListener());

        add(new JButton(">>"), new GridBagConstraints(1, 3, 1, 1, 0, .25,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                EMPTY_INSETS, 0, 0));


        removeButton = new JButton(REMOVE_BUTTON_LABEL);
        add(removeButton, new GridBagConstraints(1, 7, 1, 1, 0, .25,
                GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(
                0, 5, 0, 5), 0, 0));
        removeButton.addActionListener(new RemoveListener());

        add(new JButton("<<"), new GridBagConstraints(1, 8, 1, 1, 0, .25,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                EMPTY_INSETS, 0, 0));

*/
        add(buttonPanel, new GridBagConstraints(1, 2, 1, 1, 0, .25,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                EMPTY_INSETS, 0, 0));

        destLabel = new JLabel(DEFAULT_DEST_CHOICE_LABEL);
        destListModel = new SortedListModel<E>();
        destList = new JList<E>(destListModel);
        destList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount()>=2) {
                    removeSelected();
                }
            }
        });
        add(destLabel, new GridBagConstraints(2, 0, 1, 1, 0, 0,
                GridBagConstraints.CENTER, GridBagConstraints.NONE,
                EMPTY_INSETS, 0, 0));
        add(new JScrollPane(destList), new GridBagConstraints(2, 1, 1, 9, .5,
                1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                EMPTY_INSETS, 0, 0));
    }

    private void removeSelected() {
        List<E> selected = destList.getSelectedValuesList();
        addSourceElements(selected);
        clearDestinationSelected();
    }

    private void addSelected() {
        List<E> selected = sourceList.getSelectedValuesList();
        addDestinationElements(selected);
        clearSourceSelected();
    }

    private class AddAllListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            ListModel<E> model = sourceList.getModel();
            List<E> all = new ArrayList<E>(model.getSize());
            for (int i=0; i< model.getSize(); i++) {
                all.add(model.getElementAt(i));
            }
            addDestinationElements(all);
            clearSourceListModel();
        }
    }

    private class AddListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            addSelected();
        }
    }

    private class RemoveListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            removeSelected();
        }
    }

    private class RemoveAllListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            ListModel<E> model = destList.getModel();
            List<E> all = new ArrayList<E>(model.getSize());
            for (int i=0; i< model.getSize(); i++) {
                all.add(model.getElementAt(i));
            }
            addSourceElements(all);
            clearDestinationListModel();
        }
    }

    public static void main(String args[]) {
        JFrame f = new JFrame("Dual List Box Tester");
        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        DualListBox<MyNumber> dual = new DualListBox<MyNumber>();
//        dual.addSourceElements(Arrays.asList(new String[] { "One", "Two", "Three" }));
//        dual.addSourceElements(Arrays.asList(new String[] { "Four", "Five", "Six" }));
//        dual.addSourceElements(Arrays.asList(new String[] { "Seven", "Eight", "Nine" }));
//        dual.addSourceElements(Arrays.asList(new String[] { "Ten", "Eleven", "Twelve" }));
//        dual.addSourceElements(Arrays.asList(new String[] { "Thirteen", "Fourteen","Fifteen" }));
//        dual.addSourceElements(Arrays.asList(new String[] { "Sixteen", "Seventeen","Eighteen" }));
//        dual.addSourceElements(Arrays.asList(new String[] { "Nineteen", "Twenty", "Thirty" }));
//
//        dual.setDestinationElements(Arrays.asList("Nineteen", "Twenty", "Thirty"));

        dual.addSourceElements(Arrays.asList( new MyNumber("One"), new MyNumber("Two"), new MyNumber("Three") ));
        dual.addSourceElements(Arrays.asList( new MyNumber("Four"), new MyNumber("Five"), new MyNumber("Six") ));
        dual.addSourceElements(Arrays.asList( new MyNumber("Seven"), new MyNumber("Eight"), new MyNumber("Nine") ));
        dual.addSourceElements(Arrays.asList( new MyNumber("Ten"), new MyNumber("Eleven"), new MyNumber("Twelve") ));
        dual.addSourceElements(Arrays.asList( new MyNumber("Thirteen"), new MyNumber("Fourteen"),new MyNumber("Fifteen") ));
        dual.addSourceElements(Arrays.asList( new MyNumber("Sixteen"), new MyNumber("Seventeen"),new MyNumber("Eighteen") ));
        dual.addSourceElements(Arrays.asList( new MyNumber("Nineteen"), new MyNumber("Twenty"), new MyNumber("Thirty") ));

        dual.setDestinationElements(Arrays.asList(new MyNumber("Nineteen"), new MyNumber("Twenty"), new MyNumber("Thirty")));


        f.getContentPane().add(dual, BorderLayout.CENTER);
        f.setSize(400, 300);
        f.setVisible(true);
    }

    private static class MyNumber implements Comparable<MyNumber> {
        private String name;

        private MyNumber(String name) {
            this.name = name;
        }

        @Override
        public int compareTo(MyNumber o) {
            return this.name.compareTo(o.name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MyNumber myNumber = (MyNumber) o;

            if (!name.equals(myNumber.name)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
