package org.cti.wpplugin.myplants.decoder;

import org.pepsoft.minecraft.Material;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-24 05:00
 **/
public class MaterialDecoder {
    public static Material getMaterial(String input) {
        if (!input.contains("["))
            return Material.get(input);
        // 正则匹配：name[...]
        String regex = "([a-zA-Z_:][a-zA-Z0-9_:]*)\\[(.*?)\\]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input.trim());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("String format error: " + input);
        }

        String name = matcher.group(1);
        String paramPart = matcher.group(2).trim();

        Map<String, String> map = new LinkedHashMap<>();
        if (!paramPart.isEmpty()) {
            // 按逗号拆分参数
            String[] pairs = paramPart.split(",");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2); // 只分一次，避免 value 中有 '='
                if (kv.length == 2) {
                    map.put(kv[0].trim(), kv[1].trim());
                } else {
                    throw new IllegalArgumentException("Properties error: " + pair);
                }
            }
        }

        return Material.get(name,map);
    }
}
