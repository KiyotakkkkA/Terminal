package com.terminal.sdk.events;

import java.util.function.Consumer;

import com.terminal.sdk.system.SystemFacade;

/**
 * Фасад для системы событий терминала.
 * Предоставляет упрощенный интерфейс для работы с событиями.
 */
public class EventSystemFacade {
    private static EventSystemFacade instance;
    private final EventManager eventManager;
    private final SystemFacade systemFacade;
    private static final String CLASS_NAME = EventSystemFacade.class.getSimpleName();

    private EventSystemFacade() {
        this.eventManager = EventManager.getInstance();
        this.systemFacade = SystemFacade.getInstance();
    }

    /**
     * Возвращает единственный экземпляр фасада.
     * @return экземпляр EventSystemFacade
     */
    public static EventSystemFacade getInstance() {
        if (instance == null) {
            instance = new EventSystemFacade();
        }
        return instance;
    }

    /**
     * Регистрирует слушателя событий для указанных типов событий.
     * @param listener слушатель событий
     * @param eventTypes типы событий для подписки
     */
    public void registerListener(EventListener listener, EventType... eventTypes) {
        Consumer<TerminalEvent> consumer = event -> listener.onEvent(event);
        for (EventType type : eventTypes) {
            eventManager.subscribe(type, consumer);
            systemFacade.logInfo(CLASS_NAME, "Зарегистрирован слушатель для события: " + type.name());
        }
    }

    /**
     * Удаляет слушателя событий для указанных типов событий.
     * @param listener слушатель событий для удаления
     * @param eventTypes типы событий для отписки
     */
    public void removeListener(EventListener listener, EventType... eventTypes) {
        Consumer<TerminalEvent> consumer = event -> listener.onEvent(event);
        for (EventType type : eventTypes) {
            eventManager.unsubscribe(type, consumer);
            systemFacade.logInfo(CLASS_NAME, "Удален слушатель для события: " + type.name());
        }
    }

    /**
     * Отправляет событие всем зарегистрированным слушателям.
     * @param event событие для отправки
     */
    public void fireEvent(TerminalEvent event) {
        try {
            eventManager.emit(event);
            systemFacade.logDebug(CLASS_NAME, "Отправлено событие: " + event.getType().name());
        } catch (Exception e) {
            systemFacade.logError(CLASS_NAME, "Ошибка при отправке события " + event.getType().name() + ": " + e.getMessage());
        }
    }

    /**
     * Очищает все зарегистрированные слушатели.
     */
    public void clearListeners() {
        eventManager.clear();
        systemFacade.logInfo(CLASS_NAME, "Очищены все слушатели событий");
    }
} 