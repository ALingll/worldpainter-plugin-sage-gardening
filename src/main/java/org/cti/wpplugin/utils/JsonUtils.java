package org.cti.wpplugin.utils;

import com.fasterxml.jackson.databind.JsonNode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JsonUtils {

    public static String makeString(JsonNode jsonNode){
        if(jsonNode.isNull())
            return "null";
        if(jsonNode.isNumber())
            return String.valueOf(jsonNode.asInt());
        if(jsonNode.isTextual())
            return jsonNode.asText();
        if(jsonNode.isBoolean())
            return String.valueOf(jsonNode.asBoolean());
        if(jsonNode.isObject())
            return jsonNode.toString();
        return null;
    }

    public static String getFileNameWithoutExtension(Path path) {
        String fileName = path.getFileName().toString(); // 获取文件名（含后缀）
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }

    public static List<String> makeStringList(JsonNode jsonNode){
        List<String> result = new ArrayList<>();
        if(jsonNode.isTextual()){
            result.add(jsonNode.asText());
        } else if (jsonNode.isArray()) {
            jsonNode.forEach(item->result.add(makeString(item)));
        }
        return result;
    }

    public static JsonNode strictGet(JsonNode jsonNode, String key) throws IllegalArgumentException{
        if(!jsonNode.has(key))
            throw new IllegalArgumentException("JsonNode don't contains key: "+key);
        return jsonNode.get(key);
    }

    public static Optional<JsonNode> optionalGet(JsonNode jsonNode, String key){
        return Optional.ofNullable(jsonNode.get(key));
    }
}
