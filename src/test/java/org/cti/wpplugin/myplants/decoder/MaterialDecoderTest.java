package org.cti.wpplugin.myplants.decoder;

import org.junit.jupiter.api.Test;

import static org.cti.wpplugin.myplants.decoder.MaterialDecoder.getMaterial;
import static org.junit.jupiter.api.Assertions.*;

class MaterialDecoderTest {

    @Test
    void test_parse() {
        System.out.println(getMaterial("minecraft:sunflower[half=lower]"));
    }
}