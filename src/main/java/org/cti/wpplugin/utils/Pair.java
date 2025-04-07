package org.cti.wpplugin.utils;

public class Pair<K, V> {
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

