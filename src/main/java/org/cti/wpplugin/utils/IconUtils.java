package org.cti.wpplugin.utils;

import org.cti.wpplugin.myplants.decoder.TemplateProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-19 01:52
 **/
public class IconUtils {
    private static final Logger logger = LoggerFactory.getLogger(IconUtils.class);

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

    public static String iconToDataURL(Icon icon) {
        BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = bi.createGraphics();
        icon.paintIcon(null, g, 0, 0);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, "png", baos);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return "";
        }
        String base64 = Base64.getEncoder().encodeToString(baos.toByteArray());

        return "data:image/png;base64," + base64;
    }


}
