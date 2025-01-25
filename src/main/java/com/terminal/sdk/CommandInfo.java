package com.terminal.sdk;

/**
 * Информация о команде терминала.
 */
public class CommandInfo {
    private final String name;
    private final Command command;
    private final CommandCategory category;

    public CommandInfo(String name, Command command, CommandCategory category) {
        this.name = name;
        this.command = command;
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public Command getCommand() {
        return command;
    }

    public CommandCategory getCategory() {
        return category;
    }
} 