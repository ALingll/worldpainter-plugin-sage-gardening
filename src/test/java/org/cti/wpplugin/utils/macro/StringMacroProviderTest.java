package org.cti.wpplugin.utils.macro;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringMacroProviderTest {

    @Test
    void expand() {
        String s = "{test1},{test2}";
        System.out.println(StringMacroProvider.INSTANCE.expand(s));
    }
}