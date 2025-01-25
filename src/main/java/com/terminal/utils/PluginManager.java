package com.terminal.utils;

import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import com.terminal.sdk.Command;
import com.terminal.sdk.CommandCategory;
import com.terminal.sdk.CommandInfo;
import com.terminal.sdk.EventManager;
import com.terminal.sdk.EventType;
import com.terminal.sdk.TerminalEvent;
import com.terminal.sdk.TerminalPlugin;

public class PluginManager {
    private static PluginManager instance;
    private final Map<String, TerminalPlugin> plugins;
    private final Map<String, CommandInfo> pluginCommands;
    private final Map<String, Map<String, Object>> pluginConfigs;
    private final String pluginsDirectory;
    private final PrintStream out;

    private final String pluginsDir = "content/plugins";

    private PluginManager() {
        this.plugins = new HashMap<>();
        this.pluginCommands = new HashMap<>();
        this.pluginConfigs = new HashMap<>();
        this.pluginsDirectory = "content/plugins";
        try {
            this.out = new PrintStream(System.out, true, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 не поддерживается", e);
        }
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

    public void loadPlugins() {
        File directory = new File(pluginsDirectory);
        out.println("Searching for plugins in directory: " + directory.getAbsolutePath());
        
        File[] jarFiles = directory.listFiles((dir, name) -> name.endsWith(".jar"));
        
        if (jarFiles != null) {
            out.println("Found JAR files: " + jarFiles.length);
            for (File jar : jarFiles) {
                try {
                    out.println("Loading plugin from file: " + jar.getName());
                    loadPlugin(jar);
                    out.println("Plugin successfully loaded: " + jar.getName());
                } catch (Exception e) {
                    out.println("Error loading plugin " + jar.getName() + ": " + e.getMessage());
                    e.printStackTrace(out);
                }
            }
        } else {
            out.println("Plugins directory is empty or does not exist");
        }
    }

    private void loadPlugin(File jarFile) throws Exception {
        try (JarFile jar = new JarFile(jarFile)) {
            URL[] urls = { new URL("jar:file:" + jarFile.getPath() + "!/") };
            out.println("Plugin URL: " + urls[0]);
            
            try (URLClassLoader loader = new URLClassLoader(urls, getClass().getClassLoader())) {
                String mainClass = jar.getManifest().getMainAttributes().getValue("Plugin-Class");
                out.println("Main plugin class: " + mainClass);
                
                if (mainClass == null) {
                    throw new Exception("Plugin-Class not specified in manifest");
                }

                String pluginName = jarFile.getName().replace(".jar", "");
                File pluginDir = new File(pluginsDir + "/" + pluginName);
                if (!pluginDir.exists()) {
                    pluginDir.mkdirs();
                }

                Class<?> pluginClass = Class.forName(mainClass, true, loader);
                out.println("Plugin class loaded: " + pluginClass.getName());
                
                TerminalPlugin plugin = (TerminalPlugin) pluginClass.getDeclaredConstructor().newInstance();
                out.println("Plugin instance created: " + plugin.getName());
                
                try {
                    registerPlugin(plugin);
                } catch (Exception e) {
                    out.println("Error registering plugin " + plugin.getName() + ": " + e.getMessage());
                    plugin.onError(e);
                    throw e;
                }
            }
        }
    }

    private void registerPlugin(TerminalPlugin plugin) {
        List<String> missingDependencies = checkDependencies(plugin);
        if (!missingDependencies.isEmpty()) {
            throw new RuntimeException("Missing dependencies for plugin " + plugin.getName() + ": " + 
                String.join(", ", missingDependencies));
        }

        if (!plugin.isEnabled()) {
            out.println("Plugin " + plugin.getName() + " is disabled, skipping...");
            return;
        }

        plugins.put(plugin.getName(), plugin);
        
        pluginConfigs.put(plugin.getName(), new HashMap<>(plugin.getDefaultConfig()));
        
        plugin.initialize();

        Map<String, Command> commands = plugin.getCommands();
        for (Map.Entry<String, Command> entry : commands.entrySet()) {
            CommandInfo commandInfo = new CommandInfo(entry.getKey(), entry.getValue(), CommandCategory.PLUGIN);
            pluginCommands.put(entry.getKey(), commandInfo);
            out.println("New command registered: " + entry.getKey());
        }

        EventManager.getInstance().emit(
            new TerminalEvent(EventType.STATE_CHANGED, 
                String.format("Loaded plugin: %s v%s by %s - %s", 
                    plugin.getName(), 
                    plugin.getVersion(),
                    plugin.getAuthor(),
                    plugin.getDescription())
            )
        );
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

            EventManager.getInstance().emit(
                new TerminalEvent(EventType.STATE_CHANGED, 
                    "Plugin unloaded: " + plugin.getName())
            );
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
                out.println("Error shutting down plugin " + plugin.getName() + ": " + e.getMessage());
            }
        }
        plugins.clear();
        pluginCommands.clear();
    }

    public void registerPluginTheme(String pluginName, String themeContent) {
        ThemeManager.getInstance().registerPluginTheme(pluginName, themeContent);
    }
} 