package org.cti.wpplugin.layers.editors.gui;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-08 04:37
 **/
public enum UiType {
    RANGE_UI {
        public RangeItem getCompenent(Object object){
            return (RangeItem) object;
        }
    }
}
