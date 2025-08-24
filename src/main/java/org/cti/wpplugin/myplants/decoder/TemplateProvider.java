package org.cti.wpplugin.myplants.decoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cti.wpplugin.layers.editors.GardeningLayerEditor;
import org.cti.wpplugin.minecraft.IconLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.cti.wpplugin.myplants.decoder.PlantDecoder.TEMPLATE_TAG;
import static org.cti.wpplugin.utils.JsonUtils.*;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-24 02:56
 **/
public class TemplateProvider {

    public static final String TEMPLATE_REQUIRES_TAG = "requires";
    public static final String TEMPLATE_PARAMS_NUM_TAG = "params_num";

    private static final Logger logger = LoggerFactory.getLogger(TemplateProvider.class);
    public static final String templatePath = "org/cti/wpplugin/gardening/templates/";
    public static JsonNode getTemplate(String templateName, List<String> params) throws IllegalArgumentException{
        String s = "";
        TemplateRequires requires = null;

        ObjectMapper mapper = new ObjectMapper();

        String file = templatePath+templateName+".json";
        InputStream is = GardeningLayerEditor.class.getClassLoader().getResourceAsStream(file);
        if (is == null) {
            throw new IllegalArgumentException("Can't find "+file);
        }

        try {
            JsonNode root = mapper.readTree(is);
            requires = getRequires(root);
            s = root.toString();
            if (requires.paramsNum != params.size())
                throw new IllegalArgumentException("Template "+templateName+" requires "+ requires.paramsNum +" params but get "+ params.size()+".");
        } catch (IOException e) {
            throw new IllegalArgumentException("No such template:"+templateName);
        }

        for(int i = 0; i<params.size(); i++){
            s = s.replaceAll("@"+i,params.get(i));
        }
        JsonNode node = null;
        try {
            node = mapper.readTree(s);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return node;
    }

    public static void loadTemplate(JsonNode jsonNode){
        optionalGet(jsonNode, TEMPLATE_TAG).ifPresent(t->{
            String templateStr = t.textValue();
            TemplateData templateData = parseTemplate(templateStr);
            try {
                JsonNode template = getTemplate(templateData.name, templateData.params);
                mergeTo(jsonNode, template);
            }catch (IllegalArgumentException e){
                logger.error(e.getMessage());
                return;
            }
        });
    }

    private static TemplateRequires getRequires(JsonNode jsonNode){
        JsonNode requiresBody = strictGet(jsonNode, TEMPLATE_REQUIRES_TAG);
        int paramsNum = strictGet(requiresBody, TEMPLATE_PARAMS_NUM_TAG).asInt();
        return new TemplateRequires(paramsNum);
    }

    @Deprecated
    private static TemplateData parseTemplate_old(String input) {
        int lt = input.indexOf('<');
        int gt = input.lastIndexOf('>');
        if (lt == -1 || gt == -1 || gt < lt) {
            throw new IllegalArgumentException("Template Format error: " + input);
        }
        String name = input.substring(0, lt).trim();
        String paramPart = input.substring(lt + 1, gt).trim();
        List<String> params = new ArrayList<>();
        if (!paramPart.isEmpty()) {
            for (String p : paramPart.split(",")) {
                params.add(p.trim());
            }
        }
        return new TemplateData(name, params);
    }

    public static TemplateData parseTemplate(String input) {
        int lt = input.indexOf('<');
        int gt = input.lastIndexOf('>');
        if (lt == -1 || gt == -1 || gt < lt) {
            throw new IllegalArgumentException("Template Format error: " + input);
        }

        String name = input.substring(0, lt).trim();
        String paramPart = input.substring(lt + 1, gt).trim();

        List<String> params = new ArrayList<>();
        // 正则：匹配 param 或 param[...]，非贪婪
        Pattern pattern = Pattern.compile("[^,\\[\\]]+(\\[[^\\]]*\\])?");
        Matcher matcher = pattern.matcher(paramPart);
        while (matcher.find()) {
            params.add(matcher.group().trim());
        }

        return new TemplateData(name, params);
    }

    public record TemplateData(String name, List<String> params) {
        @Override
            public String toString() {
                return "ParsedResult{name='" + name + "', params=" + params + '}';
            }
    }

    public record TemplateRequires(int paramsNum) {}
}
