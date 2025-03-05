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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.demo.wpplugin.utils.JsonUtils.getFileNameWithoutExtension;

public class GardeningLayerEditor extends AbstractLayerEditor<GardeningLayer> {

    private static String DEFAULT_RESOURCES_DIR = "org/cti/wpplugin/gardening/";

    private GardeningLayer tempLayer = new GardeningLayer() ;
    private Map<String, PlantItem> itemMap = new HashMap<>();   //List of plant UI nodes

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
        tabbedPane = new JTabbedPane(JTabbedPane.LEFT, JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.setMinimumSize(new Dimension(1000,500));

        //loadJsonSettings("org/cti/wpplugin/gardening/testPlant4.json");

        // 获取资源目录的路径
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            throw new IllegalStateException("ClassLoader is null");
        }
        // 获取目录的 URI
        Path resourcePath = null;
        try {
            resourcePath = Paths.get(classLoader.getResource(DEFAULT_RESOURCES_DIR).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        // 递归扫描目录下的所有文件
        List<FileCombo> fileCombos = new ArrayList<>();
        try (java.util.stream.Stream<Path> stream = Files.walk(resourcePath)) {
            stream.filter(Files::isRegularFile)
                    .collect(Collectors.toList())
                    .forEach(path->fileCombos.add(new FileCombo(path)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        JComboBox<FileCombo> jComboBox = new JComboBox<>(fileCombos.toArray(new FileCombo[0]));
        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadJsonSettings(((FileCombo)jComboBox.getSelectedItem()).path);
            }
        });
        jComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE,50));
        jComboBox.setMinimumSize(new Dimension(10000,50));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(jComboBox);
        add(tabbedPane);

    }

    private void loadJsonSettings(Path path) {
        JsonNode jsonNode = null;
        try {
            // 使用相对路径加载资源文件
            jsonNode = new ObjectMapper().readTree(Files.newInputStream(path));
            tempLayer.putJsonNode(getFileNameWithoutExtension(path),jsonNode);
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
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(title)) {
                JOptionPane.showMessageDialog(null,
                        "Pane \""+title+"\" has already existed!",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
        }
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
                if (!tempLayer.getPlantMap().containsKey(customPlant))tempLayer.setPlant(customPlant,0);
                PlantItem plantItem = new PlantItem(title+":"+groupName+":"+plantName,plantName);
                itemMap.put(plantItem.getId(),plantItem);
                plantItem.addWeightChangedListener(e->{
                    tempLayer.setPlant(plantItem.getId(), ((PlantItem)e.getSource()).getValue());
                });
                plantItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
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
        System.out.println("create layer");
        return tempLayer;
    }

    @Override
    public void commit() {
        System.out.println("commit layer");
        System.out.println("tempLayer"+tempLayer);
        layer = new GardeningLayer();
        Map<CustomPlant, Integer> filteredMap = tempLayer.getPlantMap().entrySet().stream()
                .filter(entry -> entry.getValue() != 0) // 过滤值不为 0 的键值对
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        layer.setPlantMap(filteredMap);
        layer.setUsedJsons(tempLayer.getUsedJsons());
        System.out.println("commitedLayer"+layer);
        tempLayer = null;
        //System.out.println(layer);
    }

    @Override
    public void setLayer(GardeningLayer layer) {
        System.out.println("set layer:"+layer);
        super.setLayer(layer);
        tempLayer = layer.clone();
        reset();
    }

    @Override
    public void reset() {
        // Reset the UI to the values currently in the layer
        System.out.println("reset layer");
        tempLayer.getUsedJsons().forEach((key,value)->{
            value.fields().forEachRemaining(field->{
                addTab(field.getKey(),field.getValue());
            });
        });
        tempLayer.getPlantMap().forEach((key,value)->{
            itemMap.get(key.getFullName()).setValue(value);
        });
    }

    @Override
    public ExporterSettings getSettings() {
        if (! isCommitAvailable()) {
            throw new IllegalStateException("Settings invalid or incomplete");
        }
        final GardeningLayer previewLayer = saveSettings(this.layer);
        System.out.println("getSettings:"+this.layer);
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

    private class FileCombo{
        public String name;
        public Path path;

        public FileCombo(String name, Path path) {
            this.name = name;
            this.path = path;
        }

        public FileCombo(Path path){
            this.path = path;
            this.name = getFileNameWithoutExtension(path);
        }

        @Override
        public String toString() {
            return name;
        }
    }
}