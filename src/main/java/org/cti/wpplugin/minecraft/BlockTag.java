package org.cti.wpplugin.minecraft;

import org.pepsoft.minecraft.Material;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-03-10 16:49
 **/
public class BlockTag extends ArrayList<Material> {
    public BlockTag(String... args) {
        Arrays.stream(args).forEach(value->add(Material.get(value)));
    }
}
