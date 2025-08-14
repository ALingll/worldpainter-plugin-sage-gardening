package org.cti.wpplugin.myplants.variable;

import java.io.Serializable;
import java.util.Random;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-09 03:27
 **/
public class Variable<V> implements Serializable {
    protected V variable;
    public Variable(V v) {this.variable=v;}
    public final V getValue() {return variable;}
    public final void setValue(V v) {this.variable=v;}
    @Override
    public String toString(){return variable+"@"+Integer.toHexString(System.identityHashCode(this));}
}
