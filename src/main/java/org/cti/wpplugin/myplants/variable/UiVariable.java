package org.cti.wpplugin.myplants.variable;

import org.cti.wpplugin.layers.editors.gui.ValueEditor;
import org.cti.wpplugin.utils.Range;

import javax.swing.*;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-09 04:16
 **/
public interface UiVariable {
    public abstract ValueEditor getComponent();
    public void setDesc(String s);
    public String getDesc();
    public Object copyFrom(Object variable);
}
