package org.demo.wpplugin.utils;

import com.fasterxml.jackson.databind.JsonNode;

import java.nio.file.Path;

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

    public static String getFileNameWithoutExtension(Path path) {
        String fileName = path.getFileName().toString(); // 获取文件名（含后缀）
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
    }
}
