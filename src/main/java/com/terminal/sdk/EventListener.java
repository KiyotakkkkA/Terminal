package com.terminal.sdk;

/**
 * Интерфейс слушателя событий в системе плагинов.
 */
public interface EventListener {
    void onEvent(TerminalEvent event);
} 