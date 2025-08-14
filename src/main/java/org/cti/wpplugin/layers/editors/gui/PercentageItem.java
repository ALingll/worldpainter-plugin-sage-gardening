package org.cti.wpplugin.layers.editors.gui;

import org.cti.wpplugin.myplants.variable.Variable;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-14 05:00
 **/
public class PercentageItem extends ValueEditor<Variable<Float>>{
    @Override
    public void reset(Variable<Float> variable) {
        //System.out.println("reset"+this.variable+"->"+variable);
        this.variable = variable;
        slider.setValue((int) (variable.getValue()*100));
    }

    private final JSlider slider;
    private final JLabel label;
    private Variable<Float> variable;

    public PercentageItem(Variable<Float> variable) {
        this.variable = variable;
        setLayout(new BorderLayout(10, 0)); // 左右布局，中间留10px间距

        // 创建滑条（这里用 0~1000 表示 1~100 的浮点值，方便精度到小数点后1位）
        slider = new JSlider(100, 10000, (int) (variable.getValue()*100)); // 默认值 50.0%
        slider.setMajorTickSpacing(1000); // 每 10.0% 一个大刻度
        slider.setPaintTicks(true);

        // 创建百分比显示标签
        label = new JLabel();
        label.setPreferredSize(new Dimension(60, 20));
        label.setHorizontalAlignment(SwingConstants.RIGHT);

        // 滑条变化时更新标签
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                updateLabel();
                fireValueChanged(variable);
            }
        });

        updateLabel(); // 初始化一次

        add(slider, BorderLayout.CENTER);
        add(label, BorderLayout.EAST);
    }

    private void updateLabel() {
        variable.setValue(slider.getValue() / 100.0f); // 转成浮点百分比
        label.setText(String.format("%.1f%%", variable.getValue()));
    }

    // 获取当前百分比值（1.0 ~ 100.0）
    public float getPercentage() {
        return slider.getValue() / 100.0f;
    }

    public Variable<Float> getVar(){return variable;}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("百分比控件示例");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(300, 100);
            frame.setLayout(new FlowLayout());

            PercentageItem pc = new PercentageItem(new Variable<>(15.25f));
            frame.add(pc);

            frame.setVisible(true);
        });
    }
}
