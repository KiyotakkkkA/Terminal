package com.terminal.sdk;

import java.util.List;

/**
 * Базовый интерфейс для создания команд терминала.
 * Определяет основные методы, которые должна реализовать каждая команда:
 * - выполнение команды
 * - получение подсказок для автодополнения
 */
public interface Command {
    
    /**
     * Выполняет команду с переданными аргументами.
     * 
     * @param args массив аргументов команды
     * @throws Exception если произошла ошибка при выполнении команды
     */
    void execute(String[] args) throws Exception;
    
    /**
     * Возвращает список возможных подсказок для автодополнения
     * на основе введенных аргументов.
     * 
     * @param args текущие аргументы команды
     * @return список возможных вариантов автодополнения
     */
    List<String> getSuggestions(String[] args);
    
    String getDescription();
    
    default String executeAndGetOutput(String... args) {
        return null;
    }
} 