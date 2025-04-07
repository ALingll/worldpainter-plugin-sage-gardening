package org.cti.wpplugin.utils;

import javax.vecmath.Point3i;
import java.util.ArrayList;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-03-09 13:42
 **/
import java.util.Collections;
import java.util.List;

public class OctaSpace<T> {
    private ArrayList<ArrayList<ArrayList<T>>>[] space;

    @SuppressWarnings("unchecked")
    public OctaSpace() {
        space = (ArrayList<ArrayList<ArrayList<T>>>[]) new ArrayList[8];
        for (int i = 0; i < 8; i++) {
            space[i] = new ArrayList<>();
        }
    }

    /**
     * @description:
     * x>=0 &nbsp;&nbsp;&&&nbsp;&nbsp; y>=0 &nbsp;&nbsp;&&&nbsp;&nbsp; z>=0 &nbsp;&nbsp;return 0 <br/>
     * x>=0 &nbsp;&nbsp;&&&nbsp;&nbsp; y>=0 &nbsp;&nbsp;&&&nbsp;&nbsp; z&nbsp;&nbsp;<0 &nbsp;&nbsp;return 1 <br/>
     * x>=0 &nbsp;&nbsp;&&&nbsp;&nbsp; y&nbsp;&nbsp;<0 &nbsp;&nbsp;&&&nbsp;&nbsp; z>=0 &nbsp;&nbsp;return 2 <br/>
     * x>=0 &nbsp;&nbsp;&&&nbsp;&nbsp; y&nbsp;&nbsp;<0 &nbsp;&nbsp;&&&nbsp;&nbsp; z&nbsp;&nbsp;<0 &nbsp;&nbsp;return 3 <br/>
     * x&nbsp;&nbsp;<0 &nbsp;&nbsp;&&&nbsp;&nbsp; y>=0 &nbsp;&nbsp;&&&nbsp;&nbsp; z>=0 &nbsp;&nbsp;return 4 <br/>
     * x&nbsp;&nbsp;<0 &nbsp;&nbsp;&&&nbsp;&nbsp; y>=0 &nbsp;&nbsp;&&&nbsp;&nbsp; z&nbsp;&nbsp;<0 &nbsp;&nbsp;return 5 <br/>
     * x&nbsp;&nbsp;<0 &nbsp;&nbsp;&&&nbsp;&nbsp; y&nbsp;&nbsp;<0 &nbsp;&nbsp;&&&nbsp;&nbsp; z>=0 &nbsp;&nbsp;return 6 <br/>
     * x&nbsp;&nbsp;<0 &nbsp;&nbsp;&&&nbsp;&nbsp; y&nbsp;&nbsp;<0 &nbsp;&nbsp;&&&nbsp;&nbsp; z&nbsp;&nbsp;<0 &nbsp;&nbsp;return 7 <br/>
     * @returns: int
     * @param x
     * @param y
     * @param z
     * @author ALingll
     * @date: 2025/3/9 17:16
     */
    public static int getOctant(int x, int y, int z) {
        return (x >= 0 ? 0 : 4) + (y >= 0 ? 0 : 2) + (z >= 0 ? 0 : 1);
    }

    public static int getOctant(Point3i point3i) {
        return getOctant(point3i.x,point3i.y,point3i.z);
    }

    public T get(int x, int y, int z) throws IndexOutOfBoundsException{
        int index = getOctant(x,y,z);
        return space[index]
                .get(x<0?-1-x:x)
                .get(y<0?-1-y:y)
                .get(z<0?-1-z:z);
    }

    public T get(Point3i point3i) throws IndexOutOfBoundsException{
        return get(point3i.x,point3i.y, point3i.z);
    }

    public T set(int x, int y, int z, T value) {
        int index = getOctant(x,y,z);
        return space[index]
                .get(x<0?-1-x:x)
                .get(y<0?-1-y:y)
                .set(z<0?-1-z:z,value);
    }

    public T set(Point3i point3i, T value){
        return set(point3i.x,point3i.y, point3i.z, value);
    }

    public T setAndFill(int x, int y, int z, T value) {
        int index = getOctant(x,y,z);
        var xList = getWithPadding(space[index], x<0 ? -1-x : x, new ArrayList<>());
        var yList = getWithPadding(xList, y<0 ? -1-y : y, new ArrayList<>());
        return setWithPadding(yList, z<0 ? -1-z : z, value);
    }

    public T getAndFill(int x, int y, int z, T defaultValue) {
        int index = getOctant(x,y,z);
        var xList = getWithPadding(space[index], x<0 ? -1-x : x, new ArrayList<>());
        var yList = getWithPadding(xList, y<0 ? -1-y : y, new ArrayList<>());
        return getWithPadding(yList, z<0 ? -1-z : z, defaultValue);
    }

    public static <T> T getWithPadding(List<T> list, int index, T defaultValue, T paddingValue) {
        if(index >= list.size()){
            setWithPadding(list, index, defaultValue, paddingValue);
        }
        return list.get(index);
    }

    public static <T> T getWithPadding(List<T> list, int index, T defaultValue){
        return getWithPadding(list, index, defaultValue, null);
    }

    public static <T> T setWithPadding(List<T> list, int index, T value, T paddingValue) {
        if (index >= list.size()) {
            int paddingSize = index - list.size() + 1;
            list.addAll(Collections.nCopies(paddingSize, paddingValue));
        }
        return list.set(index, value);
    }

    public static <T> T setWithPadding(List<T> list, int index, T value) {
        return setWithPadding(list,index,value,null);
    }

}

