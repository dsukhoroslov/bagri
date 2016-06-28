package com.bagri.tools.vvm.util;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorUtil {
    public static void showError(Container owner, Throwable th) {
        Window w = WindowUtil.getWindowForComponent(owner);
        showError(w, th);
    }

    public static void showError(Window owner, Throwable th) {
        StringWriter sw = new StringWriter();
        th.printStackTrace(new PrintWriter(sw));
        showExceptionDialog(owner, sw.toString());
    }

    private static void showExceptionDialog(Window owner, String stackTrace) {
        //Create a text area.
        JTextArea textArea = new JTextArea();
        textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane areaScrollPane = new JScrollPane(textArea);
        areaScrollPane.setPreferredSize(new Dimension(500, 300));
        areaScrollPane.setMinimumSize(new Dimension(500, 300));
        areaScrollPane.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createCompoundBorder(
                                BorderFactory.createTitledBorder("Stack trace"),
                                BorderFactory.createEmptyBorder(5,5,5,5)),
                        areaScrollPane.getBorder()));
        textArea.setText(stackTrace);
        textArea.setEditable(false);
        textArea.setCaretPosition(0);
        // pass the scrollpane to the joptionpane.
        JOptionPane.showMessageDialog(owner, areaScrollPane, "An Error Has Occurred", JOptionPane.ERROR_MESSAGE);
    }
}
