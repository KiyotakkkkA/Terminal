package com.terminal.sdk;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Основной интерфейс для создания плагинов Terminal.
 * Каждый плагин должен реализовать этот интерфейс для интеграции с системой.
 */
public interface TerminalPlugin {
    /**
     * Возвращает уникальное имя плагина.
     * Имя должно быть уникальным среди всех плагинов.
     */
    String getName();

    /**
     * Возвращает версию плагина в формате семантического версионирования (MAJOR.MINOR.PATCH).
     */
    String getVersion();

    /**
     * Возвращает описание плагина и его основных функций.
     */
    String getDescription();

    /**
     * Возвращает имя автора или организации, создавшей плагин.
     */
    String getAuthor();
    
    /**
     * Возвращает карту команд, предоставляемых плагином.
     * Ключ - имя команды, значение - реализация команды.
     */
    Map<String, Command> getCommands();

    /**
     * Возвращает описания подкоманд для каждой команды плагина.
     * Ключ первого уровня - имя команды.
     * Ключ второго уровня - имя подкоманды.
     * Значение - описание подкоманды.
     */
    default Map<String, Map<String, String>> getSubcommandsDescriptions() {
        return new HashMap<>();
    }
    
    /**
     * Инициализация плагина.
     * Вызывается при загрузке плагина.
     * Здесь следует выполнять:
     * - Регистрацию команд
     * - Загрузку конфигурации
     * - Инициализацию ресурсов
     * - Подписку на события
     */
    void initialize();

    /**
     * Завершение работы плагина.
     * Вызывается при выгрузке плагина.
     * Здесь следует выполнять:
     * - Сохранение состояния
     * - Освобождение ресурсов
     * - Отписку от событий
     */
    void shutdown();

    /**
     * Проверяет, включен ли плагин.
     * Отключенные плагины не загружаются системой.
     */
    default boolean isEnabled() {
        return true;
    }
    
    /**
     * Возвращает список зависимостей плагина.
     * Зависимости указываются в формате "имя_плагина:версия".
     */
    default List<String> getDependencies() {
        return Collections.emptyList();
    }
    
    /**
     * Возвращает конфигурацию плагина по умолчанию.
     * Эта конфигурация используется при первом запуске.
     */
    default Map<String, Object> getDefaultConfig() {
        return new HashMap<>();
    }
    
    /**
     * Вызывается при изменении конфигурации плагина.
     * @param newConfig новая конфигурация
     */
    default void onConfigChanged(Map<String, Object> newConfig) {
    }
    
    /**
     * Проверяет, является ли плагин базовым (встроенным).
     */
    default boolean isBasePlugin() {
        return this.getClass().getPackage().getName().startsWith("com.terminal.plugins");
    }
    
    /**
     * Вызывается при выполнении команды плагина.
     * @param command имя выполненной команды
     * @param args аргументы команды
     */
    default void onCommandExecuted(String command, String[] args) {
    }
    
    /**
     * Вызывается при возникновении ошибки в плагине.
     * @param e возникшее исключение
     */
    default void onError(Exception e) {
    }

    /**
     * Возвращает список поддерживаемых событий плагина.
     */
    default List<EventType> getSupportedEvents() {
        return Collections.emptyList();
    }

    /**
     * Обработчик событий терминала.
     * @param event событие для обработки
     */
    default void handleEvent(TerminalEvent event) {
    }

    /**
     * Возвращает список разрешений, необходимых плагину.
     * Например: "filesystem", "network", "system" и т.д.
     */
    default List<String> getRequiredPermissions() {
        return Collections.emptyList();
    }

    /**
     * Возвращает путь к файлу справки плагина в формате Markdown.
     */
    default String getHelpFile() {
        return null;
    }

    /**
     * Возвращает карту горячих клавиш плагина.
     * Ключ - комбинация клавиш (например, "Ctrl+Shift+P")
     * Значение - действие, которое нужно выполнить
     */
    default Map<String, Runnable> getHotkeys() {
        return new HashMap<>();
    }

    /**
     * Возвращает список путей к ресурсам плагина.
     */
    default List<String> getResources() {
        return Collections.emptyList();
    }

    /**
     * Проверяет совместимость плагина с текущей версией Terminal.
     * @param terminalVersion текущая версия Terminal
     */
    default boolean isCompatible(String terminalVersion) {
        return true;
    }

    /**
     * Возвращает URL репозитория плагина.
     */
    default String getRepositoryUrl() {
        return null;
    }

    /**
     * Возвращает лицензию плагина.
     */
    default String getLicense() {
        return "MIT";
    }

    /**
     * Возвращает список тегов плагина для категоризации.
     */
    default List<String> getTags() {
        return Collections.emptyList();
    }

    /**
     * Возвращает минимальную версию Java, необходимую для работы плагина.
     */
    default String getMinJavaVersion() {
        return "1.8";
    }

    /**
     * Возвращает URL для проверки обновлений плагина.
     */
    default String getUpdateUrl() {
        return null;
    }

    /**
     * Вызывается при обновлении плагина.
     * @param oldVersion предыдущая версия
     * @param newVersion новая версия
     */
    default void onUpgrade(String oldVersion, String newVersion) {
    }
} 