package com.terminal.sdk;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
     * Асинхронно выполняет команду с анимацией загрузки.
     * По умолчанию использует синхронный метод execute().
     * 
     * @param args массив аргументов команды
     * @return CompletableFuture с результатом выполнения
     */
    default CompletableFuture<Void> executeAsync(String[] args) {
        String animationId = "cmd_" + System.currentTimeMillis();
        AnimationManager.getInstance().startAnimation(animationId);
        
        return CompletableFuture.runAsync(() -> {
            try {
                execute(args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                AnimationManager.getInstance().stopAnimation(animationId);
            }
        });
    }
    
    /**
     * Возвращает список возможных подсказок для автодополнения
     * на основе введенных аргументов.
     * 
     * @param args текущие аргументы команды
     * @return список возможных вариантов автодополнения
     */
    List<String> getSuggestions(String[] args);
    
    /**
     * Возвращает описание команды.
     */
    String getDescription();
    
    /**
     * Выполняет команду и возвращает результат в виде строки.
     * 
     * @param args аргументы команды
     * @return результат выполнения команды
     */
    default String executeAndGetOutput(String... args) {
        return null;
    }
    
    /**
     * Проверяет, является ли команда длительной операцией.
     * Если true, то будет показана анимация загрузки.
     */
    default boolean isLongRunning() {
        return false;
    }
} 