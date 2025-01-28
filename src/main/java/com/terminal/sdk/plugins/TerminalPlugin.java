package com.terminal.sdk.plugins;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.terminal.sdk.core.Command;
import com.terminal.sdk.events.EventType;

/**
 * Основной интерфейс для создания плагинов Terminal.
 * Каждый плагин должен реализовать этот интерфейс для интеграции с системой.
 * 
 * Пример простого плагина:
 * {@code
 * public class ExamplePlugin implements TerminalPlugin {
 *     private Map<String, Command> commands = new HashMap<>();
 *     
 *     @Override
 *     public void initialize() {
 *         commands.put("hello", new HelloCommand());
 *     }
 *     
 *     @Override
 *     public String getName() { return "ExamplePlugin"; }
 *     
 *     @Override
 *     public String getVersion() { return "1.0.0"; }
 *     
 *     @Override
 *     public String getDescription() { return "Пример простого плагина"; }
 *     
 *     @Override
 *     public String getAuthor() { return "Author Name"; }
 *     
 *     @Override
 *     public Map<String, Command> getCommands() { return commands; }
 * }
 * }
 */
public interface TerminalPlugin {
    /**
     * Возвращает уникальное имя плагина.
     * Имя должно быть уникальным среди всех плагинов.
     * 
     * @return уникальное имя плагина, например "FileManager" или "NetworkTools"
     */
    String getName();

    /**
     * Возвращает версию плагина в формате семантического версионирования (MAJOR.MINOR.PATCH).
     * Например: "1.0.0", "2.3.1"
     * 
     * @return строка версии в формате MAJOR.MINOR.PATCH
     */
    String getVersion();

    /**
     * Возвращает описание плагина и его основных функций.
     * Описание должно быть информативным и понятным пользователю.
     * 
     * @return описание плагина на русском языке
     */
    String getDescription();

    /**
     * Возвращает имя автора или организации, создавшей плагин.
     * 
     * @return имя автора или организации
     */
    String getAuthor();
    
    /**
     * Возвращает карту команд, предоставляемых плагином.
     * Ключ - имя команды, значение - реализация команды.
     * 
     * Пример:
     * {@code
     * Map<String, Command> commands = new HashMap<>();
     * commands.put("scan", new NetworkScanCommand());
     * commands.put("ping", new PingCommand());
     * return commands;
     * }
     * 
     * @return карта команд плагина
     */
    Map<String, Command> getCommands();

    /**
     * Возвращает описания подкоманд для каждой команды плагина.
     * 
     * Пример:
     * {@code
     * Map<String, Map<String, String>> subcommands = new HashMap<>();
     * Map<String, String> scanSubcommands = new HashMap<>();
     * scanSubcommands.put("network", "Сканировать сеть");
     * scanSubcommands.put("ports", "Сканировать порты");
     * subcommands.put("scan", scanSubcommands);
     * return subcommands;
     * }
     * 
     * @return карта описаний подкоманд
     */
    default Map<String, Map<String, String>> getSubcommandsDescriptions() {
        return new HashMap<>();
    }
    
    /**
     * Инициализация плагина. Вызывается при загрузке плагина.
     * 
     * В этом методе следует:
     * - Зарегистрировать команды через commands.put()
     * - Загрузить конфигурацию
     * - Инициализировать ресурсы
     * - Подписаться на события через EventManager
     */
    void initialize();

    /**
     * Завершение работы плагина. Вызывается при выгрузке плагина.
     * 
     * В этом методе следует:
     * - Сохранить текущее состояние
     * - Освободить ресурсы
     * - Отписаться от событий через EventManager
     */
    void shutdown();

    /**
     * Проверяет, включен ли плагин.
     * Отключенные плагины не загружаются системой.
     * 
     * @return true если плагин включен, false если отключен
     */
    boolean isEnabled();
    
    /**
     * Возвращает список зависимостей плагина.
     * Зависимости указываются в формате "имя_плагина:версия".
     * 
     * Пример:
     * {@code
     * return Arrays.asList(
     *     "NetworkTools:1.0.0",
     *     "FileManager:2.1.0"
     * );
     * }
     * 
     * @return список зависимостей
     */
    default List<String> getDependencies() {
        return Collections.emptyList();
    }
    
    /**
     * Возвращает конфигурацию плагина по умолчанию.
     * Эта конфигурация используется при первом запуске.
     * 
     * Пример:
     * {@code
     * Map<String, Object> config = new HashMap<>();
     * config.put("timeout", 5000);
     * config.put("maxRetries", 3);
     * return config;
     * }
     * 
     * @return карта конфигурации по умолчанию
     */
    Map<String, Object> getDefaultConfig();
    
    /**
     * Вызывается при изменении конфигурации плагина.
     * 
     * @param newConfig новая конфигурация
     */
    default void onConfigChanged(Map<String, Object> newConfig) {
    }
    
    /**
     * Проверяет, является ли плагин базовым (встроенным).
     * Не рекомендуется переопределять этот метод.
     * 
     * @return true для встроенных плагинов, false для пользовательских
     */
    default boolean isBasePlugin() {
        return this.getClass().getPackage().getName().startsWith("com.terminal.plugins");
    }
    
    /**
     * Вызывается при выполнении команды плагина.
     * 
     * @param command имя выполненной команды
     * @param args аргументы команды
     */
    default void onCommandExecuted(String command, String[] args) {
    }
    
    /**
     * Вызывается при возникновении ошибки в плагине.
     * 
     * @param e возникшее исключение
     */
    void onError(Exception e);

    /**
     * Возвращает список поддерживаемых событий плагина.
     * 
     * Пример:
     * {@code
     * return Arrays.asList(
     *     EventType.COMMAND_COMPLETED,
     *     EventType.ERROR_OCCURRED
     * );
     * }
     * 
     * @return список поддерживаемых типов событий
     */
    List<EventType> getSupportedEvents();
} 