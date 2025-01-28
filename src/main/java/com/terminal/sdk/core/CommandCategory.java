package com.terminal.sdk.core;

/**
 * Категории команд терминала.
 */
public enum CommandCategory {
    FILE_OPERATIONS("Файловые операции"),
    SEARCH_AND_PROCESS("Поиск и обработка"),
    ARCHIVE_OPERATIONS("Работа с архивами"),
    NETWORK("Сетевые операции"),
    SYSTEM("Системные команды"),
    PLUGINS("Плагины"),
    OTHER("Другое");

    private final String description;

    CommandCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 