package org.demo.wpplugin.myplants;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
            CustomPlant customPlant = CustomPlant.createByJson("snake_plant","root_name:group_1",new ObjectMapper().readTree(jsonString));
            System.out.println(customPlant);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}