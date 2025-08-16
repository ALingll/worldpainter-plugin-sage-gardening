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
    String desc = null;

    public RangeItemVar(Range range, int max, int min){
        this(range, max, min, null);
    }
    public RangeItemVar(Range range, int max, int min, String desc) {
        super(range);
        this.max=max;
        this.min=min;
        this.desc = desc;
    }

    @Override
    public ValueEditor<Range> getComponent() {
        return new RangeItem(range,max,min);
    }

    @Override
    public void setDesc(String s) {
        this.desc = s;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public Object copyFrom(Object variable) {
        Range rangeVar = (Range) variable;
        this.range.low=rangeVar.low;
        this.range.high=rangeVar.high;
        return this.range;
    }

    public String toString(){
        return variable+"@"+Integer.toHexString(System.identityHashCode(this))+"{"
                +range
                +"}";
    }

}
