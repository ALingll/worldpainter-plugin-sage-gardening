package org.demo.wpplugin.utils;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class Range {
    int low;
    int high;

    public Range(int low, int high) {
        this.low = low;
        this.high = high;
    }

    public int random(Random a_random){
        return a_random.nextInt(high-low)+low;
    }

    public void forEach(Consumer<Integer> consumer){
        IntConsumer intConsumer = consumer::accept;
        IntStream.range(low,high).forEach(intConsumer);
    }

    public static Range range(int low, int high){return new Range(low,high);}
}
