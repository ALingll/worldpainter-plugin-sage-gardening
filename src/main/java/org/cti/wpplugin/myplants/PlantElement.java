package org.cti.wpplugin.myplants;

import org.pepsoft.minecraft.Material;

import java.util.*;

public class PlantElement {
    private Material material;
    private Map<String, List<StringBuilder>> properties = new HashMap<>();
    public PlantElement(Material material) {
        this.material = material;
    }

    public PlantElement(Material material,Map<String, List<StringBuilder>> properties){
        this.material = material;
        this.properties = properties;
    }

    public Material getMaterial(Random random) {
        if(properties.isEmpty())
            return material;
        final Material[] result = {material};
        properties.forEach((key, value)->{
            result[0] = result[0]
                    .withProperty(key,value.get(random.nextInt(value.size())).toString());});
        return result[0];
    }

    public Material getMaterial() {
        if(properties.isEmpty())
            return material;
        final Material[] result = {material};
        properties.forEach((key, value)->{
            result[0] = result[0]
                    .withProperty(key,value.get(0).toString());});
        return result[0];
    }

    public PlantElement linkGlobalSettings(String id, StringBuilder target){
        properties.forEach((key,value)->{
            for(int i=0;i<value.size();i++){
                if(value.get(i).toString().equals("#"+id)){
                    value.set(i,target);
                }
            }
        });
        return this;
    }


    @Override
    public String toString() {
        return "{material="+material+",properties="+properties.toString()+"}";
    }
}
