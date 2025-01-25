package com.terminal.sdk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Менеджер событий терминала.
 * Реализует паттерн Observer для обработки различных событий в системе.
 * Позволяет компонентам подписываться на события и получать уведомления при их возникновении.
 */
public class EventManager {
    private static EventManager instance;
    private final Map<EventType, List<Consumer<TerminalEvent>>> listeners;
    private boolean isShutdown;

    /**
     * Создает новый экземпляр менеджера событий.
     * Инициализирует коллекцию слушателей и устанавливает флаг завершения работы в false.
     */
    private EventManager() {
        this.listeners = new HashMap<>();
        this.isShutdown = false;
    }

    /**
     * Возвращает единственный экземпляр менеджера событий.
     * Реализует паттерн Singleton.
     *
     * @return экземпляр EventManager
     */
    public static synchronized EventManager getInstance() {
        if (instance == null) {
            instance = new EventManager();
        }
        return instance;
    }

    /**
     * Подписывает слушателя на определенный тип событий.
     *
     * @param type тип события
     * @param listener обработчик события
     */
    public void subscribe(EventType type, Consumer<TerminalEvent> listener) {
        if (isShutdown) return;
        
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
    }

    /**
     * Отписывает слушателя от определенного типа событий.
     *
     * @param type тип события
     * @param listener обработчик события для удаления
     */
    public void unsubscribe(EventType type, Consumer<TerminalEvent> listener) {
        if (isShutdown) return;
        
        List<Consumer<TerminalEvent>> typeListeners = listeners.get(type);
        if (typeListeners != null) {
            typeListeners.remove(listener);
        }
    }

    /**
     * Генерирует новое событие и оповещает всех подписанных слушателей.
     *
     * @param event событие для обработки
     */
    public void emit(TerminalEvent event) {
        if (isShutdown) return;
        
        List<Consumer<TerminalEvent>> typeListeners = listeners.get(event.getType());
        if (typeListeners != null) {
            for (Consumer<TerminalEvent> listener : typeListeners) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    Logger.error(getClass().getSimpleName(), "Error in event listener: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Завершает работу менеджера событий.
     * Очищает все подписки и устанавливает флаг завершения работы.
     */
    public void shutdown() {
        isShutdown = true;
        listeners.clear();
    }
} 