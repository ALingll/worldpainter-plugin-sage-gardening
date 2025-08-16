package org.cti.wpplugin.utils;

import org.junit.jupiter.api.Test;

import static org.cti.wpplugin.utils.debug.DebugUtils.classStr;

class RangeTest {

    @Test
    void forEach() {
        Integer i =5;
        System.out.println(classStr(i));
        new Range(5,10).forEach(System.out::println);
    }
}