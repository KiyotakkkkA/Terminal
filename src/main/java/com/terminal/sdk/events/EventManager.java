package com.terminal.sdk.events;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.terminal.sdk.services.IEventManager;
import com.terminal.sdk.system.SystemFacade;

/**
 * Менеджер событий терминала.
 * Реализует паттерн Observer для обработки различных событий в системе.
 * Позволяет компонентам подписываться на события и получать уведомления при их возникновении.
 */
public class EventManager implements IEventManager {
    private static EventManager instance;
    private final Map<EventType, List<Consumer<TerminalEvent>>> listeners;
    private final SystemFacade systemFacade;
    private static final String CLASS_NAME = EventManager.class.getSimpleName();

    /**
     * Создает новый экземпляр менеджера событий.
     * Инициализирует коллекцию слушателей и устанавливает флаг завершения работы в false.
     */
    private EventManager() {
        this.listeners = new EnumMap<>(EventType.class);
        this.systemFacade = SystemFacade.getInstance();
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
    @Override
    public void subscribe(EventType type, Consumer<TerminalEvent> listener) {
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
        systemFacade.logInfo(CLASS_NAME, "Подписка на событие " + type);
    }

    /**
     * Отписывает слушателя от определенного типа событий.
     *
     * @param type тип события
     * @param listener обработчик события для удаления
     */
    @Override
    public void unsubscribe(EventType type, Consumer<TerminalEvent> listener) {
        List<Consumer<TerminalEvent>> typeListeners = listeners.get(type);
        if (typeListeners != null) {
            typeListeners.remove(listener);
            systemFacade.logInfo(CLASS_NAME, "Отписка от события " + type);
        }
    }

    /**
     * Генерирует новое событие и оповещает всех подписанных слушателей.
     *
     * @param event событие для обработки
     */
    @Override
    public void emit(TerminalEvent event) {
        List<Consumer<TerminalEvent>> typeListeners = listeners.get(event.getType());
        if (typeListeners != null) {
            for (Consumer<TerminalEvent> listener : new ArrayList<>(typeListeners)) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    systemFacade.logError(CLASS_NAME, "Ошибка в обработчике события: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Завершает работу менеджера событий.
     * Очищает все подписки и устанавливает флаг завершения работы.
     */
    @Override
    public void clear() {
        listeners.clear();
        systemFacade.logInfo(CLASS_NAME, "Очищены все слушатели событий");
    }
} 