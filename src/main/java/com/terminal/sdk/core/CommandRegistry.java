package com.terminal.sdk.core;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Реестр команд терминала
 */
public class CommandRegistry {
    private final Map<String, Command> commands;
    
    public CommandRegistry() {
        this.commands = new HashMap<>();
    }
    
    /**
     * Добавить команду в реестр
     */
    public void addCommand(String name, Command command) {
        commands.put(name, command);
    }
    
    /**
     * Получить команду по имени
     */
    public Command getCommand(String name) {
        return commands.get(name);
    }
    
    /**
     * Получить все зарегистрированные команды
     */
    public Map<String, Command> getCommands() {
        return Collections.unmodifiableMap(commands);
    }
    
    /**
     * Удалить команду из реестра
     */
    public void removeCommand(String name) {
        commands.remove(name);
    }
    
    /**
     * Проверить наличие команды в реестре
     */
    public boolean hasCommand(String name) {
        return commands.containsKey(name);
    }
    
    /**
     * Очистить реестр команд
     */
    public void clear() {
        commands.clear();
    }
} 