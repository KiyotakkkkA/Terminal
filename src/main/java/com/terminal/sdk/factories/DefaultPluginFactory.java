package com.terminal.sdk.factories;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

import com.terminal.sdk.plugins.PluginConfig;
import com.terminal.sdk.plugins.TerminalPlugin;
import com.terminal.sdk.services.IPluginFactory;

/**
 * Реализация фабрики плагинов по умолчанию
 */
public class DefaultPluginFactory implements IPluginFactory {
    
    @Override
    public TerminalPlugin createPlugin(PluginConfig config) {
        try {
            Class<?> pluginClass = Class.forName(config.getMainClass());
            TerminalPlugin plugin = (TerminalPlugin) pluginClass.getDeclaredConstructor().newInstance();
            initializePlugin(plugin, config);
            return plugin;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create plugin: " + config.getName(), e);
        }
    }
    
    @Override
    public TerminalPlugin createPluginWithParams(PluginConfig config, Object... params) {
        try {
            Class<?> pluginClass = Class.forName(config.getMainClass());
            TerminalPlugin plugin = (TerminalPlugin) pluginClass.getDeclaredConstructor(Object[].class).newInstance((Object) params);
            initializePlugin(plugin, config);
            return plugin;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create plugin with params: " + config.getName(), e);
        }
    }
    
    @Override
    public TerminalPlugin createPluginFromJar(String jarPath, PluginConfig config) {
        try {
            File jarFile = new File(jarPath);
            URLClassLoader classLoader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()});
            Class<?> pluginClass = classLoader.loadClass(config.getMainClass());
            TerminalPlugin plugin = (TerminalPlugin) pluginClass.getDeclaredConstructor().newInstance();
            initializePlugin(plugin, config);
            return plugin;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create plugin from JAR: " + jarPath, e);
        }
    }
    
    private void initializePlugin(TerminalPlugin plugin, PluginConfig config) {
        plugin.initialize();
    }
} 