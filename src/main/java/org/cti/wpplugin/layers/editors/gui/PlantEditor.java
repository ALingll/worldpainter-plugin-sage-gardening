package org.cti.wpplugin.layers.editors.gui;

import org.cti.wpplugin.layers.GardeningLayer;
import org.cti.wpplugin.minecraft.IconLoader;
import org.cti.wpplugin.myplants.CustomPlant;
import org.cti.wpplugin.myplants.variable.UiVariable;
import org.cti.wpplugin.utils.Pair;
import org.cti.wpplugin.utils.macro.StringMacroProvider;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.cti.wpplugin.utils.IconUtils.resizeIcon;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-11 03:25
 **/
public class PlantEditor extends ValueEditor<CustomPlant> {
    private final String id;
    private final String name;
    private final JButton toggleButton;
    private final JLabel iconLabel;
    private final WeightItem weightItem;
    private final JPanel extraComponentsPanel;
    private boolean expanded = false;
    private final Map<String, ValueEditor> extraComponents = new HashMap<>();
    private final CustomPlant refPlant;
    private JToolTip tip;

    public PlantEditor(String id, String name, CustomPlant plant) {
        this.id = id;
        this.name = name;
        this.refPlant = plant;

        setToolTipText(" ");

        WeightItem weightItem = new WeightItem(id,name);
        weightItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        this.weightItem = weightItem;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        // 第一排
        JPanel firstRow = new JPanel(new BorderLayout(5, 5));
        firstRow.setOpaque(false);
        firstRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // 按钮 A（箭头）
        toggleButton = new JButton(""); // 初始折叠箭头
        toggleButton.setPreferredSize(new Dimension(30, 30));
        toggleButton.setFont(new Font("Dialog", Font.PLAIN, 16));
        toggleButton.setMargin(new Insets(2, 4, 2, 4));
        toggleButton.addActionListener(this::toggleExpanded);

        toggleButton.setEnabled(false);           // 禁用按钮（不能点击）
        toggleButton.setOpaque(false);            // 不绘制背景
        toggleButton.setContentAreaFilled(false); // 不绘制内容区域
        toggleButton.setBorderPainted(false);     // 不绘制边框


        // 图标（如果 icon 为 null，则放空白占位）
        //TODO
        Icon icon= plant.getIcon();
        if (icon != null) {
            iconLabel = new JLabel(icon);
        } else {
            iconLabel = new JLabel();
            iconLabel.setPreferredSize(new Dimension(24, 24)); // 占位大小
        }

        JLabel warningLabel;
        if (plant.getState().isIllegal) {
            warningLabel = new JLabel(resizeIcon(UIManager.getIcon("OptionPane.warningIcon"),20,20));
            warningLabel.setToolTipText("<html><div style='width:400px;'>" + StringMacroProvider.INSTANCE.get("warning.illegal") + "</div></html>");
        } else {
            warningLabel = new JLabel();
            warningLabel.setPreferredSize(new Dimension(20, 20)); // 占位大小
        }
        // 第一排布局
        JPanel leftPart = new JPanel();
        leftPart.setOpaque(false);
        leftPart.setLayout(new BoxLayout(leftPart, BoxLayout.X_AXIS));
        leftPart.add(toggleButton);
        leftPart.add(Box.createHorizontalStrut(5));
        leftPart.add(iconLabel);
        leftPart.add(Box.createHorizontalStrut(5));
        leftPart.add(weightItem);
        leftPart.add(Box.createHorizontalStrut(5));
        leftPart.add(warningLabel);

        firstRow.add(leftPart, BorderLayout.WEST);
        firstRow.setAlignmentX(LEFT_ALIGNMENT);
        add(firstRow);

        // 拓展组件区域
        extraComponentsPanel = new JPanel();
        Border outerMargin = BorderFactory.createEmptyBorder(5, 5, 5, 5);// 边框
        Border line = BorderFactory.createLineBorder(Color.GRAY, 1, true);// 内边距（内容与边框之间的间隔）
        Border innerPadding = BorderFactory.createEmptyBorder(5, 5, 5, 5);// 顺序：外边距 在最外层 → 边框 → 内边距
        extraComponentsPanel.setBorder(
                BorderFactory.createCompoundBorder(
                        outerMargin, // 外层
                        BorderFactory.createCompoundBorder(
                                line,          // 中间
                                innerPadding   // 最里层
                        )
                )
        );
        extraComponentsPanel.setLayout(new BoxLayout(extraComponentsPanel, BoxLayout.Y_AXIS));
        extraComponentsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // 让面板占满整行
        extraComponentsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        extraComponentsPanel.setVisible(expanded); // 初始隐藏
        extraComponentsPanel.setOpaque(false);
        add(extraComponentsPanel);

        JPanel panel = new JPanel();
        // 在构造函数里设置边框
        Border margin1 = new EmptyBorder(5, 5, 5, 5);         // 内边距，留点空白
        Border line1 = new LineBorder(Color.GRAY, 1, true);   // 灰色圆角线边框
        panel.setBorder(new CompoundBorder(line1, margin1));

        //extraComponentsPanel.add(panel);
        plant.getAllUiVar().forEach(
                entry -> {
                    String varId = entry.getKey();
                    UiVariable variable = entry.getValue();
                    addExtraComponent(varId, variable.getComponent(), variable.getDesc());
                }
        );
    }

    private void toggleExpanded(ActionEvent e) {
        expanded = !expanded;
        toggleButton.setText(expanded ? "▼" : "▶");
        extraComponentsPanel.setVisible(expanded);
        extraComponentsPanel.setOpaque(true);
        revalidate();
        repaint();
    }

    public void set(GardeningLayer.PlantSetting setting){
        setWeight(setting.weight);
        setting.uiProperties.forEach((key,value)->{
            extraComponents.get(key).reset(value);
            System.out.println(key+"\t"+value+"\t"+extraComponents.get(key).getClass());
        });
    }

    @Override
    public JToolTip createToolTip() {
        JToolTip tip = new JToolTip() {
            @Override
            public Dimension getPreferredSize() {
                int width = 0, height = 0;
                for (Component c : getComponents()) {
                    Dimension d = c.getPreferredSize();
                    width = Math.max(width, d.width);
                    height += d.height;
                }
                return new Dimension(width + 10, height + 10); // 留一点边距
            }
        };
        tip.setLayout(new BoxLayout(tip, BoxLayout.Y_AXIS));

        String name = refPlant.getName();
        String desc = refPlant.getDescription();

        if (name != null && !name.isEmpty()) tip.add(new JLabel(name));
        if (desc != null && !desc.isEmpty()) tip.add(new JLabel(desc));

        Icon icon = refPlant.getIcon();
        if(icon!=null){
            icon = resizeIcon(icon,80,80);
            tip.add(new JLabel(icon));
        }
        return tip;
    }

    public String getId() {return id;}

    public String getName() {return name;}

    public void addWeightChangedListener(WeightChangedListener listener) {
        weightItem.addWeightChangedListener(listener);
    }

    // 注销监听器
    public void removeWeightChangedListener(WeightChangedListener listener) {
        weightItem.removeWeightChangedListener(listener);
    }

    public void addExtraComponent(String id, ValueEditor comp, String desc) {
        comp.addValueChangeListener(e->{
            fireValueChanged(Pair.makePair(id,e.getNewValue()));
        });

        extraComponents.put(id,comp);

        JPanel row = new JPanel(new BorderLayout(5, 5));

        JLabel idLabel = new JLabel(id);
        idLabel.setToolTipText(desc);

        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.add(idLabel,BorderLayout.WEST);
        row.add(comp,BorderLayout.CENTER);
        row.setAlignmentX(LEFT_ALIGNMENT);

        toggleButton.setText("▶");
        toggleButton.setEnabled(true);
        toggleButton.setForeground(Color.BLACK);  // 或原来的颜色

        row.setAlignmentX(Component.LEFT_ALIGNMENT); // 保证宽度对齐

        // 限制最大高度 = 首选高度，防止被 BoxLayout 拉伸
        Dimension pref = comp.getPreferredSize();
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, pref.height));

        extraComponentsPanel.add(row);

        extraComponentsPanel.revalidate();
        extraComponentsPanel.repaint();
        extraComponentsPanel.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, extraComponentsPanel.getPreferredSize().height)
        );
    }

    public int getWeight() {return weightItem.getValue();}

    public void setWeight(int i) {weightItem.setValue(i);}

    public void setPercentage(float i) {
        weightItem.setPercentage(i);
    }

    // 简单测试
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("PlantEditor Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));


            frame.pack();
            frame.setSize(400, 300);
            frame.setVisible(true);
        });
    }

    @Override
    public void reset(CustomPlant plant) {
        throw new UnsupportedOperationException("Unsupported Op");
    }

    @Override
    public String toString(){
        return "PlantEditor"+extraComponents;
    }
}

