package com.terminal.sdk;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terminal.utils.ThemeManager;

/**
 * Базовый класс для создания плагинов Terminal.
 */
public abstract class BasePlugin implements TerminalPlugin {
    protected final Map<String, Command> commands = new HashMap<>();
    protected final Map<String, Object> config = new HashMap<>();
    protected final EventManager eventManager = EventManager.getInstance();
    protected boolean isEnabled = true;
    
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    public abstract String getName();
    public abstract String getVersion();
    public abstract String getDescription();
    public abstract String getAuthor();
    
    protected abstract void registerCommands();
    
    @Override
    public void initialize() {
        loadConfig();
        registerCommands();
    }
    
    @Override
    public void shutdown() {
        saveConfig();
    }
    
    @Override
    public Map<String, Command> getCommands() {
        return commands;
    }
    
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
    
    @Override
    public List<EventType> getSupportedEvents() {
        return Collections.emptyList();
    }
    
    protected void loadConfig() {
        try {
            String configPath = String.format("content/plugins/%s/config.json", getName().toLowerCase());
            if (Files.exists(Paths.get(configPath))) {
                String content = new String(Files.readAllBytes(Paths.get(configPath)));
                Map<String, Object> loadedConfig = gson.fromJson(content, Map.class);
                config.putAll(loadedConfig);
            } else {
                config.putAll(getDefaultConfig());
                saveConfig();
            }
        } catch (Exception e) {
            onError(e);
        }
    }
    
    protected void saveConfig() {
        try {
            String configPath = String.format("content/plugins/%s/config.json", getName().toLowerCase());
            Files.createDirectories(Paths.get(configPath).getParent());
            try (FileWriter writer = new FileWriter(configPath)) {
                gson.toJson(config, writer);
            }
        } catch (Exception e) {
            onError(e);
        }
    }
    
    @Override
    public Map<String, Object> getDefaultConfig() {
        return new HashMap<>();
    }
    
    @Override
    public void onError(Exception e) {
        System.err.println("Error in plugin " + getName() + ": " + e.getMessage());
    }
    
    protected void emitEvent(EventType type, String data) {
        eventManager.emit(new TerminalEvent(type, data));
    }
    
    protected void registerTheme(String name, String theme) {
        try {
            ThemeManager.getInstance().registerPluginTheme(name, theme);
        } catch (Exception e) {
            emitEvent(EventType.ERROR_OCCURRED, "Ошибка при регистрации темы " + name + ": " + e.getMessage());
        }
    }
} 