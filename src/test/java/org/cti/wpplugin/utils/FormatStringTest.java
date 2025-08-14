package org.cti.wpplugin.utils;

import org.junit.jupiter.api.Test;

import static org.cti.wpplugin.utils.FormatString.asPercent;
import static org.junit.jupiter.api.Assertions.*;

class FormatStringTest {

    @Test
    void asPercentTest() {
        System.out.println(asPercent("50%"));    // 50.0
        System.out.println(asPercent("12.5%"));  // 12.5
        System.out.println(asPercent("0%"));     // 0.0
        System.out.println(asPercent("100%"));   // 100.0
        System.out.println(asPercent("0.5"));   // 50.0
        System.out.println(asPercent("1"));     // 100.0
        System.out.println(asPercent("0"));     // 0.0
        try{
            System.out.println(asPercent("50 %"));
        }catch (IllegalArgumentException e){
            System.out.println(e.toString());
        }
        try{
            System.out.println(asPercent("50％"));
        }catch (IllegalArgumentException e){
            System.out.println(e.toString());
        }

    }
}