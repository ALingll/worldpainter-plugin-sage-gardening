package org.cti.wpplugin.minecraft;

import org.cti.wpplugin.minecraft.BlockTag;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-03-10 17:00
 **/
public class BlockTags {
    public static Map<String, BlockTag> blockTagMap = new HashMap<>();
    public static BlockTag named(String s) {return blockTagMap.get(s);}
    public static final BlockTag DIRT = new BlockTag(
            "minecraft:coarse",
            "minecraft:dirt",
            "minecraft:grass_block",
            "minecraft:moss_block",
            "minecraft:mud",
            "minecraft:muddy_mangrove_roots",
            "minecraft:mycelium",
            "minecraft:pale_moss_bloc",
            "minecraft:podzol",
            "minecraft:rooted_dirt"
    );

    public static final BlockTag SAND = new BlockTag(
            "minecraft:sand",
            "minecraft:red_sand",
            "minecraft:suspicious_sand"
    );


    static {
        blockTagMap.put("dirt",DIRT);
        blockTagMap.put("sand",SAND);
    }
}
