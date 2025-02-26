package com.terminal.sdk.events;

/**
 * Перечисление типов событий, поддерживаемых терминалом.
 * Определяет все возможные события, на которые могут подписываться плагины.
 */
public enum EventType {
    /**
     * Событие изменения состояния терминала или плагина.
     */
    STATE_CHANGED,
    
    /**
     * Событие успешного завершения выполнения команды.
     */
    COMMAND_COMPLETED,
    
    /**
     * Событие ошибки при выполнении команды.
     */
    COMMAND_FAILED,
    
    /**
     * Событие прерывания выполнения команды.
     */
    COMMAND_INTERRUPTED,
    
    /**
     * Событие обновления вывода в терминале.
     */
    OUTPUT_UPDATED,
    
    /**
     * Событие изменения темы оформления.
     */
    THEME_CHANGED,
    
    /**
     * Событие возникновения ошибки.
     */
    ERROR_OCCURRED,
    
    /**
     * Событие обновления кадра анимации.
     */
    ANIMATION_FRAME,
    
    /**
     * Событие ошибки анимации.
     */
    ANIMATION_ERROR,
    
    /**
     * Событие начала анимации.
     */
    ANIMATION_START,
    
    /**
     * Событие остановки анимации.
     */
    ANIMATION_STOP
} 