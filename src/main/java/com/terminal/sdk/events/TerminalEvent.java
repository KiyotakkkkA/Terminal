package com.terminal.sdk.events;

/**
 * Класс, представляющий событие в терминале.
 * Содержит тип события и связанные с ним данные.
 */
public class TerminalEvent {
    private final EventType type;
    private final Object data;

    /**
     * Создает новое событие терминала.
     *
     * @param type тип события из перечисления EventType
     * @param data данные, связанные с событием (может быть null)
     */
    public TerminalEvent(EventType type, Object data) {
        this.type = type;
        this.data = data;
    }

    /**
     * Возвращает тип события.
     *
     * @return тип события
     */
    public EventType getType() {
        return type;
    }

    /**
     * Возвращает данные, связанные с событием.
     *
     * @return объект данных события
     */
    public Object getData() {
        return data;
    }
} 