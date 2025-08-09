package org.cti.wpplugin.layers.editors.gui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class WeightItem extends JPanel {
    private String id = null;
    // 定义组件字段
    private JLabel nameLabel;
    private JSpinner spinner;
    private JLabel numberLabel;
    private List<WeightChangedListener> listeners = new ArrayList<>();

    public WeightItem(String id, String labelText) {
        this.id = id;
        setOpaque(false); // 透明背景，防止影响布局

        nameLabel = new JLabel(labelText);
        spinner = new JSpinner(new SpinnerNumberModel(0, 0, 100, 1));
        numberLabel = new JLabel("0.00 %");
        numberLabel.setPreferredSize(new Dimension(80, 25));
        numberLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        // 设置 Spinner 的大小
        spinner.setPreferredSize(new Dimension(80, 25));

        // 监听 JSpinner 值变化
        spinner.addChangeListener(e -> this.fireWeightChangedEvent());

        // **使用 GroupLayout 进行布局**
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);

        // 禁用默认间隙，避免额外的空白
        layout.setAutoCreateGaps(false);
        layout.setAutoCreateContainerGaps(false);

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                        .addComponent(nameLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE) // 弹性空间
                        .addComponent(spinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(numberLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );

        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                        .addComponent(nameLabel)
                        .addComponent(spinner)
                        .addComponent(numberLabel)
        );
    }

    // 获取JSpinner的值（可以根据需要添加更多的方法）
    public int getValue() {
        return (int) spinner.getValue();
    }

    public WeightItem setValue(int value){
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

    public void setPercentage(float i){numberLabel.setText(String.format("%.2f",i)+" %");}

    @Override
    public String toString(){
        return getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    public static void main(String[] args) {
        // 创建 JFrame 并设置其内容面板为自定义的 CustomPanel
        JFrame frame = new JFrame("Custom Panel Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 100);
        frame.setLayout(new BorderLayout());

        JLabel jLabel = new JLabel("aaa");
        frame.add(jLabel, BorderLayout.NORTH);

        WeightItem panel = new WeightItem("Item:","bbb");
        panel.addWeightChangedListener(e->{
            jLabel.setText(String.valueOf(((WeightItem)e.getSource()).getValue()));
        });
        frame.add(panel, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}


