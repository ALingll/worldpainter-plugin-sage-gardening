package org.cti.wpplugin.myplants;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.pepsoft.worldpainter.biomeschemes.BiomeSchemeManager;

import static org.cti.wpplugin.myplants.decoder.PlantDecoder.loadPlantByJson;

class CustomPlantTest {

    @Test
    void createByJson_1() {
        try {
            String jsonString =
                    "{\n" +
                            "        \"settings\": {\n" +
                            "          \"global_properties\": {\n" +
                            "            \"afacing\": [\"north\",\"south\",\"east\",\"west\"]\n" +
                            "          }\n" +
                            "        },\n" +
                            "        \"data\": [\n" +
                            "          {\n" +
                            "            \"block\": \"verdantvibes:snake_plant\",\n" +
                            "            \"properties\": {\n" +
                            "              \"facing\": \"#afacing\"\n" +
                            "            }\n" +
                            "          }\n" +
                            "        ]\n" +
                            "      }";
            CustomPlant customPlant = loadPlantByJson("snake_plant","root_name:group_1",new ObjectMapper().readTree(jsonString));
            System.out.println(customPlant);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    void createByJson_2() {
        try {
            String jsonString = "{\n" +
                    "        \"settings\": {\n" +
                    "          \"global_properties\": {\n" +
                    "            \"a_facing\": [\"north\",\"south\",\"east\",\"west\"],\n" +
                    "            \"a_size\": [0,1,2]\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"data\": [\n" +
                    "          {\n" +
                    "            \"block\": \"verdantvibes:burnweed\",\n" +
                    "            \"properties\": {\n" +
                    "              \"facing\": \"#a_facing\",\n" +
                    "              \"size\": \"#a_size\",\n" +
                    "              \"half\": \"lower\"\n" +
                    "            }\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"block\": \"verdantvibes:burnweed\",\n" +
                    "            \"properties\": {\n" +
                    "              \"facing\": \"#a_facing\",\n" +
                    "              \"size\": \"#a_size\",\n" +
                    "              \"half\":\"upper\"\n" +
                    "            }\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }";
            CustomPlant customPlant = loadPlantByJson("burnweed","root_name:group_1",new ObjectMapper().readTree(jsonString));
            System.out.println(customPlant);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    void createByJson_3() {
        try {
            String jsonString = "{\n" +
                    "        \"settings\": {\n" +
                    "          \"ui_properties\": {\n" +
                    "            \"a_ui_number\": {\n" +
                    "              \"type\": \"range\",\n" +
                    "              \"max\": 10,\n" +
                    "              \"min\": 1,\n" +
                    "              \"default\": \"2~5\"\n" +
                    "            }\n" +
                    "          }\n" +
                    "        },\n" +
                    "        \"data\": [\n" +
                    "          {\n" +
                    "            \"block\": \"minecraft:cactus\",\n" +
                    "            \"times\": \"#a_ui_number\"\n" +
                    "          },\n" +
                    "          {\n" +
                    "            \"block\": \"minecraft:cactus_flower\",\n" +
                    "            \"times\": \"0~1\"\n" +
                    "          }\n" +
                    "        ]\n" +
                    "      }";
            CustomPlant customPlant = loadPlantByJson("burnweed","root_name:group_1",new ObjectMapper().readTree(jsonString));
            System.out.println(customPlant);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    void testMc(){
        //System.out.println();
    }
}