package org.cti.wpplugin.utils.macro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-18 18:10
 **/
public class StringMacroProvider {
    public static StringMacroProvider INSTANCE = new StringMacroProvider();
    private StringMacroProvider() {
        macros.put("warning.illegal","This entry is or contains an illegal block combination pattern. It cannot be naturally generated/exist in the world. When you use this pattern, you should turn off block updates/schedule ticks when entering game!");
        macros.put("test1","aaa");
        macros.put("test2","bbb");
    }

    private Map<String,String> macros = new HashMap<>();

    public String expand(String patternStr){
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\{([^}]*)\\}"); // 匹配 { ... }
        Matcher matcher = pattern.matcher(patternStr);
        while (matcher.find()) {
            result.add(matcher.group(1)); // 取花括号里面的内容
        }
        AtomicReference<String> resultStr = new AtomicReference<>(patternStr);
        result.forEach(m->{
            resultStr.set(resultStr.get().replace("{" + m + "}", macros.get(m)));
        });
        return resultStr.get();
    }

    public String get(String s){return macros.get(s);}
}
