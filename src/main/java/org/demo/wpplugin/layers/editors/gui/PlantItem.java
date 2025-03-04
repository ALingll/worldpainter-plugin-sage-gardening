package org.demo.wpplugin.layers.editors.gui;

import org.demo.wpplugin.myplants.PlantElement;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.EventObject;
import java.util.List;

public class PlantItem extends JPanel {
    private String id = null;
    // 定义组件字段
    private JLabel nameLabel;
    private JSpinner spinner;
    private JLabel numberLabel;
    private List<WeightChangedListener> listeners = new ArrayList<>();

    public PlantItem(String id, String labelText) {
        this.id = id;
        // 设置布局为FlowLayout，使子组件横向排列
        setLayout(new FlowLayout(FlowLayout.LEFT));

        // 初始化组件并添加到面板
        nameLabel = new JLabel(labelText);
        add(nameLabel);

        // 初始化JSpinner，设置数字范围和步长
        SpinnerNumberModel model = new SpinnerNumberModel(0, 0, 100, 1); // 默认值为0，最小值0，最大值100，步长1
        spinner = new JSpinner(model);
        add(spinner);

        // 初始化显示数字的标签
        numberLabel = new JLabel("0");
        add(numberLabel);

        // 监听JSpinner值变化，更新数字标签
        spinner.addChangeListener(e -> numberLabel.setText(String.valueOf(spinner.getValue())));
        spinner.addChangeListener(e -> this.fireWeightChangedEvent());
    }

    // 获取JSpinner的值（可以根据需要添加更多的方法）
    public int getValue() {
        return (int) spinner.getValue();
    }

    public PlantItem setValue(int value){
        spinner.setValue(value);
        return this;
    }

    public String getName() {
        return nameLabel.getText();
    }
    public String getId() {
        return id;
    }

    // 注册监听器
    public void addWeightChangedListener(WeightChangedListener listener) {
        listeners.add(listener);
    }

    // 注销监听器
    public void removeWeightChangedListener(WeightChangedListener listener) {
        listeners.remove(listener);
    }

    // 触发事件
    private void fireWeightChangedEvent() {
        WeightChangedEvent event = new WeightChangedEvent(this);
        for (WeightChangedListener listener : listeners) {
            listener.wightChanged(event);
        }
    }

    public static void main(String[] args) {
        // 创建 JFrame 并设置其内容面板为自定义的 CustomPanel
        JFrame frame = new JFrame("Custom Panel Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 100);
        frame.setLayout(new BorderLayout());

        JLabel jLabel = new JLabel("aaa");
        frame.add(jLabel, BorderLayout.NORTH);

        PlantItem panel = new PlantItem("Item:","bbb");
        panel.addWeightChangedListener(e->{
            jLabel.setText(String.valueOf(((PlantItem)e.getSource()).getValue()));
        });
        frame.add(panel, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}


