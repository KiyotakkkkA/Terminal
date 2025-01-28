package com.terminal.sdk.events;

/**
 * Интерфейс слушателя событий в системе плагинов.
 */
public interface EventListener {
    /**
     * Обработчик события.
     * @param event событие для обработки
     */
    void onEvent(TerminalEvent event);
} 