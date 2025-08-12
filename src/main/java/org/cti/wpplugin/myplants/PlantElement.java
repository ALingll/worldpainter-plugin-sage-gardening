package org.cti.wpplugin.myplants;

import org.cti.wpplugin.myplants.variable.RandomVariable;
import org.cti.wpplugin.myplants.variable.SingleChoiceVar;
import org.cti.wpplugin.utils.Range;
import org.pepsoft.minecraft.Material;

import java.io.Serial;
import java.io.Serializable;
import java.util.*;

public class PlantElement implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Material material;
    private Map<String, SingleChoiceVar> properties = new HashMap<>();
    private RandomVariable<Integer> times;
    public PlantElement(Material material) {
        this.material = material;
    }

    public PlantElement(Material material,Map<String, SingleChoiceVar> properties){
        this.material = material;
        this.properties = properties;
    }

    public PlantElement(Material material,Map<String, SingleChoiceVar> properties, RandomVariable<Integer> times){
        this.material = material;
        this.properties = properties;
        this.times = times;
    }

    public Material getMaterial() {
        if(properties.isEmpty())
            return material;
        final Material[] result = {material};
        properties.forEach((key, value)->{
            result[0] = result[0]
                    .withProperty(key, value.getValue());});
        return result[0];
    }

    public Material getMaterial(Random random) {
        if(properties.isEmpty())
            return material;
        final Material[] result = {material};
        properties.forEach((key, value)->{
            result[0] = result[0]
                    .withProperty(key,value.random(random));});
        return result[0];
    }

    public int getTimes(){
        return times.getValue();
    }

    public Map<String, SingleChoiceVar> getAllProperties(){return properties;}

//    public int getTimes(Random random){
//        return times.random(random);
//    }

//    public Material getMaterial() {
//        if(properties.isEmpty())
//            return material;
//        final Material[] result = {material};
//        properties.forEach((key, value)->{
//            result[0] = result[0]
//                    .withProperty(key,value.get(0).toString());});
//        return result[0];
//    }

//    public PlantElement linkGlobalSettings(String id, StringBuilder target){
//        properties.forEach((key,value)->{
//            for(int i=0;i<value.size();i++){
//                if(value.get(i).toString().equals("#"+id)){
//                    value.set(i,target);
//                }
//            }
//        });
//        return this;
//    }


    @Override
    public String toString() {
        return "{material="+material
                + ",properties="+properties.toString()
                + ",times="+times
                + "}";
    }
}
