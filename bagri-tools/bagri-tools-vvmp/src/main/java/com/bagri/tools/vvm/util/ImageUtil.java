package com.bagri.tools.vvm.util;

import javax.swing.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ImageUtil {
    private static final Logger LOGGER = Logger.getLogger(ImageUtil.class.getName());

    /** Returns an ImageIcon, or null if the path was invalid. */
    public static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = ImageUtil.class.getClassLoader().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            LOGGER.log(Level.WARNING, "Couldn't find file: " + path);
            return null;
        }
    }

}
