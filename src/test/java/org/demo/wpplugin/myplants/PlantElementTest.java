package org.demo.wpplugin.myplants;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.pepsoft.minecraft.Material;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import static org.demo.wpplugin.myplants.PlantElement.createByJson;
import static org.junit.jupiter.api.Assertions.*;

class PlantElementTest {

    @Test
    void testMaterial() {
        Material material1 = Material.get("verdantvibes:lobelia","aaa","bbb");
        System.out.println(material1);
        Material material2 = Material.get("verdantvibes:lobelia");
        System.out.println(material2);
    }

    @Test
    public void initByJson(){
        try {
            // 使用相对路径加载资源文件
            JsonNode jsonNode = new ObjectMapper().readTree(getClass().getClassLoader().getResourceAsStream("testPlant.json"));
            System.out.println(jsonNode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testJson() {
        JsonNode jsonNode = null;
        try {
            // 使用相对路径加载资源文件
            jsonNode = new ObjectMapper().readTree(getClass().getClassLoader().getResourceAsStream("testPlant.json"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ObjectNode objectNode = (ObjectNode) jsonNode;

        Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();
            System.out.println("Field Name: " + fieldName + ", Value: " + fieldValue);
        }
    }

    @Test
    void TestCreateByJson1() {
        try {
            String jsonString = "\"verdantvibes:lobelia\"";
            PlantElement plantElement = createByJson(new ObjectMapper().readTree(jsonString));
            System.out.println(plantElement);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void TestCreateByJson2() {
        try {
            String jsonString = "        {\n" +
                    "          \"block\":\"verdantvibes:candy_tuft\",\n" +
                    "          \"properties\": {\n" +
                    "            \"flower_amount\": [1,2,3,4],\n" +
                    "            \"facing\": [\"north\",\"south\",\"east\",\"west\"]\n" +
                    "          }\n" +
                    "        }";
            PlantElement plantElement = createByJson(new ObjectMapper().readTree(jsonString));
            System.out.println(plantElement);
            System.out.println(plantElement.getMaterial());
            System.out.println(plantElement.getMaterial());
            System.out.println(plantElement.getMaterial());
            System.out.println(plantElement.getMaterial());
            System.out.println(plantElement.getMaterial());
            System.out.println(plantElement.getMaterial());
            System.out.println(plantElement.getMaterial());
            System.out.println(plantElement.getMaterial());
            System.out.println(plantElement.getMaterial());
            System.out.println(plantElement.getMaterial());
            System.out.println(plantElement.getMaterial());
            System.out.println(plantElement.getMaterial());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}