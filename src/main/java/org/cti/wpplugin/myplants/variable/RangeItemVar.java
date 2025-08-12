package org.cti.wpplugin.myplants.variable;

import org.cti.wpplugin.layers.editors.gui.RangeItem;
import org.cti.wpplugin.layers.editors.gui.ValueEditor;
import org.cti.wpplugin.utils.Range;

import javax.swing.*;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-11 03:56
 **/
public class RangeItemVar extends RangeVar implements UiVariable{
    int min;
    int max;

    public RangeItemVar(Range range, int max, int min) {
        super(range);
        this.max=max;
        this.min=min;
    }

    @Override
    public ValueEditor<Range> getComponent() {
        return new RangeItem(range,max,min);
    }
}
