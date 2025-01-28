package com.terminal.sdk.plugins;

import java.util.HashMap;
import java.util.Map;

/**
 * Класс конфигурации плагина
 */
public class PluginConfig {
    private String name;
    private String version;
    private String description;
    private String author;
    private String mainClass;
    private Map<String, Object> properties;
    
    public PluginConfig() {
        this.properties = new HashMap<>();
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getMainClass() {
        return mainClass;
    }
    
    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }
    
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    public void setProperty(String key, Object value) {
        this.properties.put(key, value);
    }
    
    public Object getProperty(String key) {
        return this.properties.get(key);
    }
} 