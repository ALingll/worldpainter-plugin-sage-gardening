package org.cti.wpplugin.utils;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-14 18:50
 **/
public class FormatString {
    public static Range asRange(String text){
        int a, b;
        text = text.trim();
        if (text.contains("~")) {
            String[] parts = text.split("~");
            if (parts.length != 2) {
                throw new IllegalArgumentException("FormatString error: Invalid range format: " + text);
            }
            a = Integer.parseInt(parts[0].trim());
            b = Integer.parseInt(parts[1].trim());
        } else {
            try {
                a = b = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("FormatString error: Invalid range format: " + text);
            }
        }
        return new Range(a,b);
    }

    public static float asPercent(String str) {
        if (str == null) {
            throw new IllegalArgumentException("Percent string cannot be null");
        }

        str = str.trim();

        // Case 1: ends with %
        if (str.matches("^\\d+(\\.\\d+)?%$")) {
            String numberPart = str.substring(0, str.length() - 1);
            return Float.parseFloat(numberPart);
        }

        // Case 2: plain number
        if (str.matches("^\\d+(\\.\\d+)?$")) {
            return Float.parseFloat(str) * 100;
        }

        throw new IllegalArgumentException("Invalid percent format: " + str);
    }


}
