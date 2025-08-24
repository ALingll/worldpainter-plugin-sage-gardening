package org.cti.wpplugin.myplants.decoder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.cti.wpplugin.myplants.decoder.TemplateProvider.*;
import static org.junit.jupiter.api.Assertions.*;

class TemplateProviderTest {

    @Test
    void test_getTemplate() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<String> sl = Arrays.stream(new String[]{"minecraft:sunflower"}).toList();
        System.out.println(
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(
                                getTemplate("HighPlant",sl)
                        )
        );
    }

    @Test
    void test_getTemplate2() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        List<String> sl = Arrays.stream(new String[]{"minecraft:aaa","minecraft:bbb"}).toList();
        System.out.println(
                mapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(
                                getTemplate("GrowingPlant",sl)
                        )
        );
    }

    @Test
    void test_parse() {
        String input = "GrowingPlant<minecraft:sunflower[half=lower],minecraft:sunflower[half=upper]>";
        TemplateProvider.TemplateData result = parseTemplate(input);
        System.out.println("Name: " + result.name());
        result.params().forEach(System.out::println);
    }
}