package com.terminal.sdk.core;

import java.util.concurrent.CompletableFuture;

/**
 * Интерфейс для асинхронных команд
 */
public interface IAsyncCommand {
    /**
     * Асинхронное выполнение команды
     */
    CompletableFuture<Void> executeAsync(String[] args);
    
    /**
     * Проверяет, является ли команда длительной операцией
     */
    boolean isLongRunning();

    /**
     * Прерывает выполнение команды
     */
    void interrupt();
} 