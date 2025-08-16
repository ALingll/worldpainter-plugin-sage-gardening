package org.cti.wpplugin.layers;

import com.fasterxml.jackson.databind.JsonNode;
import org.cti.wpplugin.layers.editors.GardeningLayerEditor;
import org.cti.wpplugin.layers.editors.gui.ValueEditor;
import org.cti.wpplugin.layers.exporters.GardeningLayerExporter;
import org.cti.wpplugin.myplants.CustomPlant;
import org.cti.wpplugin.myplants.variable.ProbabilityVar;
import org.cti.wpplugin.myplants.variable.UiVariable;
import org.cti.wpplugin.utils.Pair;
import org.pepsoft.worldpainter.Dimension;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.exporting.LayerExporter;
import org.pepsoft.worldpainter.layers.CustomLayer;
import org.pepsoft.worldpainter.layers.Layer;
import org.pepsoft.worldpainter.layers.bo2.Bo2ObjectProvider;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;
import org.pepsoft.worldpainter.objects.WPObject;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


import static java.lang.Enum.valueOf;
import static org.cti.wpplugin.utils.debug.DebugUtils.classStr;
import static org.pepsoft.worldpainter.layers.Layer.DataSize.BIT;

public class GardeningLayer extends CustomLayer {
    public GardeningLayer() {
        super(NAME, DESCRIPTION, DATA_SIZE, PRIORITY, Color.GREEN);
        this.setPaint(createStarImage());
    }

    private static BufferedImage createStarImage() {
        int size = 16;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // 抗锯齿，让图形更平滑
        //g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 1. 背景：蓝色
        g.setColor(new Color(91,170,223));
        g.fillRect(0, 0, size, size);

        // 2. 五角星坐标
        int centerX = size / 2;
        int centerY = size / 2;
        int outerRadius = 7;
        int innerRadius = 3;
        int points = 10;
        int[] xPoints = new int[points];
        int[] yPoints = new int[points];

        for (int i = 0; i < points; i++) {
            double angle = Math.toRadians(-90 + i * 36);
            int radius = (i % 2 == 0) ? outerRadius : innerRadius;
            xPoints[i] = centerX + (int) Math.round(Math.cos(angle) * radius);
            yPoints[i] = centerY + (int) Math.round(Math.sin(angle) * radius);
        }

        // 3. 白色星星填充
        g.setColor(Color.WHITE);
        g.fillPolygon(xPoints, yPoints, points);

        // 4. 粉色边框
        g.setColor(Color.PINK);
        g.setStroke(new BasicStroke(1));
        g.drawPolygon(xPoints, yPoints, points);

        g.dispose();
        return image;
    }

    /**
     * A custom layer must override this method. The default implementation only works for singular non-configurable
     * {@link Layer}s.
     */
    @Override
    public LayerExporter getExporter(Dimension dimension, Platform platform, ExporterSettings settings) {
        return new GardeningLayerExporter(dimension, platform, settings, this);
    }

    /**
     * Human-readable short name of the plugin.
     */
    static final String NAME = "Gardening Layer";

    /**
     * Human-readable description of the plugin. This is used e.g. in the tooltip of the layer selection button.
     */
    static final String DESCRIPTION = "Loads plants from Json setting file and places them.";

    /**
     * The data size (number of possible values) for this layer. If this changes, the ID must also change.
     * The choices are:
     *
     * <table>
     *     <tr><td>{@link DataSize#BIT}</td><td>Only on (1) or off (0) per block</td></tr>
     *     <tr><td>{@link DataSize#NIBBLE}</td><td>Sixteen values (0-15) per block</td></tr>
     *     <tr><td>{@link DataSize#BYTE}</td><td>256 values (0-255) per block</td></tr>
     *     <tr><td>{@link DataSize#BIT_PER_CHUNK}</td><td>Only on (1) or off (0) per chunk</td></tr>
     * </table>
     */
    static final DataSize DATA_SIZE = BIT;

    /**
     * The priority in the export order for this layer. The exporters for layers with higher numbers will be invoked
     * <em>after</em> those for layers with lower numbers by default. Note that users can change the export order on the
     * Export screen. Some priorities of the built-in layers are:
     *
     * <table>
     *     <tr><td>0-9</td><td>Void</td></tr>
     *     <tr><td>10-19</td><td>Resources and Custom Underground Pockets</td></tr>
     *     <tr><td>20-29</td><td>Caverns, Chasms, Tunnels and Caves</td></tr>
     *     <tr><td>30-39</td><td>Custom Ground Cover, Custom Plants</td></tr>
     *     <tr><td>40-49</td><td>Deciduous Forest, Pine Forest, Swamp Land and Jungle</td></tr>
     *     <tr><td>50-59</td><td>Custom Objects</td></tr>
     *     <tr><td>60-69</td><td>Frost, Annotations</td></tr>
     * </table>
     *
     * <strong>Note</strong> that of course all the first pass exporters are always executed before the second pass
     * exporters.
     */
    static final int PRIORITY = 39;

    /**
     * The colour with which to render this layer in the editor. It must be passed to the superclass constructor, but it
     * can be subsequently ignored by implementing {@link #getRenderer()} and providing your own renderer.
     */
    static final Color COLOUR = Color.MAGENTA;

    /**
     * This class is serialised in the .world file when it is saved, so it must be stable. It is recommended to give it
     * a fixed {@code serialVersionUID} and ensure that any changes are backwards compatible.
     */
    @Serial
    private static final long serialVersionUID = 2017010601L;


    private Map<CustomPlant, PlantSetting> plantMap = new HashMap<>() ;
    private Map<String, Pair<JsonNode,JsonNode>> usedJsons = new HashMap<>();

    public boolean isCheckFoundation() {
        return checkFoundation;
    }
    public void setCheckFoundation(boolean checkFoundation) {
        this.checkFoundation = checkFoundation;
    }

    private boolean checkFoundation = false;
    private int density = 100;

    public void setDensity(int density){this.density=density;}
    public int getDensity(){return this.density;}

    public void putJsonNode(String fileName, JsonNode metaData, JsonNode jsonNode){usedJsons.put(fileName, new Pair<>(metaData,jsonNode));}

    public void setUsedJsons(Map<String, Pair<JsonNode,JsonNode>> map){usedJsons = map;}

    public Map<String, Pair<JsonNode,JsonNode>> getUsedJsons() {
        return usedJsons;
    }

    public Map<CustomPlant, PlantSetting> getPlantMap(){return plantMap;}

    public void setPlantMap(Map<CustomPlant, PlantSetting> plantMap) {
        this.plantMap = plantMap;
    }

    public Bo2ObjectProvider getObjectProvider(Platform platform) {
        List<CustomPlant> customPlantList = new ArrayList<>(plantMap.keySet());
        AtomicInteger sum = new AtomicInteger(0);
        plantMap.forEach((plant, value) -> sum.addAndGet(value.weight));
        List<Integer> index = new ArrayList<>(Collections.nCopies(sum.get(), null));
        int total = 0;

        for(int i = 0; i<customPlantList.size(); i++){
            for(int j = 0; j<plantMap.get(customPlantList.get(i)).weight; j++){
                index.set(total,i);
                total++;
            }
        }

        return new Bo2ObjectProvider() {
            private final Random random = new Random();
            @Override
            public String getName() {
                return GardeningLayer.this.getName();
            }

            @Override
            public WPObject getObject() {
                if(customPlantList.isEmpty()||sum.get()==0)
                    return null;
                return random.nextFloat()*100<=density ? customPlantList.get(index.get(random.nextInt(sum.get()))).nextObject(random) : null;
            }

            @Override
            public List<WPObject> getAllObjects() {
                throw new UnsupportedOperationException("Not supported");
            }

            @Override
            public void setSeed(long seed) {
                random.setSeed(seed);
            }

            @Override
            public Bo2ObjectProvider clone() {
                throw new UnsupportedOperationException("Not supported");
            }
        };
    }

    public PlantSetting getSetting(CustomPlant plant){
        return plantMap.get(plant);
    }

    public void remove(CustomPlant plant){
        plantMap.remove(plant);
    }

    private CustomPlant getPlant(String name, String domain){
        Optional<Map.Entry<CustomPlant, PlantSetting>> result = plantMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey().getFullName().equals(domain+":"+name))
                .findFirst();
        return result.get().getKey();
    }

    private CustomPlant getPlant(String fullId){
        Optional<Map.Entry<CustomPlant, PlantSetting>> result = plantMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey().getFullName().equals(fullId))
                .findFirst();
        return result.get().getKey();
    }

    public GardeningLayer setPlant(CustomPlant customPlant, PlantSetting setting){
        plantMap.put(customPlant,setting);
        return this;
    }

    public GardeningLayer setPlant(CustomPlant customPlant, int weight) {
        if(plantMap.containsKey(customPlant)){
            plantMap.get(customPlant).weight=weight;
        }else{
            plantMap.put(customPlant, new PlantSetting(weight));
        }
        return this;
    }

    public GardeningLayer setPlant(CustomPlant customPlant, String id, Object varObj) {
        if(plantMap.containsKey(customPlant)){
            plantMap.get(customPlant).uiProperties.put(id, varObj);
        }
        return this;
    }

    public GardeningLayer setPlant(String name, String domain, String id, Object varObj) {
        return setPlant(getPlant(name,domain),id,varObj);
    }

    public GardeningLayer setPlant(String fullId, String id, Object varObj) {
        return setPlant(getPlant(fullId),id,varObj);
    }

    public GardeningLayer setPlant(String name, String domain, int weight) {
        return setPlant(getPlant(name,domain),weight);
    }

    public GardeningLayer setPlant(String fullId, int weight) {
        return setPlant(getPlant(fullId),weight);
    }



//    @Override
//    public String toString(){
//        Map<String,Integer> map = new HashMap<>();
//        plantMap.forEach((key,value)->map.put(key.getFullName(),value.weight));
//        return map+"\n"+usedJsons.keySet();
//    }

    @Override
    public GardeningLayer clone() {
        GardeningLayer layer = new GardeningLayer();
        layer.plantMap = new HashMap<>();
        this.plantMap.forEach((key,value)-> layer.plantMap.put(key,value));
        layer.usedJsons = new HashMap<>();
        layer.usedJsons.putAll(this.usedJsons);
        return layer;
    }

    public void copyFrom(GardeningLayer layer){
        this.usedJsons = layer.getUsedJsonsDeepCopy();
        this.density = layer.density;
        this.checkFoundation = layer.checkFoundation;
        this.plantMap = new HashMap<>();
        this.plantMap.putAll(layer.plantMap);
//        Map<CustomPlant, GardeningLayer.PlantSetting> oldMap = layer.plantMap;
//        this.plantMap.forEach((key,value)->{
//            if(!oldMap.containsKey(key))
//                this.plantMap.remove(key);
//            else{
//                value.uiProperties.forEach((nameKey,obj)->{
//
//                });
//                Map<String,Object> oldUiProperties = oldMap.get(key).uiProperties;
//                key.getAllUiVar().forEach(entry -> {
//                    String id = entry.getKey();
//                    UiVariable uiVariable = entry.getValue();
//                    uiVariable.copyFrom(oldUiProperties.get(id));
//                });
//            }
//        });
//        oldMap.forEach((key,value)->{
//            if(!this.plantMap.containsKey(key)) this.plantMap.put(key,value);
//        });
    }

    private Map<String, Pair<JsonNode, JsonNode>> getUsedJsonsDeepCopy() {
        Map<String, Pair<JsonNode, JsonNode>> copy = new HashMap<>();
        for (Map.Entry<String, Pair<JsonNode, JsonNode>> entry : usedJsons.entrySet()) {
            String keyCopy = entry.getKey(); // String 不可变，直接引用没问题
            JsonNode leftCopy = entry.getValue().first == null
                    ? null
                    : entry.getValue().first.deepCopy();
            JsonNode rightCopy = entry.getValue().second == null
                    ? null
                    : entry.getValue().second.deepCopy();
            copy.put(keyCopy, Pair.makePair(leftCopy, rightCopy));
        }

        return copy;
    }

    @Override
    public String toString(){
        final String[] mapStr = {""};
        plantMap.forEach((key,value)->{
            mapStr[0] = mapStr[0]+"(key="+key.toString().replace("\n","\n\t")+",\nvalue="+value+"\n";
        });
        return classStr(this)+"{\n"
                +"\tplantMap=\t"+classStr(plantMap)+mapStr[0].replace("\n","\n\t")+"\n"
                +"}";
    }

    public enum FoundationHandle{
        IGNORE("Plants will ignore the foundation type and can be placed anywhere"),
        CHECK_FOUNDATION("Plants will not be placed on unsupported foundation types"),
        ADD_FOUNDATION("The plant can be placed anywhere, and the block at its root will be replaced with the preferred Foundation");

        private final String description;

        FoundationHandle(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static class PlantSetting implements Serializable {
        public int weight = 0;
        public Map<String, Object> uiProperties = new HashMap<>();
        public PlantSetting(int weight){
            this.weight = weight;
        }
        public PlantSetting(int weight, Map<String, Object> uiProperties){
            this.weight = weight;
            this.uiProperties = uiProperties;
        }
        @Override
        public String toString(){return classStr(this)+String.format("{w=%d,p=%s}",weight,uiProperties);}

    }
}