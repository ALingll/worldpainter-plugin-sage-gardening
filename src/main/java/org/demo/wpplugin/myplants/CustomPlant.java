package org.demo.wpplugin.myplants;

import com.fasterxml.jackson.databind.JsonNode;
import org.checkerframework.checker.units.qual.C;
import org.pepsoft.minecraft.Entity;
import org.pepsoft.minecraft.Material;
import org.pepsoft.minecraft.TileEntity;
import org.pepsoft.util.AttributeKey;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.objects.WPObject;

import javax.vecmath.Point3i;
import java.io.Serializable;
import java.util.*;

import org.demo.wpplugin.utils.Pair;

import static org.demo.wpplugin.utils.JsonUtils.makeString;

public class CustomPlant implements WPObject {
    private String name;
    private String domain;
    private ArrayList<PlantElement> palette = new ArrayList<>();
    private Map<String, Pair<StringBuilder,List<String>>> globalProperties = new HashMap<>();
    private Point3i dimension = new Point3i(1,1,1);
    private boolean enableRandom = true;
    private Random random = null;
    private int[][][] blockSpace;

    public CustomPlant(String name, String domain, ArrayList<PlantElement> palette, boolean enableRandom) {
        this.name = name;
        this.domain = domain;
        this.palette = palette;
        this.enableRandom = true;
        nextObject();
        this.enableRandom = enableRandom;
    }

    public CustomPlant(String name, String domain, Material material){
        this.name = name;
        this.domain = domain;
        this.palette.add(new PlantElement(material));
        this.enableRandom = true;
        nextObject();
        this.enableRandom = false;
    }

    public CustomPlant(String name, String domain, String material){
        this.name = name;
        this.domain = domain;
        this.palette.add(new PlantElement(Material.get(material)));
        this.enableRandom = true;
        nextObject();
        this.enableRandom = false;
    }

    public CustomPlant(String name, String domain, PlantElement material){
        this.name = name;
        this.domain = domain;
        this.palette.add(material);
        this.enableRandom = true;
        nextObject();
        this.enableRandom = false;
    }

    public String getFullName(){
        return domain+":"+name;
    }

    public CustomPlant setGlobalProperties(String id, List<String> stringList){
        globalProperties.put(id,new Pair<>(new StringBuilder(stringList.get(0)),stringList));
        return this;
    }

    private CustomPlant linkAllGlobalProperties(){
        globalProperties.forEach((key,value)->{
            palette.forEach(item->item.linkGlobalSettings(key,value.first));
        });
        return this;
    }

    public CustomPlant nextObject(Random random){
        if(!enableRandom)
            return this;
        this.random = random;
        int height = palette.size();
        dimension.z = height;
        blockSpace = new int[1][1][height];
        for (int i=0;i<height;i++){
            blockSpace[0][0][i]=i;
        }
        return this;
    }

    public CustomPlant nextObject(){
        if(!enableRandom)
            return this;
        int height = palette.size();
        dimension.z = height;
        blockSpace = new int[1][1][height];
        for (int i=0;i<height;i++){
            blockSpace[0][0][i]=i;
        }
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {

    }

    @Override
    public Point3i getDimensions() {
        return dimension;
    }

    @Override
    public Point3i getOffset() {
        return new Point3i(0,0,0);
    }

    @Override
    public Material getMaterial(int x, int y, int z) {
        if(this.random != null)
            return palette.get(blockSpace[x][y][z]).getMaterial(random);
        return palette.get(blockSpace[x][y][z]).getMaterial();
    }

    @Override
    public boolean getMask(int x, int y, int z) {
        return true;
    }

    @Override
    public List<Entity> getEntities() {
        return null;
    }

    @Override
    public List<TileEntity> getTileEntities() {
        return null;
    }

    @Override
    public void prepareForExport(Dimension dimension) {}

    @Override
    public Map<String, Serializable> getAttributes() {
        return null;
    }

    @Override
    public void setAttributes(Map<String, Serializable> attributes) {

    }

    @Override
    public <T extends Serializable> void setAttribute(AttributeKey<T> key, T value) {

    }

    @Override
    public WPObject clone() {
        return null;
    }

    @Override
    public int hashCode(){
        return Objects.hash(name,domain);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomPlant p = (CustomPlant) o;
        return domain.equals(p.domain) && name.equals(p.name);
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getFullName()+":"+"\n");
        stringBuilder.append("global_properties:"+globalProperties.toString()+"\n");
        stringBuilder.append("palette:"+palette.toString());
        return stringBuilder.toString();
    }

    public static CustomPlantBuilder getBuilder(){return new CustomPlantBuilder();}

    public static class CustomPlantBuilder{
        private String name = null;
        private String domain = null;
        private ArrayList<PlantElement> palette = new ArrayList<>();
        private boolean enableRandom = false;

        public CustomPlantBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public CustomPlantBuilder setDomain(String domain) {
            this.domain = domain;
            return this;
        }

        public CustomPlantBuilder setPalette(ArrayList<PlantElement> palette) {
            this.palette = palette;
            return this;
        }

        public CustomPlantBuilder pushBack(PlantElement plantElement) {
            palette.add(plantElement);
            return this;
        }

        public CustomPlantBuilder setEnableRandom(boolean enableRandom) {
            this.enableRandom = enableRandom;
            return this;
        }

        public CustomPlant build(){
            return new CustomPlant(name,domain,palette, enableRandom);
        }
    }

    public static CustomPlant createByJson(String name, String domain, JsonNode jsonNode){
        try {
            if(jsonNode.isTextual()){
                return new CustomPlant(name,domain,jsonNode.asText());
            }else if(jsonNode.isArray()){
                ArrayList<PlantElement> t_palette = new ArrayList<>();
                for (JsonNode item:jsonNode){
                    t_palette.add(PlantElement.createByJson(item));
                }
                return new CustomPlant(name, domain, t_palette, true);
            }else if(jsonNode.isObject()){
                if(jsonNode.has(DATA_TAG)){
                    ArrayList<PlantElement> t_palette = new ArrayList<>();
                    JsonNode data = jsonNode.get(DATA_TAG);
                    if(data.isArray())
                        for (JsonNode item:data){
                            t_palette.add(PlantElement.createByJson(item));
                        }
                    else {
                        t_palette.add(PlantElement.createByJson(data.get(DATA_TAG)));
                    }
                    CustomPlant result = new CustomPlant(name,domain,t_palette,true);
                    if(jsonNode.has(SETTINGS_TAG)){
                        JsonNode settings = jsonNode.get(SETTINGS_TAG);
                        if(settings.has(GLOBAL_PROPERTIES_TAG)){
                            JsonNode global_properties_data = settings.get(GLOBAL_PROPERTIES_TAG);
                            global_properties_data.fields().forEachRemaining((item)->{
                                List<String> values = new ArrayList<>();
                                if(item.getValue().isArray()){
                                    item.getValue().forEach(v->values.add(makeString(v)));
                                }else {
                                    values.add(makeString(item.getValue()));
                                }
                                result.setGlobalProperties(item.getKey().toString(),values);
                            });
                            result.linkAllGlobalProperties();
                        }
                    }
                    return result;
                }else {
                    return new CustomPlant(name,domain,PlantElement.createByJson(jsonNode));
                }
            }else {
                throw new IllegalArgumentException("Unsupported json format as CustomPlant:"+jsonNode.toPrettyString());
            }
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            return null;
        }
    }

    private static String DATA_TAG = "data";
    private static String SETTINGS_TAG = "settings";
    private static String GLOBAL_PROPERTIES_TAG = "global_properties";

}
