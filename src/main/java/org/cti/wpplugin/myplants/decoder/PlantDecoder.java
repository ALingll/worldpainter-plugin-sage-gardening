package org.cti.wpplugin.myplants.decoder;

import com.fasterxml.jackson.databind.JsonNode;
import org.cti.wpplugin.myplants.CustomPlant;
import org.cti.wpplugin.myplants.PlantElement;
import org.cti.wpplugin.utils.BlockTags;
import org.cti.wpplugin.utils.Pair;
import org.cti.wpplugin.utils.Range;
import org.pepsoft.minecraft.Material;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.cti.wpplugin.utils.JsonUtils.*;

public class PlantDecoder {

    public static final String FOUNDATIONS_TAG = "foundations";
    public static final String META_DATA_TAG = "metadata";
    public static final String DECODER_VERSION = "beta-1.0";
    public static final String ENCODER_VERSION_TAG = "encoder_v";
    public static final String PROVIDES_TAG = "provides";
    public static final String AUTHOR_TAG = "author";
    public static final String DATA_TAG = "data";
    public static final String SETTINGS_TAG = "settings";
    public static final String GLOBAL_PROPERTIES_TAG = "global_properties";
    public static final String UI_PROPERTIES_TAG = "ui_properties";
    public static final String UI_TYPE_TAG = "type";
    public static final String UI_MAX_VALUE_TAG = "max";
    public static final String UI_MIN_VALUE_TAG = "min";
    public static final String UI_DEFAULT_VALUE_TAG = "default";
    public static final String BLOCK_TAG = "block";
    public static final String PROPERTIES_TAG = "properties";
    public static final String TIMES_TAG = "times";

    private static Material getMaterial(String s){
        try {
            Material m = Material.get(s);
            return m;
        }catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Can't create material by name:"+s);
        }
    }

    public static PlantElement loadPlantElementByJson(JsonNode jsonNode){
        try {
            if (jsonNode.isTextual())
                return new PlantElement(getMaterial(jsonNode.asText()));
            if (jsonNode.isObject()){
                checkElementJsonFormat(jsonNode);
                Material m = getMaterial(jsonNode.get(BLOCK_TAG).asText());
                Map<String, List<StringBuilder>> prop = new HashMap<>();
                AtomicReference<Range> times = new AtomicReference<>(new Range(1, 1));
                optionalGet(jsonNode,TIMES_TAG).ifPresent(node -> {
                    times.set(Range.asRange(node.textValue()));
                });
                optionalGet(jsonNode,PROPERTIES_TAG).ifPresent(jsonProperties->{
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
                });
                return new PlantElement(m,prop, times.get());
            }
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private static void checkElementJsonFormat(JsonNode jsonNode){
        if(!jsonNode.has(BLOCK_TAG))
            throw new IllegalArgumentException("JsonNode should have field \"block\":"+jsonNode.toString());
        if(jsonNode.has(PROPERTIES_TAG)&&!jsonNode.get(PROPERTIES_TAG).isObject())
            throw new IllegalArgumentException("JsonNode's field \"properties\" should be an object:"+jsonNode.toString());
    }

    public static CustomPlant loadPlantByJson(String name, String domain, JsonNode jsonNode){
        try {
            if(jsonNode.isTextual()){
                return new CustomPlant(name,domain,jsonNode.asText());
            }else if(jsonNode.isArray()){
                ArrayList<PlantElement> t_palette = new ArrayList<>();
                for (JsonNode item:jsonNode){
                    t_palette.add(loadPlantElementByJson(item));
                }
                return new CustomPlant(name, domain, t_palette, true);
            }else if(jsonNode.isObject()){
                Pair<Set<Material>, Material> foundations = loadFoundationsByJson(jsonNode);
                if(jsonNode.has(DATA_TAG)){
                    ArrayList<PlantElement> t_palette = new ArrayList<>();
                    JsonNode data = jsonNode.get(DATA_TAG);
                    if(data.isArray())
                        for (JsonNode item:data){
                            t_palette.add(loadPlantElementByJson(item));
                        }
                    else {
                        t_palette.add(loadPlantElementByJson(data.get(DATA_TAG)));
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
                                result.setVariableProperties(item.getKey().toString(),values);
                            });
                            result.linkAllGlobalProperties();
                        }
                        optionalGet(settings,UI_PROPERTIES_TAG).ifPresent(ui_properties -> {
                            ui_properties.properties().forEach(item ->{
                                String varName = item.getKey();
                                JsonNode varBody = item.getValue();
                                switch (strictGet(varBody,UI_TYPE_TAG).textValue()){
                                    case "range" : {
                                        int maxValue = strictGet(varBody,UI_MAX_VALUE_TAG).asInt();
                                        int minValue = strictGet(varBody,UI_MIN_VALUE_TAG).asInt();
                                        Range range = new Range(maxValue,minValue);
                                        optionalGet(varBody,UI_DEFAULT_VALUE_TAG).ifPresent(node ->{
                                            Range defaultRange = Range.asRange(node.textValue());
                                            range.high = defaultRange.high;
                                            range.low = defaultRange.low;
                                        });
                                    }
                                    default:
                                        throw new IllegalArgumentException(
                                                "Unsupported UI type "+"\""
                                                +strictGet(varBody,UI_TYPE_TAG).textValue()
                                                +"\"");
                                }
                            });
                        });
                    }
                    return result.setFoundations(foundations);
                }else {
                    return new CustomPlant(name,domain,loadPlantElementByJson(jsonNode)).setFoundations(foundations);
                }
            }else {
                throw new IllegalArgumentException("Unsupported json format as CustomPlant:"+jsonNode.toPrettyString());
            }
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            return null;
        }
    }

    private static Pair<Set<Material>, Material> loadFoundationsByJson(JsonNode jsonNode) {
        List<Material> result = new ArrayList<>();
        optionalGet(jsonNode, FOUNDATIONS_TAG)
                .ifPresent(value->makeStringList(value)
                        .forEach(str->{
                            if(str.startsWith("#"))
                                BlockTags.named(str.substring(1)).forEach(material -> result.add(material));
                            else
                                result.add(Material.get(str));
                        }));
        Material prefer = result.isEmpty() ? null : result.get(0);
        return Pair.makePair(new HashSet<>(result),prefer);
    }


    public static boolean checkDecoderVersion(String s){return s.equals(DECODER_VERSION);}

}
