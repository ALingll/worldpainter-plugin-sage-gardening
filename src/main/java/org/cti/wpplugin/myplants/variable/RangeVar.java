package org.cti.wpplugin.myplants.variable;

import org.cti.wpplugin.layers.editors.gui.RangeItem;
import org.cti.wpplugin.utils.Range;

import java.util.Random;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-09 03:30
 **/
public class RangeVar extends RandomVariable<Integer> implements UiVariable<RangeItem>{
    public int max;
    public int min;
    public Range range;

    public RangeVar(Range range, int max, int min) {
        super((max+min)/2);
        this.range = range;
        this.max = max;
        this.min = min;
    }

    @Override
    public RangeItem getComponent() {
        return new RangeItem(range,max,min);
    }

    @Override
    public Integer random(Random random) {
        return null;
    }
}
