package org.cti.wpplugin.myplants.variable;

import org.cti.wpplugin.utils.Range;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-09 04:16
 **/
public interface UiVariable<ComponentType> {
    public abstract ComponentType getComponent();
}
