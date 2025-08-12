package org.cti.wpplugin.utils;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class Range {
    public int low;
    public int high;

    public Range(int low, int high) {
        this.low = low;
        this.high = high;
    }

    public int random(Random a_random){
        System.out.println("Random "+this);
        return low==high ? low : a_random.nextInt(high-low+1)+low;
    }

    public void forEach(Consumer<Integer> consumer){
        IntConsumer intConsumer = consumer::accept;
        IntStream.range(low,high).forEach(intConsumer);
    }

    public static Range asRange(String text){
        int a, b;
        text = text.trim();
        if (text.contains("~")) {
            String[] parts = text.split("~");
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid range format: " + text);
            }
            a = Integer.parseInt(parts[0].trim());
            b = Integer.parseInt(parts[1].trim());
        } else {
            try {
                a = b = Integer.parseInt(text);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid range format: " + text);
            }
        }
        return new Range(a,b);
    }

    public static Range range(int low, int high){return new Range(low,high);}

    @Override
    public String toString(){
        return low+"~"+high+"@"+Integer.toHexString(System.identityHashCode(this));
    }
}
