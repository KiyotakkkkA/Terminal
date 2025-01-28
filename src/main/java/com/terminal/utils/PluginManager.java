package com.terminal.utils;

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
import com.terminal.sdk.plugins.TerminalPlugin;
import com.terminal.sdk.services.IPluginManager;
import com.terminal.sdk.system.Logger;

public class PluginManager implements IPluginManager {
    private static PluginManager instance;
    private final Map<String, TerminalPlugin> plugins;
    private final Map<String, CommandInfo> pluginCommands;
    private final Map<String, Map<String, Object>> pluginConfigs;
    private final String pluginsDirectory;
    private final String pluginsDir = "content/plugins";
    private final EventManager eventManager;

    private PluginManager() {
        this.plugins = new HashMap<>();
        this.pluginCommands = new HashMap<>();
        this.pluginConfigs = new HashMap<>();
        this.pluginsDirectory = "content/plugins";
        this.eventManager = EventManager.getInstance();
        createPluginsDirectory();
    }

    public static synchronized PluginManager getInstance() {
        if (instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }

    private void createPluginsDirectory() {
        File directory = new File(pluginsDirectory);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    @Override
    public void loadPlugins() {
        File directory = new File(pluginsDirectory);
        Logger.info(getClass().getSimpleName(), "Поиск плагинов в директории: " + directory.getAbsolutePath());
        
        File[] jarFiles = directory.listFiles((dir, name) -> name.endsWith(".jar"));
        
        if (jarFiles != null) {
            Logger.info(getClass().getSimpleName(), "Найдено JAR файлов: " + jarFiles.length);
            for (File jar : jarFiles) {
                try {
                    Logger.info(getClass().getSimpleName(), "Загрузка плагина из файла: " + jar.getName());
                    loadPlugin(jar);
                    Logger.info(getClass().getSimpleName(), "Плагин успешно загружен: " + jar.getName());
                } catch (Exception e) {
                    Logger.error(getClass().getSimpleName(), "Ошибка загрузки плагина " + jar.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } else {
            Logger.warning(getClass().getSimpleName(), "Директория плагинов пуста или не существует");
        }
    }

    private void loadPlugin(File jarFile) throws Exception {
        try (JarFile jar = new JarFile(jarFile)) {
            URL[] urls = { new URL("jar:file:" + jarFile.getPath() + "!/") };
            Logger.debug(getClass().getSimpleName(), "URL плагина: " + urls[0]);
            
            try (URLClassLoader loader = new URLClassLoader(urls, getClass().getClassLoader())) {
                String mainClass = jar.getManifest().getMainAttributes().getValue("Plugin-Class");
                Logger.debug(getClass().getSimpleName(), "Основной класс плагина: " + mainClass);
                
                if (mainClass == null) {
                    throw new Exception("Plugin-Class не указан в манифесте");
                }

                String pluginName = jarFile.getName().replace(".jar", "");
                File pluginDir = new File(pluginsDir + "/" + pluginName);
                if (!pluginDir.exists()) {
                    pluginDir.mkdirs();
                }

                Class<?> pluginClass = Class.forName(mainClass, true, loader);
                Logger.debug(getClass().getSimpleName(), "Класс плагина загружен: " + pluginClass.getName());
                
                TerminalPlugin plugin = (TerminalPlugin) pluginClass.getDeclaredConstructor().newInstance();
                Logger.debug(getClass().getSimpleName(), "Экземпляр плагина создан: " + plugin.getName());
                
                try {
                    registerPlugin(plugin);
                } catch (Exception e) {
                    Logger.error(getClass().getSimpleName(), "Ошибка регистрации плагина " + plugin.getName() + ": " + e.getMessage());
                    plugin.onError(e);
                    throw e;
                }
            }
        }
    }

    private void registerPlugin(TerminalPlugin plugin) {
        List<String> missingDependencies = checkDependencies(plugin);
        if (!missingDependencies.isEmpty()) {
            throw new RuntimeException("Отсутствуют зависимости для плагина " + plugin.getName() + ": " + 
                String.join(", ", missingDependencies));
        }

        if (!plugin.isEnabled()) {
            Logger.warning(getClass().getSimpleName(), "Плагин " + plugin.getName() + " отключен, пропускаем...");
            return;
        }

        plugins.put(plugin.getName(), plugin);
        pluginConfigs.put(plugin.getName(), new HashMap<>(plugin.getDefaultConfig()));
        plugin.initialize();

        Map<String, Command> commands = plugin.getCommands();
        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            CommandInfo commandInfo = new CommandInfo(entry.getKey(), entry.getValue().getDescription(), CommandCategory.PLUGINS.name(), entry.getValue());
            pluginCommands.put(entry.getKey(), commandInfo);
            Logger.info(getClass().getSimpleName(), "Зарегистрирована новая команда: " + entry.getKey());
        }
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

    @Override
    public void updatePluginConfig(String pluginName, Map<String, Object> newConfig) {
        TerminalPlugin plugin = plugins.get(pluginName);
        if (plugin != null) {
            Map<String, Object> currentConfig = pluginConfigs.get(pluginName);
            currentConfig.putAll(newConfig);
            plugin.onConfigChanged(currentConfig);
        }
    }

    @Override
    public Map<String, Object> getPluginConfig(String pluginName) {
        return Collections.unmodifiableMap(pluginConfigs.getOrDefault(pluginName, new HashMap<>()));
    }

    @Override
    public void unloadPlugin(String pluginName) {
        TerminalPlugin plugin = plugins.remove(pluginName);
        if (plugin != null) {
            plugin.shutdown();
            
            pluginCommands.entrySet().removeIf(entry -> 
                plugin.getCommands().containsKey(entry.getKey()));
            
            eventManager.emit(new TerminalEvent(EventType.STATE_CHANGED, "Plugin unloaded: " + plugin.getName()));
        }
    }

    @Override
    public Map<String, CommandInfo> getPluginCommands() {
        return Collections.unmodifiableMap(pluginCommands);
    }

    @Override
    public List<TerminalPlugin> getLoadedPlugins() {
        return new ArrayList<>(plugins.values());
    }

    @Override
    public void shutdown() {
        for (TerminalPlugin plugin : plugins.values()) {
            try {
                plugin.shutdown();
            } catch (Exception e) {
                Logger.error(getClass().getSimpleName(), "Ошибка при выключении плагина " + plugin.getName() + ": " + e.getMessage());
            }
        }
        plugins.clear();
        pluginCommands.clear();
    }

    @Override
    public void registerPluginTheme(String pluginName, String themeContent) {
        ThemeManager.getInstance().registerPluginTheme(pluginName, themeContent);
    }
} 