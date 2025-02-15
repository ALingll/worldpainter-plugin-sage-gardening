package org.demo.wpplugin.layers.editors;

import org.demo.wpplugin.layers.DemoCustomLayer;
import org.demo.wpplugin.layers.DemoCustomLayerSettings;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.layers.AbstractLayerEditor;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class DemoCustomLayerEditor extends AbstractLayerEditor<DemoCustomLayer> {
    public DemoCustomLayerEditor(){
        initGUI();
        platform = context.getDimension().getWorld().getPlatform();
    }

    private void initGUI() {
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
        // 创建组件
        JLabel label = new JLabel("Hello, World!");
        // 将组件添加到面板
        // 设置水平布局
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.CENTER) // 使用居中对齐
                        .addComponent(label) // 添加 JLabel 组件
        );
        // 设置垂直布局
        layout.setVerticalGroup(
                layout.createSequentialGroup() // 顺序排列组件
                        .addComponent(label) // 添加 JLabel 组件
        );
    }

    public DemoCustomLayerEditor(Platform platform) {
        initGUI();
        this.platform = platform;
    }

    @Override
    public DemoCustomLayer createLayer() {
        return new DemoCustomLayer();
    }

    @Override
    public void commit() {
        // Write the configuration currently selected by the user to the layer
    }

    @Override
    public void reset() {
        // Reset the UI to the values currently in the layer
    }

    @Override
    public ExporterSettings getSettings() {
        return new DemoCustomLayerSettings(layer);
    }

    @Override
    public boolean isCommitAvailable() {
        // Check whether the configuration currently selected by the user is valid and could be written to the layer
        return true;
    }
    private final Platform platform;
}