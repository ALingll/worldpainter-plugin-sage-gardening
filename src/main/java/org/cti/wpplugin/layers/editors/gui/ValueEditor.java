package org.cti.wpplugin.layers.editors.gui;

import javax.swing.*;
import javax.swing.event.EventListenerList;
import java.util.EventListener;
import java.util.EventObject;

/**
 * 公共基类：带 valueChanged 事件的自定义组件
 */
public abstract class ValueEditor<T> extends JPanel {

    /** 监听器列表 */
    private final EventListenerList listenerList = new EventListenerList();

    /**
     * 添加 valueChanged 监听器
     */
    public void addValueChangeListener(ValueChangeListener listener) {
        listenerList.add(ValueChangeListener.class, listener);
    }

    /**
     * 移除 valueChanged 监听器
     */
    public void removeValueChangeListener(ValueChangeListener listener) {
        listenerList.remove(ValueChangeListener.class, listener);
    }

    /**
     * 通知所有监听器：值发生变化
     */
    protected void fireValueChanged(Object newValue) {
        ValueChangeEvent event = new ValueChangeEvent(this, newValue);
        for (ValueChangeListener listener : listenerList.getListeners(ValueChangeListener.class)) {
            listener.valueChanged(event);
        }
    }

    public abstract void reset(T t);

    /**
     * 事件对象
     */
    public static class ValueChangeEvent extends EventObject {
        private final Object newValue;

        public ValueChangeEvent(Object source, Object newValue) {
            super(source);
            this.newValue = newValue;
        }

        public Object getNewValue() {
            return newValue;
        }
    }

    /**
     * 事件监听接口
     */
    public interface ValueChangeListener extends EventListener {
        void valueChanged(ValueChangeEvent e);
    }

    @Override
    public String toString(){
        return this.getClass().getName();
    }
}
