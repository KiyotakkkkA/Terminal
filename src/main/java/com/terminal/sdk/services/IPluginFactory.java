package com.terminal.sdk.services;

import com.terminal.sdk.plugins.TerminalPlugin;
import com.terminal.sdk.plugins.PluginConfig;

/**
 * Интерфейс фабрики для создания плагинов
 */
public interface IPluginFactory {
    /**
     * Создает плагин на основе конфигурации
     */
    TerminalPlugin createPlugin(PluginConfig config);
    
    /**
     * Создает плагин с дополнительными параметрами
     */
    TerminalPlugin createPluginWithParams(PluginConfig config, Object... params);
    
    /**
     * Создает плагин из JAR файла
     */
    TerminalPlugin createPluginFromJar(String jarPath, PluginConfig config);
} 