package com.bagri.tools.vvm.util;

import javax.swing.*;
import java.awt.*;

public class WindowUtil {

    public static Window getWindowForComponent(Component parentComponent) throws HeadlessException {
        if (parentComponent == null)
            return getRootFrame();
        if (parentComponent instanceof Frame || parentComponent instanceof Dialog)
            return (Window)parentComponent;
        return getWindowForComponent(parentComponent.getParent());
    }

    public static Frame getFrameForComponent(Component parentComponent) throws HeadlessException {
        return JOptionPane.getFrameForComponent(parentComponent);
    }

    public static Frame getRootFrame() throws HeadlessException {
        return JOptionPane.getRootFrame();
    }
}
