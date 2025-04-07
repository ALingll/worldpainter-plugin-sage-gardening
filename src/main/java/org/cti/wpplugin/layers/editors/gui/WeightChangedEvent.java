package org.cti.wpplugin.layers.editors.gui;

import java.util.EventObject;

// 自定义事件
public class WeightChangedEvent extends EventObject {
    public WeightChangedEvent(Object source) {
        super(source);
    }
}
