package org.cti.wpplugin.utils;

import java.io.Serializable;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class Range implements Serializable {
    public int low;
    public int high;

    public Range(int low, int high) {
        this.low = low;
        this.high = high;
    }

    public int random(Random a_random){
        //System.out.println("Random "+this);
        return low==high ? low : a_random.nextInt(high-low+1)+low;
    }

    public void forEach(Consumer<Integer> consumer){
        IntConsumer intConsumer = consumer::accept;
        IntStream.range(low,high).forEach(intConsumer);
    }

    public static Range range(int low, int high){return new Range(low,high);}

    @Override
    public String toString(){
        return low+"~"+high+"@"+Integer.toHexString(System.identityHashCode(this));
    }
}
