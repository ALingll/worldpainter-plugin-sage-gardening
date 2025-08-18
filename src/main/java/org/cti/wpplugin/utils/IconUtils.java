package org.cti.wpplugin.utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-19 01:52
 **/
public class IconUtils {

    public static ImageIcon resizeIcon(Icon icon, int width, int height) {
        // 把 Icon 画到 BufferedImage 上
        BufferedImage img = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        icon.paintIcon(null, g2, 0, 0);
        g2.dispose();

        // 缩放
        Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }


}
