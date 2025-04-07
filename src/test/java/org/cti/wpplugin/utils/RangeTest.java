package org.cti.wpplugin.utils;

import org.junit.jupiter.api.Test;

class RangeTest {

    @Test
    void forEach() {
        new Range(5,10).forEach(System.out::println);
    }
}