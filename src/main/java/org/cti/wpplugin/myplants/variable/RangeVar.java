package org.cti.wpplugin.myplants.variable;

import org.cti.wpplugin.layers.editors.gui.RangeItem;
import org.cti.wpplugin.utils.Range;

import java.util.Random;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-09 03:30
 **/
public class RangeVar extends RandomVariable<Integer>{

    public Range range;

    public RangeVar(Range range) {
        super((range.low+range.high)/2);
        this.range = range;

    }

    @Override
    public Integer random(Random random) {
        variable = range.random(random);
        return variable;
    }
}
