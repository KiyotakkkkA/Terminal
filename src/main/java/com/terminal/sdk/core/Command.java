package com.terminal.sdk.core;

import java.util.concurrent.CompletableFuture;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.animating.AnimationManager;
import com.terminal.sdk.system.CurrentPathHolder;

/**
 * Базовый класс для всех команд терминала
 */
public abstract class Command {
    protected final StyledDocument doc;
    protected final Style style;
    protected final CurrentPathHolder pathHolder;
    
    public Command(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        this.doc = doc;
        this.style = style;
        this.pathHolder = pathHolder;
    }
    
    /**
     * Выполнить команду
     */
    public abstract void execute(String[] args);
    
    /**
     * Получить подсказки для автодополнения
     */
    public abstract String[] getSuggestions(String[] args);
    
    /**
     * Получить описание команды
     */
    public abstract String getDescription();
    
    /**
     * Получить имя команды
     */
    public abstract String getName();
    
    /**
     * Получить категорию команды
     */
    public abstract String getCategory();
    
    /**
     * Получить информацию о команде
     */
    public abstract CommandInfo getInfo();
    
    /**
     * Асинхронно выполняет команду с анимацией загрузки.
     * По умолчанию использует синхронный метод execute().
     * 
     * @param args массив аргументов команды
     * @return CompletableFuture с результатом выполнения
     */
    protected CompletableFuture<Void> executeAsync(String[] args) {
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
     * Выполняет команду и возвращает результат в виде строки.
     * 
     * @param args аргументы команды
     * @return результат выполнения команды
     */
    public String executeAndGetOutput(String... args) {
        return null;
    }
    
    /**
     * Проверяет, является ли команда длительной операцией.
     */
    protected boolean isLongRunning() {
        return false;
    }
} 