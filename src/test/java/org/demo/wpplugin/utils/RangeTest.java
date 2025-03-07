package org.demo.wpplugin.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RangeTest {

    @Test
    void forEach() {
        new Range(5,10).forEach(System.out::println);
    }
}