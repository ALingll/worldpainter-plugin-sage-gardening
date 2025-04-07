package org.cti.wpplugin.layers;

import com.fasterxml.jackson.databind.JsonNode;
import org.cti.wpplugin.layers.exporters.GardeningLayerExporter;
import org.cti.wpplugin.myplants.CustomPlant;
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
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.pepsoft.worldpainter.layers.Layer.DataSize.BIT;

public class GardeningLayer extends CustomLayer {
    public GardeningLayer() {
        super(NAME, DESCRIPTION, DATA_SIZE, PRIORITY, COLOUR);
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
    private static final long serialVersionUID = 1L;


    private Map<CustomPlant, Integer> plantMap = new HashMap<>() ;
    private Map<String, Pair<JsonNode,JsonNode>> usedJsons = new HashMap<>();

    public boolean isCheckFoundation() {
        return checkFoundation;
    }

    public void setCheckFoundation(boolean checkFoundation) {
        this.checkFoundation = checkFoundation;
    }

    private boolean checkFoundation = false;


    public void putJsonNode(String fileName, JsonNode metaData, JsonNode jsonNode){usedJsons.put(fileName, new Pair<>(metaData,jsonNode));}

    public void setUsedJsons(Map<String, Pair<JsonNode,JsonNode>> map){usedJsons = map;}

    public Map<String, Pair<JsonNode,JsonNode>> getUsedJsons() {
        return usedJsons;
    }

    public Map<CustomPlant, Integer> getPlantMap(){return plantMap;}

    public void setPlantMap(Map<CustomPlant, Integer> plantMap) {
        this.plantMap = plantMap;
    }

    public Bo2ObjectProvider getObjectProvider(Platform platform) {
        List<CustomPlant> customPlantList = new ArrayList<>(plantMap.keySet());
        AtomicInteger sum = new AtomicInteger(0);
        plantMap.forEach((plant, value) -> sum.addAndGet(value));
        List<Integer> index = new ArrayList<>(Collections.nCopies(sum.get(), null));
        int total = 0;

        for(int i = 0; i<customPlantList.size(); i++){
            for(int j = 0; j<plantMap.get(customPlantList.get(i)); j++){
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
                return customPlantList.get(index.get(random.nextInt(sum.get()))).nextObject(random);
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

    public GardeningLayer setPlant(CustomPlant customPlant, int weight) {
        plantMap.put(customPlant,weight);
        return this;
    }

    public GardeningLayer setPlant(String name, String domain, int weight) {
        Optional<Map.Entry<CustomPlant, Integer>> result = plantMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey().getFullName().equals(domain+":"+name))
                .findFirst();
        plantMap.put(result.get().getKey(), weight);
        return this;
    }

    public GardeningLayer setPlant(String fullId, int weight) {
        Optional<Map.Entry<CustomPlant, Integer>> result = plantMap.entrySet()
                .stream()
                .filter(entry -> entry.getKey().getFullName().equals(fullId))
                .findFirst();
        plantMap.put(result.get().getKey(), weight);
        return this;
    }

    @Override
    public String toString(){
        Map<String,Integer> map = new HashMap<>();
        plantMap.forEach((key,value)->map.put(key.getFullName(),value));
        return map+"\n"+usedJsons.keySet();
    }

    @Override
    public GardeningLayer clone() {
        GardeningLayer layer = new GardeningLayer();
        layer.plantMap = new HashMap<>();
        this.plantMap.forEach((key,value)-> layer.plantMap.put(key,value));
        layer.usedJsons = new HashMap<>();
        layer.usedJsons.putAll(this.usedJsons);
        return layer;
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
}