package org.demo.wpplugin.myplants;

import com.fasterxml.jackson.databind.JsonNode;
import org.demo.wpplugin.utils.JsonUtils;
import org.pepsoft.minecraft.Material;

import java.util.*;

import static org.demo.wpplugin.utils.JsonUtils.makeString;

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
        System.out.println(result[0]);
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

    public static PlantElement createByJson(JsonNode jsonNode){
        try {
            if (jsonNode.isTextual())
                return new PlantElement(getMaterial(jsonNode.asText()));
            if (jsonNode.isObject()){
                checkJsonFormat(jsonNode);
                Material m = getMaterial(jsonNode.get(BLOCK_TAG).asText());
                Map<String, List<StringBuilder>> prop = new HashMap<>();
                if(jsonNode.has(PROPERTIES_TAG)){
                    JsonNode jsonProperties = jsonNode.get(PROPERTIES_TAG);
                    jsonProperties.fields().forEachRemaining(field ->{
                        if(field.getValue().isTextual()){
                            List<StringBuilder> list = new ArrayList<>();
                            list.add(new StringBuilder(field.getValue().asText()));
                            prop.put(field.getKey(),list);
                        }else if(field.getValue().isArray()){
                            List<StringBuilder> list = new ArrayList<>();
                            for(JsonNode item:field.getValue()){
                                list.add(new StringBuilder(makeString(item)));
                            }
                            prop.put(field.getKey(),list);
                        }
                    });
                }
                return new PlantElement(m,prop);
            }
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private static void checkJsonFormat(JsonNode jsonNode){
        if(!jsonNode.has(BLOCK_TAG))
            throw new IllegalArgumentException("JsonNode should have field \"block\":"+jsonNode.toString());
        if(jsonNode.has(PROPERTIES_TAG)&&!jsonNode.get(PROPERTIES_TAG).isObject())
            throw new IllegalArgumentException("JsonNode's field \"properties\" should be an object:"+jsonNode.toString());
    }

    private static Material getMaterial(String s){
        try {
            Material m = Material.get(s);
            return m;
        }catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Can't create material by name:"+s);
        }
    }

    private static String BLOCK_TAG = "block";
    private static String PROPERTIES_TAG = "properties";

    @Override
    public String toString() {
        return "{material="+material+",properties="+properties.toString()+"}";
    }
}
