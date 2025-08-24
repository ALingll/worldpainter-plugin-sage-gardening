package org.cti.wpplugin.myplants.decoder;

import com.fasterxml.jackson.databind.JsonNode;
import org.cti.wpplugin.myplants.CustomPlant;
import org.cti.wpplugin.myplants.PlantElement;
import org.cti.wpplugin.myplants.PlantHabit;
import org.cti.wpplugin.myplants.variable.*;
import org.cti.wpplugin.minecraft.BlockTags;
import org.cti.wpplugin.utils.Pair;
import org.cti.wpplugin.utils.Range;
import org.pepsoft.minecraft.Material;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;

import static org.cti.wpplugin.myplants.decoder.TemplateProvider.loadTemplate;
import static org.cti.wpplugin.utils.FormatString.asPercent;
import static org.cti.wpplugin.utils.FormatString.asRange;
import static org.cti.wpplugin.utils.JsonUtils.*;

public class PlantDecoder {

    public static final String FOUNDATIONS_TAG = "foundations";
    public static final String DESCRIPTION_TAG = "desc";
    public static final String META_DATA_TAG = "metadata";
    public static final String DECODER_VERSION = "beta-1.0";
    public static final String ENCODER_VERSION_TAG = "encoder_v";
    public static final String PROVIDES_TAG = "provides";
    public static final String AUTHOR_TAG = "author";
    public static final String DATA_TAG = "data";
    public static final String ICON_TAG = "icon";
    public static final String HABIT_TAG = "habit";
    public static final String TEMPLATE_TAG = "template";
    public static final String SETTINGS_TAG = "settings";
    public static final String GLOBAL_PROPERTIES_TAG = "global_properties";
    public static final String UI_PROPERTIES_TAG = "ui_properties";
    public static final String UI_TYPE_TAG = "type";
    public static final String UI_MAX_VALUE_TAG = "max";
    public static final String UI_MIN_VALUE_TAG = "min";
    public static final String UI_DEFAULT_VALUE_TAG = "default";
    public static final String BLOCK_TAG = "block";
    public static final String STATE_TAG = "state";
    public static final String PROPERTIES_TAG = "properties";
    public static final String TIMES_TAG = "times";

    private static Material getMaterial(String s){
        try {
            Material m = MaterialDecoder.getMaterial(s);
            System.out.println(m);
            return m;
        }catch (IllegalArgumentException e){
            throw new IllegalArgumentException("Can't create material by name: "+s);
        }
    }

    public static PlantElement loadPlantElementByJson(JsonNode jsonNode, CustomPlant parent){
        try {
            if (jsonNode.isTextual())
                return new PlantElement(getMaterial(jsonNode.asText()));
            if (jsonNode.isObject()){
                checkElementJsonFormat(jsonNode);
                Material m = getMaterial(jsonNode.get(BLOCK_TAG).asText());
                Map<String, SingleChoiceVar> prop = new HashMap<>();
                optionalGet(jsonNode,PROPERTIES_TAG).ifPresent(jsonProperties->{
                    jsonProperties.fields().forEachRemaining(field ->{
                        if(field.getValue().isTextual()){
                            String text = field.getValue().asText();
                            tryLoadVar(text).ifPresentOrElse(
                                    id -> {
                                        if(parent == null) throw new IllegalArgumentException("Parent is null.");
                                        prop.put(field.getKey(), parent.getNamedVar(id, SingleChoiceVar.class));
                                    },
                                    () -> {
                                        List<String> list = new ArrayList<>();
                                        list.add(text);
                                        prop.put(field.getKey(),new SingleChoiceVar(list));
                                    }
                            );
                        }else if(field.getValue().isArray()){
                            List<String> list = new ArrayList<>();
                            for(JsonNode item:field.getValue()){
                                list.add(makeString(item));
                            }
                            prop.put(field.getKey(),new SingleChoiceVar(list));
                        }
                    });
                });

                AtomicReference<RandomVariable<Integer>> times = new AtomicReference<>(new RangeVar(new Range(1,1)));
                optionalGet(jsonNode,TIMES_TAG).ifPresent(node -> {
                    tryLoadVar(node.textValue())
                            .ifPresentOrElse(
                                    id -> {
                                        if(parent == null) throw new IllegalArgumentException("Parent is null.");
                                        //System.out.println(id+" "+parent.getNamedVar(id, AbstractIntVar.class));
                                        times.set(parent.getNamedVar(id, AbstractIntVar.class));
                                    },
                                    () -> {
                                        Range range = asRange(node.textValue());
                                        times.set(new RangeVar(range));
                                    }
                            );
                });
                return new PlantElement(m,prop, times.get());
            }
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            return null;
        }
        return null;
    }

    private static CustomPlant.State loadStateByJson(JsonNode stateBody){
        if(stateBody.isArray()){
            CustomPlant.State result = new CustomPlant.State();
            List<String> states = StreamSupport.stream(stateBody.spliterator(), false)
                    .map(JsonNode::asText)
                    .toList();
            if(states.contains("illegal")) result.isIllegal = true;
            return result;
        }else throw new IllegalArgumentException("Illegal state format: "+stateBody.textValue());
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
                CustomPlant result = new CustomPlant(name, domain, t_palette, true);
                for (JsonNode item:jsonNode){
                    t_palette.add(loadPlantElementByJson(item, result));
                }
                return result;
            }else if(jsonNode.isObject()){
                loadTemplate(jsonNode);
                Pair<Set<Material>, Material> foundations = loadFoundationsByJson(jsonNode);
                AtomicReference<String> icon = new AtomicReference<>();
                AtomicReference<PlantHabit> habit = new AtomicReference<>(PlantHabit.TERRESTRIAL);
                optionalGet(jsonNode,ICON_TAG).ifPresent(n->{
                    icon.set(n.textValue());
                });
                optionalGet(jsonNode, HABIT_TAG).ifPresent(n->{
                    habit.set(PlantHabit.of(n.textValue()));
                });
                if(jsonNode.has(DATA_TAG)){
                    ArrayList<PlantElement> t_palette = new ArrayList<>();
                    CustomPlant result = new CustomPlant(name,domain,t_palette,true);
                    if(jsonNode.has(SETTINGS_TAG)){
                        JsonNode settings = jsonNode.get(SETTINGS_TAG);
                        optionalGet(settings, GLOBAL_PROPERTIES_TAG).ifPresent( global_properties_data -> {
                                    global_properties_data.fields().forEachRemaining((item)->{
                                        List<String> values = new ArrayList<>();
                                        if(item.getValue().isArray()){
                                            item.getValue().forEach(v->values.add(makeString(v)));
                                        }else {
                                            values.add(makeString(item.getValue()));
                                        }
                                        result.setNamedVar(item.getKey().toString(),new SingleChoiceVar(values));
                                    });
                                });
                        optionalGet(settings,UI_PROPERTIES_TAG).ifPresent(ui_properties -> {
                            ui_properties.properties().forEach(item ->{
                                String varName = item.getKey();
                                JsonNode varBody = item.getValue();
                                UiVariable uiVariable = null;
                                switch (strictGet(varBody,UI_TYPE_TAG).textValue()){
                                    case "range" : {
                                        int maxValue = strictGet(varBody,UI_MAX_VALUE_TAG).asInt();
                                        int minValue = strictGet(varBody,UI_MIN_VALUE_TAG).asInt();
                                        Range range = new Range(maxValue,minValue);
                                        optionalGet(varBody,UI_DEFAULT_VALUE_TAG).ifPresent(node ->{
                                            Range defaultRange = asRange(node.textValue());
                                            range.high = defaultRange.high;
                                            range.low = defaultRange.low;
                                        });
                                        RangeItemVar rangeItemVar = new RangeItemVar(range,maxValue,minValue);
                                        uiVariable = rangeItemVar;
                                        result.setNamedVar(varName, rangeItemVar);
                                        result.setUiVar(varName, rangeItemVar);
                                        break;
                                    }
                                    case "percent":{
                                        AtomicReference<Float> value = new AtomicReference<>((float) 50);
                                        optionalGet(varBody,UI_DEFAULT_VALUE_TAG).ifPresent(
                                                p -> {
                                                    float temp;
                                                    try {
                                                        temp = asPercent(p.textValue());
                                                        value.set(temp);
                                                    }catch (IllegalArgumentException e){
                                                        e.printStackTrace();
                                                    }
                                                }
                                        );
                                        ProbabilityVar probabilityVar = new ProbabilityVar(value.get());
                                        uiVariable = probabilityVar;
                                        result.setNamedVar(varName, probabilityVar);
                                        result.setUiVar(varName, probabilityVar);
                                        break;
                                    }
                                    default:
                                        throw new IllegalArgumentException(
                                                "Unsupported UI type "+"\""
                                                +strictGet(varBody,UI_TYPE_TAG).textValue()
                                                +"\"");
                                }
                                UiVariable finalUiVariable = uiVariable;
                                optionalGet(varBody, DESCRIPTION_TAG).ifPresent(desc ->{
                                    finalUiVariable.setDesc(desc.textValue());
                                });
                            });
                        });
                    }
                    JsonNode data = strictGet(jsonNode,DATA_TAG);
                    if(data.isArray())
                        for (JsonNode item:data){
                            result.addElement(loadPlantElementByJson(item, result));
                        }
                    else {
                        result.addElement(loadPlantElementByJson(data, result));
                    }
                    AtomicReference<CustomPlant.State> state = new AtomicReference<>(new CustomPlant.State());
                    optionalGet(jsonNode,STATE_TAG).ifPresent(s->{
                        state.set(loadStateByJson(s));
                    });
                    result.setState(state.get());
                    result.setIcon(icon.get());
                    result.setHabit(habit.get());
                    return result.setFoundations(foundations);
                }else {
                    CustomPlant customPlant = new CustomPlant(name,domain,loadPlantElementByJson(jsonNode,null)).setFoundations(foundations);
                    customPlant.setIcon(icon.get());
                    customPlant.setHabit(habit.get());
                    return customPlant;
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

    private static Optional<String> tryLoadVar(String s){
        if (s.startsWith("#")){
            return Optional.of(s.substring(1));
        }
        return Optional.ofNullable(null);
    }

    public static boolean checkDecoderVersion(String s){return s.equals(DECODER_VERSION);}

}
