package com.terminal.sdk.plugins;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import com.terminal.sdk.core.Command;
import com.terminal.sdk.core.CommandCategory;
import com.terminal.sdk.core.CommandInfo;
import com.terminal.sdk.events.EventManager;
import com.terminal.sdk.events.EventType;
import com.terminal.sdk.events.TerminalEvent;
import com.terminal.sdk.system.SystemFacade;

/**
 * Фасад для управления плагинами Terminal.
 * Предоставляет централизованный интерфейс для работы с плагинами.
 */
public class PluginManager {
    private static PluginManager instance;
    private final Map<String, TerminalPlugin> plugins;
    private final Map<String, CommandInfo> pluginCommands;
    private final Map<String, Map<String, Object>> pluginConfigs;
    private final String pluginsDirectory;
    private final SystemFacade systemFacade;
    private static final String CLASS_NAME = PluginManager.class.getSimpleName();
    
    private PluginManager() {
        this.plugins = new HashMap<>();
        this.pluginCommands = new HashMap<>();
        this.pluginConfigs = new HashMap<>();
        this.pluginsDirectory = "content/plugins";
        this.systemFacade = SystemFacade.getInstance();
        createPluginsDirectory();
    }
    
    public static PluginManager getInstance() {
        if (instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }
    
    private void createPluginsDirectory() {
        try {
            File dir = new File(pluginsDirectory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        } catch (Exception e) {
            systemFacade.logError(CLASS_NAME, "Ошибка при создании директории плагинов: " + e.getMessage());
        }
    }
    
    public void loadPlugins() {
        File dir = new File(pluginsDirectory);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        
        File[] files = dir.listFiles((d, name) -> name.endsWith(".jar"));
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            try {
                loadPlugin(file);
            } catch (Exception e) {
                systemFacade.logError(CLASS_NAME, "Ошибка при загрузке плагина " + file.getName() + ": " + e.getMessage());
            }
        }
    }
    
    private void loadPlugin(File jarFile) throws Exception {
        try (JarFile jar = new JarFile(jarFile);
             URLClassLoader loader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()})) {
            
            jar.stream().forEach(entry -> {
                String name = entry.getName();
                if (name.endsWith(".class") && !name.contains("$")) {
                    try {
                        String className = name.replace("/", ".").replace(".class", "");
                        Class<?> clazz = loader.loadClass(className);
                        
                        if (TerminalPlugin.class.isAssignableFrom(clazz) && !clazz.isInterface()) {
                            TerminalPlugin plugin = (TerminalPlugin) clazz.getDeclaredConstructor().newInstance();
                            registerPlugin(plugin);
                        }
                    } catch (Exception e) {
                        systemFacade.logError(CLASS_NAME, "Ошибка при загрузке класса: " + e.getMessage());
                    }
                }
            });
        }
    }
    
    private void registerPlugin(TerminalPlugin plugin) {
        if (!plugin.isEnabled()) {
            systemFacade.logWarning(CLASS_NAME, "Плагин " + plugin.getName() + " отключен, пропускаем...");
            return;
        }
        
        List<String> missingDependencies = checkDependencies(plugin);
        if (!missingDependencies.isEmpty()) {
            systemFacade.logError(CLASS_NAME, "Не найдены зависимости для плагина " + plugin.getName() + ": " + missingDependencies);
            return;
        }
        
        plugins.put(plugin.getName(), plugin);
        pluginConfigs.put(plugin.getName(), new HashMap<>(plugin.getDefaultConfig()));
        plugin.initialize();
        
        Map<String, Command> commands = plugin.getCommands();
        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            CommandInfo commandInfo = new CommandInfo(entry.getKey(), entry.getValue().getDescription(), CommandCategory.PLUGINS.name(), entry.getValue());
            pluginCommands.put(entry.getKey(), commandInfo);
            systemFacade.logInfo(CLASS_NAME, "Зарегистрирована новая команда: " + entry.getKey());
        }
        
        EventManager.getInstance().emit(new TerminalEvent(EventType.STATE_CHANGED, "Plugin loaded: " + plugin.getName()));
    }
    
    private List<String> checkDependencies(TerminalPlugin plugin) {
        List<String> missingDependencies = new ArrayList<>();
        for (String dependency : plugin.getDependencies()) {
            if (!plugins.containsKey(dependency)) {
                missingDependencies.add(dependency);
            }
        }
        return missingDependencies;
    }
    
    public void updatePluginConfig(String pluginName, Map<String, Object> newConfig) {
        TerminalPlugin plugin = plugins.get(pluginName);
        if (plugin != null) {
            Map<String, Object> currentConfig = pluginConfigs.get(pluginName);
            currentConfig.putAll(newConfig);
            plugin.onConfigChanged(currentConfig);
        }
    }
    
    public Map<String, Object> getPluginConfig(String pluginName) {
        return Collections.unmodifiableMap(pluginConfigs.getOrDefault(pluginName, new HashMap<>()));
    }
    
    public void onCommandExecuted(String command, String[] args) {
        plugins.values().forEach(plugin -> {
            try {
                plugin.onCommandExecuted(command, args);
            } catch (Exception e) {
                plugin.onError(e);
            }
        });
    }
    
    public void unloadPlugin(String pluginName) {
        TerminalPlugin plugin = plugins.remove(pluginName);
        if (plugin != null) {
            plugin.shutdown();
            
            pluginCommands.entrySet().removeIf(entry -> 
                plugin.getCommands().containsKey(entry.getKey()));
            
            EventManager.getInstance().emit(new TerminalEvent(EventType.STATE_CHANGED, "Plugin unloaded: " + plugin.getName()));
        }
    }
    
    public Map<String, CommandInfo> getPluginCommands() {
        return Collections.unmodifiableMap(pluginCommands);
    }
    
    public List<TerminalPlugin> getLoadedPlugins() {
        return new ArrayList<>(plugins.values());
    }
    
    public void shutdown() {
        for (TerminalPlugin plugin : plugins.values()) {
            try {
                plugin.shutdown();
            } catch (Exception e) {
                systemFacade.logError(CLASS_NAME, "Ошибка при выключении плагина " + plugin.getName() + ": " + e.getMessage());
            }
        }
        plugins.clear();
        pluginCommands.clear();
    }
} 