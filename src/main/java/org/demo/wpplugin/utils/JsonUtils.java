package org.demo.wpplugin.utils;

import com.fasterxml.jackson.databind.JsonNode;

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
        return null;
    }
}
