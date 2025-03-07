package org.demo.wpplugin.layers.editors;

import org.junit.jupiter.api.Test;

import javax.swing.*;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class GardeningLayerEditorTest {

    @Test
    public void testInit(){
        File file = new File("src/");
        System.out.println(file.getAbsoluteFile());
    }

}