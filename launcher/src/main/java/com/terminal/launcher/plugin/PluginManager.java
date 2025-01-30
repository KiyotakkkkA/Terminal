package com.terminal.launcher.plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class PluginManager {
    private static final Logger logger = Logger.getLogger(PluginManager.class.getName());
    private List<Plugin> loadedPlugins;

    public PluginManager() {
        this.loadedPlugins = new ArrayList<>();
    }

    public boolean disablePlugin(String pluginName) {
        try {
            Plugin plugin = loadedPlugins.stream()
                .filter(p -> p.getName().equals(pluginName))
                .findFirst()
                .orElse(null);

            if (plugin != null) {
                plugin.onDisable();
                loadedPlugins.remove(plugin);
                logger.info("Плагин " + pluginName + " успешно отключен");
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.severe("Ошибка при отключении плагина " + pluginName + ": " + e.getMessage());
            return false;
        }
    }

    public void shutdown() {
        for (Plugin plugin : loadedPlugins) {
            try {
                plugin.onDisable();
            } catch (Exception e) {
                logger.severe("Ошибка при отключении плагина " + plugin.getName() + ": " + e.getMessage());
            }
        }
        loadedPlugins.clear();
    }
} 