package org.cti.wpplugin.layers.editors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.cti.wpplugin.layers.GardeningLayer;
import org.cti.wpplugin.layers.editors.gui.PercentageItem;
import org.cti.wpplugin.layers.editors.gui.PlantEditor;
import org.cti.wpplugin.layers.editors.gui.WeightItem;
import org.cti.wpplugin.myplants.CustomPlant;
import org.cti.wpplugin.myplants.variable.ProbabilityVar;
import org.cti.wpplugin.myplants.variable.Variable;
import org.cti.wpplugin.utils.Pair;
import org.cti.wpplugin.utils.macro.StringMacroProvider;
import org.pepsoft.worldpainter.Platform;
import org.pepsoft.worldpainter.layers.AbstractLayerEditor;
import org.pepsoft.worldpainter.layers.exporters.ExporterSettings;
import org.pepsoft.worldpainter.Configuration;
import org.pepsoft.worldpainter.layers.renderers.PaintPicker;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
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
import static org.cti.wpplugin.utils.IconUtils.resizeIcon;
import static org.cti.wpplugin.utils.JsonUtils.*;
import static org.cti.wpplugin.utils.debug.DebugUtils.sayCalled;

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
    private JSpinner densityItem = new JSpinner(new SpinnerNumberModel(100, 0, 100, 1));
    private JComboBox<GardeningLayer.FOUNDATION> foundationjComboBox = new JComboBox<>(GardeningLayer.FOUNDATION.values());

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
        tabbedPane.setMinimumSize(new Dimension(10000,500));
        //tabbedPane.setMaximumSize(new Dimension(Integer.MAX_VALUE,Integer.MAX_VALUE));
        tabbedPane.setTabPlacement(JTabbedPane.LEFT);

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
        jComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE,jComboBox.getPreferredSize().height));
        jComboBox.setMinimumSize(new Dimension(12000,jComboBox.getPreferredSize().height));
        jComboBox.setPreferredSize(new Dimension(700,jComboBox.getPreferredSize().height));

        // 创建一个横向排列的面板
        JPanel inputRow = new JPanel();
        inputRow.setLayout(new BoxLayout(inputRow, BoxLayout.X_AXIS));
        // 第一个标签和文本框
        JLabel label1 = new JLabel("Name:");
        textField1.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        // 第二个标签和文本框
        JLabel label2 = new JLabel("Paint:");

        JLabel label3 = new JLabel("Density:");
        label3.setToolTipText("Sets the global probability of vegetation spawning, where 100% will generate a plant at every pixel.");
        JLabel label4 = new JLabel("%");
        densityItem.addChangeListener(e->{
            tempLayer.setDensity((Integer) densityItem.getValue());
            context.settingsChanged();
        });

        JLabel label5 = new JLabel("Foundation:");
        foundationjComboBox.setSelectedItem(GardeningLayer.FOUNDATION.STRICTLY);
        foundationjComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof GardeningLayer.FOUNDATION policy) {
                    setText(policy.toString());
                    setToolTipText(policy.description);
                }
                return c;
            }
        });
        foundationjComboBox.addActionListener(e->{
            GardeningLayer.FOUNDATION foundation = (GardeningLayer.FOUNDATION) foundationjComboBox.getSelectedItem();
            tempLayer.setCheckFoundation(foundation);
            context.settingsChanged();
        });

        // 给两个标签一点右边距，使间距更好看
        label1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        label2.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 5));
        label3.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 5));
        label4.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 5));
        label5.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 5));
        // 添加组件到横向面板
        inputRow.add(label1);
        inputRow.add(textField1);
        inputRow.add(label2);
        inputRow.add(paintPicker);
        inputRow.add(label3);
        inputRow.add(densityItem);
        inputRow.add(label4);
        inputRow.add(label5);
        inputRow.add(foundationjComboBox);
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
            List<Provide> provides;
            JsonNode providesObj = strictGet(metaData,PROVIDES_TAG);
            if(providesObj.isObject()){
                provides = new ArrayList<>();
                providesObj.propertyStream().forEach(e->{
                    String name = e.getKey();
                    Provide provide = new Provide(name);
                    provides.add(provide);
                    JsonNode body = e.getValue();
                    optionalGet(body,Provide.MC_VERSION_TAG).ifPresent(t->{
                        provide.mcVersion = t.textValue();
                    });
                    optionalGet(body,Provide.MODS_TAG).ifPresent(t->{
                        provide.mods.addAll(makeStringList(t));
                    });
                    optionalGet(body,Provide.STATE_TAG).ifPresent(t->{
                        provide.state = StringMacroProvider.INSTANCE.expand(t.textValue());
                    });
                });
            }else{
                provides = makeStringList(providesObj).stream().map(Provide::new).toList();
            }

            String encoder_v = makeString(strictGet(metaData,ENCODER_VERSION_TAG));
            if(!checkDecoderVersion(encoder_v)){
                JOptionPane.showMessageDialog(null,
                        "Encoder version "+encoder_v+" is not supported!",
                        "Warning",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            provides.forEach(provide -> {
                JsonNode provideBody = strictGet(jsonNode,provide.name);
                createTabPanel(provide.name,provideBody,provide).ifPresent(panel -> {
                    tabbedPane.addTab(provide.name,panel);
                    optionalGet(metaData,AUTHOR_TAG).ifPresent(author -> {
                        int index = tabbedPane.indexOfTab(provide.name);
                        if (index != -1) tabbedPane.setToolTipTextAt(index, "Author:"+author.asText());
                    });
                    tempLayer.putJsonNode(provide.name, metaData, provideBody);

                });
            });
        });
    }

    private Optional<JScrollPane> createTabPanel(String title, JsonNode content, Provide provide) {
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
                if (!tempLayer.getPlantMap().containsKey(customPlant)) {
                    tempLayer.setPlant(customPlant,0);
                }
                else {
                    GardeningLayer.PlantSetting setting = tempLayer.getSetting(customPlant);
                    GardeningLayer.PlantSetting newSetting = customPlant.enable(setting);
                    tempLayer.remove(customPlant);
                    tempLayer.setPlant(customPlant,newSetting);
                }

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
        //System.out.println("Old layer4:"+layer);
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

        JLabel titleLabel = new JLabel(provide.name);
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, titleLabel.getFont().getSize()));
        JPanel titleRow = new JPanel();
        titleRow.setLayout(new BoxLayout(titleRow, BoxLayout.Y_AXIS));
        JPanel row1 = new JPanel();
        row1.setLayout(new BoxLayout(row1, BoxLayout.X_AXIS));
        row1.add(titleLabel,BorderLayout.WEST);
        row1.add(Box.createHorizontalGlue());
        row1.add(closeButton,BorderLayout.EAST);
        row1.setAlignmentX(LEFT_ALIGNMENT);
        titleRow.add(row1);

        if(!provide.mcVersion.isEmpty()) {
            JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            JLabel versionLabel = new JLabel("Minecraft:"+provide.mcVersion);
            row2.add(versionLabel);
            row2.setAlignmentX(LEFT_ALIGNMENT);
            titleRow.add(Box.createVerticalStrut(5)); // 在控件之间增加竖直间距
            titleRow.add(row2);
        }

        if(!provide.state.isEmpty()){
            JLabel warningLabel = new JLabel("Warning!...",resizeIcon(UIManager.getIcon("OptionPane.warningIcon"),16,16),JLabel.LEFT);
            warningLabel.setHorizontalTextPosition(SwingConstants.LEFT);
            warningLabel.setToolTipText("<html><div style='width:400px;'>" + provide.state + "</div></html>");
            JPanel row3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            row3.add(warningLabel);
            row3.setAlignmentX(LEFT_ALIGNMENT);
            titleRow.add(Box.createVerticalStrut(5)); // 在控件之间增加竖直间距
            titleRow.add(row3);
        }

        if(!provide.mods.isEmpty()){
            JLabel modsLabel = new JLabel("Mods Required!");
            modsLabel.setToolTipText("These plants referred/required mods blocks which listed below:\n\t"
                    +String.join("\n\t", provide.mods));
            JPanel row4 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            row4.add(modsLabel);
            row4.setAlignmentX(LEFT_ALIGNMENT);
            titleRow.add(Box.createVerticalStrut(5)); // 在控件之间增加竖直间距
            titleRow.add(row4);
        }

        // 设置边框和内外边距
        Border outerMargin = BorderFactory.createEmptyBorder(5, 5, 5, 5);// 边框
        Border line = BorderFactory.createLineBorder(Color.GRAY, 1, true);// 内边距（内容与边框之间的间隔）
        Border innerPadding = BorderFactory.createEmptyBorder(5, 5, 5, 5);// 顺序：外边距 在最外层 → 边框 → 内边距
        titleRow.setBorder(
                BorderFactory.createCompoundBorder(
                        outerMargin, // 外层
                        BorderFactory.createCompoundBorder(
                                line,          // 中间
                                innerPadding   // 最里层
                        )
                )
        );

        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

        GridBagConstraints gbc1 = new GridBagConstraints();
        gbc1.fill = GridBagConstraints.HORIZONTAL; // 水平填充
        gbc1.weightx = 1; // 水平方向上伸展
        gbc1.weighty = 0; // 垂直方向上不伸展
        gbc1.gridwidth = GridBagConstraints.REMAINDER; // 占据剩余的所有列
        gbc1.anchor = GridBagConstraints.NORTH; // 组件顶部对齐
        gbc1.insets = new Insets(5, 1, 1, 5); // 设置组件之间的间距
        panel.add(titleRow, gbc1,0);

        // 创建带滚动条的 JScrollPane
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
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
        //sayCalled();

        Map<CustomPlant, GardeningLayer.PlantSetting> filteredMap = tempLayer.getPlantMap().entrySet().stream()
                .filter(entry -> entry.getValue().weight != 0) // 过滤值不为 0 的键值对
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        tempLayer.setPlantMap(filteredMap);

        saveSettings(layer);

        //System.out.println("Commit layer:"+layer);
        tempLayer = new GardeningLayer();
    }

    @Override
    public void setLayer(GardeningLayer layer) {
        //sayCalled();
        //System.out.println("Old layer:"+layer);
        super.setLayer(layer);

        tempLayer.copyFrom(layer);
        //System.out.println("New layer1:"+tempLayer);
        //System.out.println("Old layer1:"+layer);

        reset();
        //System.out.println("New layer2:"+tempLayer);
        //System.out.println("Old layer2:"+layer);
    }

    @Override
    public void reset() {
        //sayCalled();
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
            Provide provide1 = new Provide(provide);
            JsonNode providesObj = strictGet(metaData, PROVIDES_TAG);
            if(providesObj.isObject()){
                optionalGet(providesObj,provide).ifPresent(body->{
                    optionalGet(body,Provide.MC_VERSION_TAG).ifPresent(t->{
                        provide1.mcVersion = t.textValue();
                    });
                    optionalGet(body,Provide.MODS_TAG).ifPresent(t->{
                        provide1.mods.addAll(makeStringList(t));
                    });
                    optionalGet(body,Provide.STATE_TAG).ifPresent(t->{
                        provide1.state = StringMacroProvider.INSTANCE.expand(t.textValue());
                    });
                });
            }

            createTabPanel(provide,provideBody, provide1).ifPresent(panel -> {
                tabbedPane.addTab(provide,panel);
                optionalGet(metaData,AUTHOR_TAG).ifPresent(author -> {
                    int index = tabbedPane.indexOfTab(provide);
                    if (index != -1) tabbedPane.setToolTipTextAt(index, "Author:"+author.asText());
                });
            });

        });
        //System.out.println("New layer3:"+tempLayer);
        //System.out.println("Old layer3:"+layer);
        tempLayer.getPlantMap().forEach((key,value)->{
            itemMap.get(key.getFullName()).set(value);
            //System.out.println(value);
        });
        textField1.setText(layer.getName());
        paintPicker.setPaint(layer.getPaint());
        paintPicker.setOpacity(layer.getOpacity());
        densityItem.setValue(layer.getDensity());
        foundationjComboBox.setSelectedItem(layer.getCheckFoundation());
    }

    @Override
    public ExporterSettings getSettings() {
        //sayCalled();
        if (! isCommitAvailable()) {
            throw new IllegalStateException("Settings invalid or incomplete");
        }
        //System.out.println("A:"+layer);
        final GardeningLayer previewLayer = saveSettings(null);
        //System.out.println("B:"+layer);
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
        //sayCalled();
        if(layer==null)
            layer = createLayer();
        layer.setPaint(paintPicker.getPaint());
        layer.setOpacity(paintPicker.getOpacity());
        layer.setDensity((Integer) densityItem.getValue());
        layer.setName(textField1.getText());

        layer.copyFrom(tempLayer);

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

    private static class Provide {
        public static String STATE_TAG = "state";
        public static String MC_VERSION_TAG = "mc_ver";
        public static String MODS_TAG = "mods";

        public String name = "";
        public String state = "";
        public String mcVersion = "";
        public List<String> mods = new ArrayList<>();
        private Provide(String name){
            this.name = name;
        }

        public Provide(String name, String state, String mcVersion, List<String> mods) {
            this.name = name;
            this.state = state;
            this.mcVersion = mcVersion;
            this.mods = mods;
        }

        public Provide(String name, String state, String mcVersion, String... mods) {
            this.name = name;
            this.state = state;
            this.mcVersion = mcVersion;
            this.mods = Arrays.asList(mods);
        }
    }

}