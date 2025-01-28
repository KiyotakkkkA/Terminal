package com.terminal.sdk.services;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.core.AsyncCommand;
import com.terminal.sdk.core.Command;
import com.terminal.sdk.system.CurrentPathHolder;

/**
 * Интерфейс фабрики для создания команд терминала
 */
public interface ICommandFactory {
    /**
     * Создает базовую команду
     */
    Command createCommand(String name, StyledDocument doc, Style style, CurrentPathHolder pathHolder);
    
    /**
     * Создает асинхронную команду
     */
    AsyncCommand createAsyncCommand(String name, StyledDocument doc, Style style, CurrentPathHolder pathHolder);
    
    /**
     * Создает команду с дополнительным стилем
     */
    Command createCommandWithExtraStyle(String name, StyledDocument doc, Style style, Style extraStyle, CurrentPathHolder pathHolder);
    
    /**
     * Создает команду с дополнительными параметрами
     */
    Command createCommandWithParams(String name, StyledDocument doc, Style style, CurrentPathHolder pathHolder, Object... params);
} 