package org.cti.wpplugin.utils;

import java.io.Serializable;

public class Pair<K, V> implements Serializable {
    public K first;
    public V second;

    public Pair(K k, V v) {
        first = k;
        second = v;
    }

    @Override
    public String toString(){
        return "("+first+", "+second+")";
    }

    public static <K,V> Pair<K,V> makePair(K k, V v){return new Pair<>(k,v);}

}

