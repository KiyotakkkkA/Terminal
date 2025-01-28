package com.terminal.sdk.core;

/**
 * Класс для хранения информации о команде
 */
public class CommandInfo {
    private final String name;
    private final String description;
    private final String category;
    private final Command command;
    
    public CommandInfo(String name, String description, String category, Command command) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.command = command;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getCategory() {
        return category;
    }
    
    public Command getCommand() {
        return command;
    }
} 