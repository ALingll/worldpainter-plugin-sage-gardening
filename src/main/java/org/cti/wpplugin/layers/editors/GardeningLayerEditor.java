package org.cti.wpplugin.layers.editors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cti.wpplugin.layers.GardeningLayer;
import org.cti.wpplugin.layers.editors.gui.PlantEditor;
import org.cti.wpplugin.layers.editors.gui.WeightItem;
import org.cti.wpplugin.myplants.CustomPlant;
import org.cti.wpplugin.utils.Pair;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.layers.AbstractLayerEditor;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;
import org.pepsoft.worldpainter.Configuration;
import org.pepsoft.worldpainter.layers.renderers.PaintPicker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.cti.wpplugin.myplants.decoder.PlantDecoder.*;
import static org.cti.wpplugin.utils.JsonUtils.*;

public class GardeningLayerEditor extends AbstractLayerEditor<GardeningLayer> {

    private static String DEFAULT_RESOURCES_DIR = "org/cti/wpplugin/gardening/";
    private static String DEFAULT_RESOURCES_SETTINGS_DIR = "org/cti/wpplugin/gardening/settings.json";
    private static String DEFAULT_RESOURCES_TEST_DIR = "org/cti/wpplugin/gardening/test/";
    private static String DEFAULT_RESOURCES_Internal_DIR = "org/cti/wpplugin/gardening/internal/";
    private static String DEFAULT_USER_RESOURCES_DIR = "sage-gardening-data/";

    private GardeningLayer tempLayer = new GardeningLayer() ;
    private Map<String, PlantEditor> itemMap = new HashMap<>();   //List of plb ant UI nodes
    private JTabbedPane tabbedPane;
    private JTextField textField1 = new JTextField(10); // 设置列宽
    private PaintPicker paintPicker = new PaintPicker();

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

        List<FileCombo> fileCombos = new ArrayList<>();

        //get internal resources
        InputStream inputStream =
                GardeningLayerEditor.class
                .getClassLoader()
                .getResourceAsStream(DEFAULT_RESOURCES_SETTINGS_DIR);
        if (inputStream == null) {
            System.out.println("Can't find "+DEFAULT_RESOURCES_SETTINGS_DIR);
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode settings;
        try {
            settings = mapper.readTree(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        settings.get("internal").forEach(item ->{
            fileCombos.add(new FileCombo(DEFAULT_RESOURCES_Internal_DIR+item.asText()+".json",
                    FileCombo.Scope.INTERNAL_DEFINED));
        });

        //get user defined settings
        File gardeningLayerDir = new File(Configuration.getConfigDir(), "plugin_data/gardening_layer");
        if (!gardeningLayerDir.exists()) {
            boolean created = gardeningLayerDir.mkdirs();
            if (!created) {
                System.err.println("Can`t create: " + gardeningLayerDir.getAbsolutePath());
                return;
            }
        }
        try (Stream<Path> paths = Files.walk(gardeningLayerDir.toPath())) {
            paths.filter(Files::isRegularFile)
                    .forEach(item -> fileCombos.add(new FileCombo(item.toString(), FileCombo.Scope.USER_DEFINED)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        JComboBox<FileCombo> jComboBox = new JComboBox<>(fileCombos.toArray(new FileCombo[0]));
        jComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileCombo fileCombo=(FileCombo)jComboBox.getSelectedItem();
                switch (fileCombo.scope){
                    case USER_DEFINED -> {
                        ObjectMapper mapper = new ObjectMapper();
                        File file = new File(fileCombo.path); // 用字符串路径构造文件
                        JsonNode rootNode;
                        try {
                            rootNode = mapper.readTree(file);   // 读取 JSON 并解析为 JsonNode
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        loadJsonSettings(rootNode);
                    }
                    case INTERNAL_DEFINED -> {
                        InputStream is = GardeningLayerEditor.class.getClassLoader().getResourceAsStream(fileCombo.path);
                        if (is == null) {
                            throw new IllegalArgumentException("Can't find "+fileCombo.path);
                        }
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode rootNode;
                        try {
                            rootNode = mapper.readTree(is);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                        loadJsonSettings(rootNode);
                    }
                }
            }
        });
        jComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE,50));
        jComboBox.setMinimumSize(new Dimension(10000,50));

        // 创建一个横向排列的面板
        JPanel inputRow = new JPanel();
        inputRow.setLayout(new BoxLayout(inputRow, BoxLayout.X_AXIS));
        // 第一个标签和文本框
        JLabel label1 = new JLabel("Name:");
        textField1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        // 第二个标签和文本框
        JLabel label2 = new JLabel("Paint:");
        // 给两个标签一点右边距，使间距更好看
        label1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        label2.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 5));
        // 添加组件到横向面板
        inputRow.add(label1);
        inputRow.add(textField1);
        inputRow.add(label2);
        inputRow.add(paintPicker);
        // 设置面板最大宽度撑满
        inputRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));


        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(jComboBox);
        add(tabbedPane);
        add(inputRow);

    }

    private void loadJsonSettings(JsonNode jsonNode) {
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

    private Optional<JScrollPane> createTabPanel(String title, JsonNode content) {
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

        //System.out.println(tempLayer.getPlantMap());

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
                if (!tempLayer.getPlantMap().containsKey(customPlant)) tempLayer.setPlant(customPlant,0);

//                WeightItem weightItem = new WeightItem(title+":"+groupName+":"+plantName,plantName);
//                itemMap.put(weightItem.getId(), weightItem);
//                weightItem.addWeightChangedListener(e->{
//                    tempLayer.setPlant(weightItem.getId(), ((WeightItem)e.getSource()).getValue());
//                    context.settingsChanged();
//                    computeAllPercent();
//                });
//                weightItem.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

                PlantEditor plantEditor = new PlantEditor(title+":"+groupName+":"+plantName, plantName, customPlant);
                plantEditor.addWeightChangedListener(e->{
                    tempLayer.setPlant(plantEditor.getId(), ((WeightItem)e.getSource()).getValue());
                    context.settingsChanged();
                    computeAllPercent();
                });
                plantEditor.addValueChangeListener(e->{
                    Pair<String,Object> value = (Pair<String,Object>) e.getNewValue();
                    tempLayer.setPlant(plantEditor.getId(), value.first, value.second);
                    context.settingsChanged();
                });
                itemMap.put(plantEditor.getId(), plantEditor);

                panel.add(plantEditor,gbc);
                //panel.add(new JLabel(plantName),gbc);
            }
        });
        gbc.weighty = 1; // 让这个组件填充剩余空间
        panel.add(new JPanel(), gbc); // 空白面板，作为占位符

        // TODO 添加关闭按钮
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
                JPanel plantPanel = (JPanel) ((JScrollPane) tabbedPane.getComponentAt(selectedIndex)).getViewport().getView();
                for(Component component: plantPanel.getComponents()){
                    if(component instanceof PlantEditor){
                        String id = ((PlantEditor) component).getId();
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

        // 创建带滚动条的 JScrollPane
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // 始终显示垂直滚动条
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        return Optional.ofNullable(scrollPane);
    }

    private void computeAllPercent(){
        AtomicInteger total = new AtomicInteger();
        itemMap.values().forEach(value -> total.addAndGet(value.getWeight()));
        itemMap.values().forEach(value->{
            if (value.getWeight() == 0) {
                value.setPercentage(0);
            } else {
                value.setPercentage((float) value.getWeight()*100 / (float)total.get());
            }
        });
    }


    @Override
    public GardeningLayer createLayer() {
        return new GardeningLayer();
    }

    @Override
    public void commit() {
        Map<CustomPlant, GardeningLayer.PlantSetting> filteredMap = tempLayer.getPlantMap().entrySet().stream()
                .filter(entry -> entry.getValue().weight != 0) // 过滤值不为 0 的键值对
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        layer.setPlantMap(filteredMap);
        layer.setUsedJsons(tempLayer.getUsedJsons());
        saveSettings(layer);
    }

    @Override
    public GardeningLayer getLayer(){
        return super.getLayer();
    }

    @Override
    public void setLayer(GardeningLayer layer) {
        super.setLayer(layer);
        if(layer==null)
            tempLayer = createLayer();
        else
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
            itemMap.get(key.getFullName()).set(value);
            //System.out.println(itemMap);
        });
        textField1.setText(layer.getName());
        paintPicker.setPaint(layer.getPaint());
        paintPicker.setOpacity(layer.getOpacity());
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
        if(layer==null)
            layer = createLayer();
        layer.setPaint(paintPicker.getPaint());
        layer.setOpacity(paintPicker.getOpacity());
        layer.setName(textField1.getText());
        return layer;
    }

    @Override
    public boolean isCommitAvailable() {
        // Check whether the configuration currently selected by the user is valid and could be written to the layer
        return true;
    }
    private final Platform platform;

    private class FileCombo{
        public enum Scope{
            INTERNAL_DEFINED,
            USER_DEFINED
        }
        public String name;
        public String path;
        public Scope scope;

        public FileCombo(String name, String path, Scope scope) {
            this.name = name;
            this.path = path;
            this.scope = scope;
        }

        public FileCombo(String path, Scope scope){
            this.path = path;
            this.name = getFileNameWithoutExtension(path);
            this.scope = scope;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}