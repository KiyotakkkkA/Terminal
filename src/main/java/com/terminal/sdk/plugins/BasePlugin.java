package com.terminal.sdk.plugins;

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.terminal.sdk.core.Command;
import com.terminal.sdk.events.EventManager;
import com.terminal.sdk.events.EventType;
import com.terminal.sdk.events.TerminalEvent;
import com.terminal.utils.ThemeManager;

/**
 * Базовый класс для создания плагинов Terminal.
 * Предоставляет стандартную реализацию основных методов и управление конфигурацией.
 * 
 * Пример использования:
 * {@code
 * public class NetworkPlugin extends BasePlugin {
 *     @Override
 *     public String getName() { return "NetworkTools"; }
 *     
 *     @Override
 *     public String getVersion() { return "1.0.0"; }
 *     
 *     @Override
 *     public String getDescription() { 
 *         return "Инструменты для работы с сетью"; 
 *     }
 *     
 *     @Override
 *     public String getAuthor() { return "Author Name"; }
 *     
 *     @Override
 *     protected void registerCommands() {
 *         commands.put("scan", new NetworkScanCommand());
 *         commands.put("ping", new PingCommand());
 *     }
 *     
 *     @Override
 *     public Map<String, Object> getDefaultConfig() {
 *         Map<String, Object> config = new HashMap<>();
 *         config.put("timeout", 5000);
 *         config.put("maxRetries", 3);
 *         return config;
 *     }
 * }
 * }
 */
public abstract class BasePlugin implements TerminalPlugin {
    /**
     * Карта команд плагина. Ключ - имя команды, значение - реализация команды.
     * Заполняется в методе {@link #registerCommands()}.
     */
    protected final Map<String, Command> commands = new HashMap<>();
    
    /**
     * Текущая конфигурация плагина.
     * Загружается автоматически при инициализации.
     */
    protected final Map<String, Object> config = new HashMap<>();
    
    /**
     * Менеджер событий для отправки и получения событий.
     */
    protected final EventManager eventManager = EventManager.getInstance();
    
    /**
     * Флаг состояния плагина.
     */
    protected boolean isEnabled = true;
    
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * Абстрактный метод для регистрации команд плагина.
     * Реализуется в конкретном плагине для добавления команд в {@link #commands}.
     */
    protected abstract void registerCommands();
    
    @Override
    public void initialize() {
        loadConfig();
        registerCommands();
    }
    
    @Override
    public void shutdown() {
        saveConfig();
    }
    
    @Override
    public Map<String, Command> getCommands() {
        return Collections.unmodifiableMap(commands);
    }
    
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
    
    @Override
    public List<EventType> getSupportedEvents() {
        return Collections.emptyList();
    }
    
    /**
     * Загружает конфигурацию плагина из файла.
     * Если файл не существует, создает его с конфигурацией по умолчанию.
     */
    protected void loadConfig() {
        try {
            String configPath = String.format("content/plugins/%s/config.json", getName().toLowerCase());
            if (Files.exists(Paths.get(configPath))) {
                String content = new String(Files.readAllBytes(Paths.get(configPath)));
                Map<String, Object> loadedConfig = gson.fromJson(content, Map.class);
                config.putAll(loadedConfig);
            } else {
                config.putAll(getDefaultConfig());
                saveConfig();
            }
        } catch (Exception e) {
            onError(e);
        }
    }
    
    /**
     * Сохраняет текущую конфигурацию плагина в файл.
     */
    protected void saveConfig() {
        try {
            String configPath = String.format("content/plugins/%s/config.json", getName().toLowerCase());
            Files.createDirectories(Paths.get(configPath).getParent());
            try (FileWriter writer = new FileWriter(configPath)) {
                gson.toJson(config, writer);
            }
        } catch (Exception e) {
            onError(e);
        }
    }
    
    @Override
    public Map<String, Object> getDefaultConfig() {
        return new HashMap<>();
    }
    
    @Override
    public void onError(Exception e) {
        System.err.println("Ошибка в плагине " + getName() + ": " + e.getMessage());
    }
    
    /**
     * Отправляет событие через менеджер событий.
     * 
     * @param type тип события
     * @param data данные события
     */
    protected void emitEvent(EventType type, String data) {
        eventManager.emit(new TerminalEvent(type, data));
    }
    
    /**
     * Регистрирует новую тему оформления.
     * 
     * @param name имя темы
     * @param theme JSON-строка с описанием темы
     */
    protected void registerTheme(String name, String theme) {
        try {
            ThemeManager.getInstance().registerPluginTheme(name, theme);
        } catch (Exception e) {
            emitEvent(EventType.ERROR_OCCURRED, "Ошибка при регистрации темы " + name + ": " + e.getMessage());
        }
    }
    
    /**
     * Получает значение из конфигурации по ключу.
     * 
     * @param key ключ
     * @param defaultValue значение по умолчанию
     * @return значение из конфигурации или значение по умолчанию
     */
    protected Object getConfigValue(String key, Object defaultValue) {
        return config.getOrDefault(key, defaultValue);
    }
    
    /**
     * Устанавливает значение в конфигурации.
     * 
     * @param key ключ
     * @param value значение
     */
    protected void setConfigValue(String key, Object value) {
        config.put(key, value);
        saveConfig();
    }
} 