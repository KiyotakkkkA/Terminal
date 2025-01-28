package com.terminal.sdk.services;

import java.util.List;
import java.util.Map;

import com.terminal.sdk.core.CommandInfo;
import com.terminal.sdk.plugins.TerminalPlugin;

public interface IPluginManager {
    void loadPlugins();
    void unloadPlugin(String pluginName);
    void updatePluginConfig(String pluginName, Map<String, Object> newConfig);
    Map<String, Object> getPluginConfig(String pluginName);
    Map<String, CommandInfo> getPluginCommands();
    List<TerminalPlugin> getLoadedPlugins();
    void shutdown();
    void registerPluginTheme(String pluginName, String themeContent);
} 