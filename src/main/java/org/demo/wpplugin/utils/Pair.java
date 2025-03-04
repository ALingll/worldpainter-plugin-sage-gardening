package org.demo.wpplugin.utils;

import java.util.*;

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
}

