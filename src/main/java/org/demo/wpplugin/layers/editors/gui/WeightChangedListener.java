package org.demo.wpplugin.layers.editors.gui;

import java.util.EventListener;

// 自定义监听器接口
public interface WeightChangedListener extends EventListener {
    void wightChanged(WeightChangedEvent event);
}
