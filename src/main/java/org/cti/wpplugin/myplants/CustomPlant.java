package org.cti.wpplugin.myplants;

import org.pepsoft.minecraft.Entity;
import org.pepsoft.minecraft.Material;
import org.pepsoft.minecraft.TileEntity;
import org.pepsoft.util.AttributeKey;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.exporting.MinecraftWorld;
import org.pepsoft.worldpainter.objects.WPObject;

import javax.vecmath.Point3i;
import java.io.Serializable;
import java.util.*;

import org.cti.wpplugin.utils.Pair;

public class CustomPlant implements WPObject {
    private String name;
    private String domain;
    private ArrayList<PlantElement> palette = new ArrayList<>();
    private Set<Material> foundations = new HashSet<>();
    private Material prefer_foundation;
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
    }

    public CustomPlant(String name, String domain, String material){
        this.name = name;
        this.domain = domain;
        this.palette.add(new PlantElement(Material.get(material)));
        this.enableRandom = true;
        nextObject();
    }

    public CustomPlant(String name, String domain, PlantElement material){
        this.name = name;
        this.domain = domain;
        this.palette.add(material);
        this.enableRandom = true;
        nextObject();
    }

    public CustomPlant setFoundations(Pair<Set<Material>, Material> pair) {
        this.foundations = pair.first;
        this.prefer_foundation = pair.second;
        return this;
    }

    public Material getPreferFoundation(){return prefer_foundation;}

    public String getFullName(){
        return domain+":"+name;
    }

    public CustomPlant setGlobalProperties(String id, List<String> stringList){
        globalProperties.put(id,new Pair<>(new StringBuilder(stringList.get(0)),stringList));
        return this;
    }

    public CustomPlant linkAllGlobalProperties(){
        globalProperties.forEach((key,value)->{
            palette.forEach(item->item.linkGlobalSettings(key,value.first));
        });
        return this;
    }

    public CustomPlant nextObject(Random random){
        if(!enableRandom)
            return this;
        this.random = random;
        globalProperties.forEach((key,value)->{
            value.first.replace(0,value.first.length(),value.second.get(random.nextInt(value.second.size())));
        });
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

    public boolean isValidFoundation(MinecraftWorld world, int x, int y, int height){
        Material material = world.getMaterialAt(x,y,height);
        for(Material m:foundations){
            if(material.isNamed(m.name)) return true;
        }
        return false;
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


}
