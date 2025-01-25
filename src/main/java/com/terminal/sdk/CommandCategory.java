package com.terminal.sdk;

/**
 * Категории команд терминала.
 */
public enum CommandCategory {
    FILE_OPERATIONS("Файловые операции"),
    NETWORK("Сетевые команды"),
    SYSTEM("Системные команды"),
    SEARCH_AND_PROCESS("Поиск и обработка"),
    PLUGIN("Плагины"),
    OTHER("Прочее");

    private final String description;

    CommandCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 