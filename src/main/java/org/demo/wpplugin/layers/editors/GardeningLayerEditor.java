package org.demo.wpplugin.layers.editors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.demo.wpplugin.layers.GardeningLayer;
import org.demo.wpplugin.layers.editors.gui.PlantItem;
import org.demo.wpplugin.myplants.CustomPlant;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.layers.AbstractLayerEditor;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;
import org.pepsoft.worldpainter.layers.plants.PlantLayer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;

public class GardeningLayerEditor extends AbstractLayerEditor<GardeningLayer> {
    private GardeningLayer tempLayer = new GardeningLayer() ;
    private Map<String, PlantItem> itemMap = new HashMap<>();

    private JTabbedPane tabbedPane;

    public GardeningLayerEditor(){
        initGUI();
        platform = context.getDimension().getWorld().getPlatform();
    }

    public GardeningLayerEditor(Platform platform) {
        this.layer = new GardeningLayer();
        initGUI();
        this.platform = platform;
    }

    private void initGUI() {
        tabbedPane = new JTabbedPane();
        setMinimumSize(new Dimension(200,300));

        loadJsonSettings("testPlant4.json");

        // 在窗口中加入标签页容器
        add(tabbedPane, BorderLayout.CENTER);

    }

    private void loadJsonSettings(String path) {
        JsonNode jsonNode = null;
        try {
            // 使用相对路径加载资源文件
            jsonNode = new ObjectMapper().readTree(getClass().getClassLoader().getResourceAsStream(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ObjectNode objectNode = (ObjectNode) jsonNode;

        Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = field.getKey();
            JsonNode fieldValue = field.getValue();
            addTab(fieldName, fieldValue);
        }
    }

    private void addTab(String title, JsonNode content) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        Iterator<Map.Entry<String, JsonNode>> fields = content.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String groupName = field.getKey();
            JsonNode itemList = field.getValue();
            Iterator<Map.Entry<String, JsonNode>> itemListFields = itemList.fields();
            while (itemListFields.hasNext()) {
                Map.Entry<String, JsonNode> afield = itemListFields.next();
                String plantName = afield.getKey();
                JsonNode itemNode = afield.getValue();
                CustomPlant customPlant = CustomPlant.createByJson(plantName, title+":"+groupName,itemNode);
                tempLayer.setPlant(customPlant,0);
                PlantItem plantItem = new PlantItem(title+":"+groupName+":"+plantName,plantName);
                itemMap.put(plantItem.getId(),plantItem);
                plantItem.addWeightChangedListener(e->{
                    tempLayer.setPlant(plantItem.getId(), ((PlantItem)e.getSource()).getValue());
                });
                panel.add(plantItem);
            }
        }
        tabbedPane.addTab(title, panel);

        // 添加关闭按钮
        JButton closeButton = new JButton("X");
        closeButton.setBounds(0, 0, 20, 20);
        closeButton.addActionListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex != -1) {
                tabbedPane.removeTabAt(selectedIndex);
            }
        });
        panel.add(closeButton);
    }



    @Override
    public GardeningLayer createLayer() {
        System.out.println("createLayer"+tempLayer);
        this.layer = tempLayer;
        return this.layer;
    }

    @Override
    public void commit() {
        layer = tempLayer;
        layer = new GardeningLayer();
        Map<CustomPlant, Integer> filteredMap = tempLayer.getPlantMap().entrySet().stream()
                .filter(entry -> entry.getValue() != 0) // 过滤值不为 0 的键值对
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        layer.setPlantMap(filteredMap);

        //System.out.println(layer);
    }

    @Override
    public void setLayer(GardeningLayer layer) {
        super.setLayer(layer);
        tempLayer = layer.clone();
        reset();
    }

    @Override
    public void reset() {
        // Reset the UI to the values currently in the layer
        //System.out.println("reset"+layer.getPlantMap());
        //System.out.println("reset"+itemMap);
        layer.getPlantMap().forEach((key,value)->{
            itemMap.get(key.getFullName()).setValue(value);
        });
    }

    @Override
    public ExporterSettings getSettings() {
        if (! isCommitAvailable()) {
            throw new IllegalStateException("Settings invalid or incomplete");
        }
        final GardeningLayer previewLayer = saveSettings(this.layer);
        return new ExporterSettings() {
            @Override
            public boolean isApplyEverywhere() {
                return false;
            }

            @Override
            public GardeningLayer getLayer() {
                return previewLayer;
            }

            @Override
            public ExporterSettings clone() {
                throw new UnsupportedOperationException("Not supported");
            }
        };
    }

    private GardeningLayer saveSettings(GardeningLayer layer) {
        if(layer == null)
            layer = createLayer();
        return layer;
    }

    @Override
    public boolean isCommitAvailable() {
        // Check whether the configuration currently selected by the user is valid and could be written to the layer
        return true;
    }
    private final Platform platform;
}