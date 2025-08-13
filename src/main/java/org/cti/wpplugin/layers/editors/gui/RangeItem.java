package org.cti.wpplugin.layers.editors.gui;

import javax.swing.*;
import java.awt.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.cti.wpplugin.utils.Range;

public class RangeItem extends ValueEditor<Range> {
    @Override
    public void reset(Range range) {
        //System.out.println("reset "+this.range+"->"+range);
        highSpinner.setValue(range.high);
        lowSpinner.setValue(range.low);
        this.range = range;
        //System.out.println("reset2 "+this.range+"->"+range);
    }
    private final JSpinner lowSpinner;

    private final JSpinner highSpinner;

    private final int minLimit;
    private final int maxLimit;

    private Range range;

    public RangeItem(Range r, int maxLimit, int minLimit) {
        this.minLimit = minLimit;
        this.maxLimit = maxLimit;
        this.range = r;

        setLayout(new FlowLayout(FlowLayout.LEFT));

        lowSpinner = new JSpinner(new SpinnerNumberModel(range.low, minLimit, maxLimit, 1));
        highSpinner = new JSpinner(new SpinnerNumberModel(range.high, minLimit, maxLimit, 1));

        Dimension size = new Dimension(60, 25);
        lowSpinner.setPreferredSize(size);
        highSpinner.setPreferredSize(size);

        add(lowSpinner);
        add(new JLabel(" ~ "));
        add(highSpinner);

        // 同步高值 ≥ 低值，低值 ≤ 高值
        lowSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int low = (Integer) lowSpinner.getValue();
                int high = (Integer) highSpinner.getValue();
                if (high < low) {
                    highSpinner.setValue(low); // 同步
                }
                range.low=(Integer) lowSpinner.getValue();
                range.high=(Integer) highSpinner.getValue();
                fireValueChanged(range);
            }
        });

        highSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int low = (Integer) lowSpinner.getValue();
                int high = (Integer) highSpinner.getValue();
                if (high < low) {
                    lowSpinner.setValue(high); // 同步
                }
                range.low=(Integer) lowSpinner.getValue();
                range.high=(Integer) highSpinner.getValue();
                fireValueChanged(range);
            }
        });
    }

    public Range getRange() {
        if(range==null)
            return new Range((Integer) lowSpinner.getValue(),(Integer) highSpinner.getValue());
        range.low = (Integer) lowSpinner.getValue();
        range.high = (Integer) highSpinner.getValue();
        return range;
    }

    /** 设置当前值 */
    public void setRange(Range range) {
        this.range = range;
        lowSpinner.setValue(range.low);
        highSpinner.setValue(range.high);
    }

    @Override
    public String toString(){
        return this.getClass().getName()+"{"+range+"}";
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("PlantEditor Demo");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));

            frame.add(new RangeItem(new Range(2,10),15,1));





            frame.pack();
            frame.setSize(400, 300);
            frame.setVisible(true);
        });
    }
}
