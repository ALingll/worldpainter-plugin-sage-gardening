package org.cti.wpplugin.layers.editors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cti.wpplugin.layers.GardeningLayer;
import org.cti.wpplugin.layers.editors.gui.PlantItem;
import org.cti.wpplugin.myplants.CustomPlant;
import org.cti.wpplugin.utils.EnvironmentChecker;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.layers.AbstractLayerEditor;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.cti.wpplugin.myplants.decoder.PlantDecoder.*;
import static org.cti.wpplugin.utils.JsonUtils.*;

public class GardeningLayerEditor extends AbstractLayerEditor<GardeningLayer> {

    private static String DEFAULT_RESOURCES_DIR = "org/cti/wpplugin/gardening/";
    private static String DEFAULT_RESOURCES_TEST_DIR = "org/cti/wpplugin/gardening/test/";
    private static String DEFAULT_USER_RESOURCES_DIR = "sage-gardening-data/";

    private GardeningLayer tempLayer = new GardeningLayer() ;
    private Map<String, PlantItem> itemMap = new HashMap<>();   //List of plant UI nodes
    private JTabbedPane tabbedPane;

    public GardeningLayerEditor(){
        initGUI();
        platform = null;
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
        List<FileCombo> fileCombos = new ArrayList<>();
        try (java.util.stream.Stream<Path> stream = Files.list(resourcePath)) { // 只扫描当前目录
            stream.filter(Files::isRegularFile)  // 仅保留文件，不包含子目录
                    .forEach(path -> {
                        fileCombos.add(new FileCombo(path));
                        System.out.println("resourcePath:"+path);
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if(!EnvironmentChecker.isRunInJar()){
            Path testPath = null;
            try {
                testPath = Paths.get(classLoader.getResource(DEFAULT_RESOURCES_TEST_DIR).toURI());
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
            try (java.util.stream.Stream<Path> stream = Files.list(testPath)) { // 只扫描当前目录
                stream.filter(Files::isRegularFile)  // 仅保留文件，不包含子目录
                        .forEach(path -> fileCombos.add(new FileCombo(path)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        Path userPath = null;
        try {
            URL resource = classLoader.getResource(DEFAULT_USER_RESOURCES_DIR);
            if(resource!=null){
                userPath = Paths.get(resource.toURI());
                try (java.util.stream.Stream<Path> stream = Files.list(userPath)) { // 只扫描当前目录
                    stream.filter(Files::isRegularFile)  // 仅保留文件，不包含子目录
                            .forEach(path -> fileCombos.add(new FileCombo(path)));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (URISyntaxException e) {
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
        JsonNode jsonNode;
        try {
            // 使用相对路径加载资源文件
            jsonNode = new ObjectMapper().readTree(Files.newInputStream(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int i = 0;

        optionalGet(jsonNode,META_DATA_TAG).ifPresent(metaData->{
            List<String> provides = makeStringList(strictGet(metaData,PROVIDES_TAG));
            String encoder_v = makeString(strictGet(metaData,ENCODER_VERSION_TAG));
            if(!checkDecoderVersion(encoder_v)){
                JOptionPane.showMessageDialog(null,
                        "Encoder version "+encoder_v+" is not supported!",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            provides.forEach(provide -> {
                JsonNode provideBody = strictGet(jsonNode,provide);
                createTabPanel(provide,provideBody).ifPresent(panel -> {
                    tabbedPane.addTab(provide,panel);
                    optionalGet(metaData,AUTHOR_TAG).ifPresent(author -> {
                        int index = tabbedPane.indexOfTab(provide);
                        if (index != -1) tabbedPane.setToolTipTextAt(index, "Author:"+author.asText());
                    });
                    tempLayer.putJsonNode(provide, metaData, provideBody);
                });
            });
        });
    }

    private Optional<JPanel> createTabPanel(String title, JsonNode content) {
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals(title)) {
                JOptionPane.showMessageDialog(null,
                        "Pane \""+title+"\" has already existed!",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return Optional.ofNullable(null);
            }
        }
        //JPanel panel = new JPanel();
        JPanel panel = new JPanel(new GridBagLayout());
        //panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; // 水平填充
        gbc.weightx = 1; // 水平方向上伸展
        gbc.weighty = 0; // 垂直方向上不伸展
        gbc.gridwidth = GridBagConstraints.REMAINDER; // 占据剩余的所有列
        gbc.anchor = GridBagConstraints.NORTH; // 组件顶部对齐
        gbc.insets = new Insets(5, 1, 1, 5); // 设置组件之间的间距

        Iterator<Map.Entry<String, JsonNode>> fields = content.fields();
        content.fields().forEachRemaining(field->{
            String groupName = field.getKey();
            panel.add(new JSeparator(),gbc);
            JLabel groupLabel = new JLabel(groupName);
            groupLabel.setFont(new Font(groupLabel.getFont().getName(), Font.BOLD, groupLabel.getFont().getSize()));
            panel.add(groupLabel,gbc);
            JsonNode itemList = field.getValue();
            Iterator<Map.Entry<String, JsonNode>> itemListFields = itemList.fields();
            while (itemListFields.hasNext()) {
                Map.Entry<String, JsonNode> afield = itemListFields.next();
                String plantName = afield.getKey();
                JsonNode itemNode = afield.getValue();
                CustomPlant customPlant = loadPlantByJson(plantName, title+":"+groupName,itemNode);
                if (!tempLayer.getPlantMap().containsKey(customPlant))tempLayer.setPlant(customPlant,0);
                PlantItem plantItem = new PlantItem(title+":"+groupName+":"+plantName,plantName);
                itemMap.put(plantItem.getId(),plantItem);
                plantItem.addWeightChangedListener(e->{
                    tempLayer.setPlant(plantItem.getId(), ((PlantItem)e.getSource()).getValue());
                    computeAllPercent();
                });
                plantItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
                panel.add(plantItem,gbc);
                //panel.add(new JLabel(plantName),gbc);
            }
        });
        gbc.weighty = 1; // 让这个组件填充剩余空间
        panel.add(new JPanel(), gbc); // 空白面板，作为占位符

        // 添加关闭按钮
        JButton closeButton = new JButton("delete");
        closeButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(
                    null,
                    "Are you sure you want to proceed? \n This will clear all configuration items in this page", // Message
                    "Confirmation", // Title
                    JOptionPane.YES_NO_OPTION // 仅显示 "是" 和 "否"
            );

            if (result != JOptionPane.YES_OPTION)
                return;
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex != -1) {
                String selectedTitle = tabbedPane.getTitleAt(selectedIndex);
                for(Component component:((Container)tabbedPane.getComponentAt(selectedIndex)).getComponents()){
                    if(component instanceof PlantItem){
                        String id = ((PlantItem) component).getId();
                        itemMap.remove(id);
                        tempLayer.getPlantMap().entrySet().removeIf(entry -> entry.getKey().getFullName().equals(id));
                    }
                }
                tempLayer.getUsedJsons().remove(selectedTitle);
                tabbedPane.removeTabAt(selectedIndex);
            }
            computeAllPercent();
        });
        panel.add(closeButton,0);

        return Optional.ofNullable(panel);
    }

    private void computeAllPercent(){
        AtomicInteger total = new AtomicInteger();
        itemMap.values().forEach(value -> total.addAndGet(value.getValue()));
        itemMap.values().forEach(value->{
            if (value.getValue() == 0) {
                value.setPercentage(0);
            } else {
                value.setPercentage((float) value.getValue()*100 / (float)total.get());
            }
        });
    }


    @Override
    public GardeningLayer createLayer() {
        System.out.println("create layer");
        return tempLayer;
    }

    @Override
    public void commit() {
        Map<CustomPlant, Integer> filteredMap = tempLayer.getPlantMap().entrySet().stream()
                .filter(entry -> entry.getValue() != 0) // 过滤值不为 0 的键值对
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        layer.setPlantMap(filteredMap);
        layer.setUsedJsons(tempLayer.getUsedJsons());
        tempLayer = null;
    }

    @Override
    public GardeningLayer getLayer(){
        return super.getLayer();
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
        tempLayer.getUsedJsons().forEach((provide,value)->{
            JsonNode metaData = value.first;
            JsonNode provideBody = value.second;

            String encoder_v = makeString(strictGet(metaData,ENCODER_VERSION_TAG));
            if(!checkDecoderVersion(encoder_v)){
                JOptionPane.showMessageDialog(null,
                        "Encoder version "+encoder_v+" is not supported!",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            createTabPanel(provide,provideBody).ifPresent(panel -> {
                tabbedPane.addTab(provide,panel);
                optionalGet(metaData,AUTHOR_TAG).ifPresent(author -> {
                    int index = tabbedPane.indexOfTab(provide);
                    if (index != -1) tabbedPane.setToolTipTextAt(index, "Author:"+author.asText());
                });
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