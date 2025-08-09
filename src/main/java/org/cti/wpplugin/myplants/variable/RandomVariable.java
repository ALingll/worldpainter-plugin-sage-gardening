package org.cti.wpplugin.myplants.variable;

import java.util.Random;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-09 04:53
 **/
public abstract class RandomVariable<V> extends Variable<V> {
    public RandomVariable(V v) {
        super(v);
    }

    public abstract V random(Random random);
}
