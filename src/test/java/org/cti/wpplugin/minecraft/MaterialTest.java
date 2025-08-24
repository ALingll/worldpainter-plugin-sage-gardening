package org.cti.wpplugin.minecraft;

import org.junit.jupiter.api.Test;
import org.pepsoft.minecraft.Material;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-24 04:32
 **/
class MaterialTest {
    @Test
    void testMaterial(){
        System.out.println(Material.get("minecraft:sunflower[half=lower]"));
    }
}
