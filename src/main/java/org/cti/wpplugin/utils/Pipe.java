package org.cti.wpplugin.utils;

import java.util.function.Function;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-03-09 18:38
 **/
public class Pipe {
    @SuppressWarnings("unchecked")
    public static <T, R> R pipe(T value, Function<T, R>... functions) {
        for (Function<T, R> function : functions) {
            value = (T) function.apply(value);
        }
        return (R) value;
    }
}
