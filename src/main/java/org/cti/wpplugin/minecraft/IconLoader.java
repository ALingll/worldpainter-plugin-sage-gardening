package org.cti.wpplugin.minecraft;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.pepsoft.worldpainter.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static org.cti.wpplugin.utils.JsonUtils.optionalGet;

/**
 * @author: ALingll
 * @desc:
 * @create: 2025-08-20 17:29
 **/
public class IconLoader {
    private static final Logger logger = LoggerFactory.getLogger(IconLoader.class);
    private String RESOURCES_TAG = "resources";
    private static IconLoader INSTANCE;
    private static Map<String,File> resources = new HashMap<>();

    public static IconLoader getInstance(){
        if(INSTANCE == null)
            INSTANCE = new IconLoader();
        return INSTANCE;
    }

    private IconLoader(){
        File settingsDir = new File(Configuration.getConfigDir(), "plugin_data/gardening_layer/settings.json");
        if (!settingsDir.exists()) {
            boolean created = settingsDir.mkdirs();
            if (!created) {
                System.err.println("Can`t create: " + settingsDir.getAbsolutePath());
                return;
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        JsonNode settings;
        try {
            settings = mapper.readTree(settingsDir);   // 读取 JSON 并解析为 JsonNode
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        optionalGet(settings,RESOURCES_TAG).ifPresent(node->{
            node.propertyStream().forEach(entry->{
                String name = entry.getKey();
                File path = new File(entry.getValue().textValue());
                if(path.exists()){
                    resources.put(name,path);
                }else {
                    logger.info("Can't find resource: "+path);
                }
            });
        });
    }

    public Icon getIcon(String path) {
        if (path == null || !path.contains("/")) {
            logger.error("Invalid resource path: {}", path);
            return null;
        }

        // 解析顶层目录
        String topDir = path.substring(0, path.indexOf('/'));
        File jarFile = resources.get(topDir);
        if (jarFile == null) {
            logger.error("No jar registered for top-level dir: {}", topDir);
            return null;
        }

        // ===== 容错逻辑：尝试 .disabled =====
        File jarToUse = jarFile;
        if (!jarToUse.isFile()) {
            File disabled = new File(jarFile.getPath() + ".disabled");
            if (disabled.isFile()) {
                jarToUse = disabled;
                logger.warn("Jar {} not found, using disabled jar {}", jarFile, disabled);
            } else {
                logger.error("Jar file not found (also not found with .disabled): {}", jarFile);
                return null;
            }
        }

        // 从 jar 中读取资源
        String fullPath = "assets/" + path;
        try (JarFile jar = new JarFile(jarToUse)) {
            ZipEntry entry = jar.getEntry(fullPath);
            if (entry == null) {
                logger.error("Resource not found in jar {}: {}", jarToUse, fullPath);
                return null;
            }

            try (InputStream in = jar.getInputStream(entry)) {
                byte[] data = in.readAllBytes();
                return new ImageIcon(data);
            }
        } catch (IOException e) {
            logger.error("Error loading icon from jar {}: {}", jarToUse, path, e);
            return null;
        }
    }

}
