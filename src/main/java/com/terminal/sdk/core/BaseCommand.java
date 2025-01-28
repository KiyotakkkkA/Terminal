package com.terminal.sdk.core;

/**
 * Базовый интерфейс для всех команд терминала
 */
public interface BaseCommand {
    /**
     * Получает имя команды
     */
    String getName();

    /**
     * Получает информацию о команде
     */
    CommandInfo getInfo();

    /**
     * Получает категорию команды
     */
    CommandCategory getCategory();
} 