package org.cti.wpplugin.myplants;

import org.cti.wpplugin.layers.GardeningLayer;
import org.cti.wpplugin.myplants.variable.RandomVariable;
import org.cti.wpplugin.myplants.variable.SingleChoiceVar;
import org.cti.wpplugin.myplants.variable.UiVariable;
import org.cti.wpplugin.myplants.variable.Variable;
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

import static org.cti.wpplugin.utils.debug.DebugUtils.classStr;

public class CustomPlant implements WPObject {
    private String name;
    private String domain;
    private ArrayList<PlantElement> palette = new ArrayList<>();
    private Set<Material> foundations = new HashSet<>();
    private Material prefer_foundation;
    private Map<String, RandomVariable> globalVariables = new HashMap<>();      //命名变量
    private Map<String, UiVariable> uiVariableMap =new HashMap<>();             //Ui控制变量
    private Set<RandomVariable> allVariables = new HashSet<>();                 //所有随机变量，包括子元素匿名变量和全局命名变量
    private Point3i dimension = new Point3i(1,1,1);
    private boolean enableRandom = true;
    private Random random = null;
    private int[][][] blockSpace;

    public CustomPlant(String name, String domain, List<PlantElement> palette, boolean enableRandom) {
        this.name = name;
        this.domain = domain;
        palette.forEach(this::addElement);
        this.enableRandom = true;
        nextObject();
        this.enableRandom = enableRandom;
    }

    public CustomPlant(String name, String domain, boolean enableRandom) {
        this.name = name;
        this.domain = domain;
        this.enableRandom = enableRandom;
    }

    public CustomPlant(String name, String domain, Material material){
        this.name = name;
        this.domain = domain;
        addElement(new PlantElement(material));
        this.enableRandom = true;
        nextObject();
    }

    public CustomPlant(String name, String domain, String material){
        this.name = name;
        this.domain = domain;
        addElement(new PlantElement(Material.get(material)));
        this.enableRandom = true;
        nextObject();
    }

    public CustomPlant(String name, String domain, PlantElement material){
        this.name = name;
        this.domain = domain;
        addElement(material);
        this.enableRandom = true;
        nextObject();
    }

    public CustomPlant setFoundations(Pair<Set<Material>, Material> pair) {
        this.foundations = pair.first;
        this.prefer_foundation = pair.second;
        return this;
    }

    public CustomPlant addElement(PlantElement material){
        this.palette.add(material);
        allVariables.addAll(material.getAllProperties().values());
        return this;
    }

    public Material getPreferFoundation(){return prefer_foundation;}

    public String getFullName(){
        return domain+":"+name;
    }

//    public CustomPlant setVariableProperties(String id, List<String> stringList){
//        globalVariables.put(id,new SingleChoiceVar(stringList));
//        return this;
//    }

    public CustomPlant setNamedVar(String id, RandomVariable variable){
        globalVariables.put(id,variable);
        allVariables.add(variable);
        return this;
    }

    public RandomVariable getNamedVar(String id){
        return globalVariables.get(id);
    }

    public CustomPlant setUiVar(String id, UiVariable variable){
        uiVariableMap.put(id, variable);
        return this;
    }

    public Set<Map.Entry<String, UiVariable>> getAllUiVar(){
        return uiVariableMap.entrySet();
    }

    public <VarType extends RandomVariable> VarType getNamedVar(String id, Class<VarType> clazz) throws IllegalArgumentException {
        RandomVariable variable = getNamedVar(id);
        if (variable==null)
            throw new IllegalArgumentException(id+" has not defined.");
        if(!clazz.isInstance(variable)){
            throw new IllegalArgumentException(
                    String.format("%s type: %s \t Required type %s", id, id.getClass().getName(), clazz.getName()));
        }
        return clazz.cast(variable);
    }

//    public CustomPlant setUiProperties(String id, Variable variable){
//        uiProperties.put(id,variable);
//        return this;
//    }

//    public CustomPlant linkAllGlobalProperties(){
//        variableProperties.forEach((key, value)->{
//            palette.forEach(item->item.linkGlobalSettings(key,value.getVariable()));
//        });
//        return this;
//    }

    public CustomPlant nextObject(Random random){
        if(!enableRandom)
            return this;
        this.random = random;

        allVariables.forEach(item -> item.random(random));

        int[] allNum = palette.stream()
                .mapToInt(PlantElement::getTimes)
                .toArray();
        int sum = Arrays.stream(allNum).sum();
        dimension.z = sum;
        blockSpace = new int[1][1][sum];
        int index = 0;
        for (int i=0;i<palette.size();i++){
            for(int j=0;j<allNum[i];j++){
                blockSpace[0][0][index]=i;
                index++;
            }
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
            return palette.get(blockSpace[x][y][z]).getMaterial();
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

//    @Override
//    public String toString(){
//        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder.append(getFullName()+":"+"\n");
//        stringBuilder.append("global_properties:"+ globalVariables.toString()+"\n");
//        stringBuilder.append("palette:"+palette.toString());
//        return stringBuilder.toString();
//    }

    public static CustomPlantBuilder getBuilder(){return new CustomPlantBuilder();}

    public GardeningLayer.PlantSetting enable(GardeningLayer.PlantSetting setting){
        Map<String, Object> settingVar = setting.uiProperties;
        Map<String, Object> newSettingVar = new HashMap<>();
        settingVar.forEach((key,value)->{
            newSettingVar.put(key,uiVariableMap.get(key).copyFrom(value));
        });
        return new GardeningLayer.PlantSetting(setting.weight, newSettingVar);
    }

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

    @Override
    public String toString(){
        return classStr(this)+"{\n\t"
                +"id="+domain+"L"+name+"\n\t"
                +"uiVar"+uiVariableMap+"}";
    }

}
