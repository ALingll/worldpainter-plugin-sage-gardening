package org.cti.wpplugin.minecraft;

import org.junit.jupiter.api.Test;

import javax.swing.*;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

class IconLoaderTest {

    @Test
    public void test(){
        SwingUtilities.invokeLater(() -> {
            Icon icon = IconLoader.getInstance().getIcon("verdantvibes/textures/block/monstera.png");

            JFrame frame = new JFrame("Icon Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(300, 300);

            frame.add(new JLabel(icon != null ? icon : new ImageIcon(), JLabel.CENTER));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });

        // ⚠️ 阻塞住测试线程，否则JUnit会立刻结束
        try {
            Thread.sleep(60_000); // 挂住1分钟，够你看了
        } catch (InterruptedException ignored) {}
    }
}
