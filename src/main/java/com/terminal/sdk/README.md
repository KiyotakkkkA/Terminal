# Terminal SDK Documentation

## Обзор

Terminal SDK предоставляет набор инструментов для разработки плагинов и тем для терминального приложения. SDK включает в себя базовые классы и интерфейсы для создания команд, обработки событий и управления темами оформления.

## Основные компоненты

### Command Interface

Базовый интерфейс для создания команд терминала. Определяет методы для выполнения команд и получения подсказок.

### BasePlugin

Базовый класс для создания плагинов. Предоставляет основную функциональность для регистрации команд и обработки событий.

### EventManager

Система управления событиями, позволяющая плагинам подписываться на события и реагировать на них.

### ThemeBuilder

Инструмент для создания и настройки тем оформления терминала.

### Logger

Утилита для логирования событий и отладки плагинов.

## Создание плагина

1. Создайте класс, наследующий `BasePlugin`
2. Реализуйте необходимые методы:
   - `initialize()` - инициализация плагина
   - `shutdown()` - освобождение ресурсов
   - Добавьте обработчики событий при необходимости

Пример:

```java
public class MyPlugin extends BasePlugin {
    @Override
    public void initialize() {
        // Инициализация
    }

    @Override
    public void shutdown() {
        // Очистка
    }
}
```

## Создание команды

1. Создайте класс, реализующий интерфейс `Command`
2. Реализуйте методы:
   - `execute(String[] args)` - выполнение команды
   - `getSuggestions(String[] args)` - получение подсказок

Пример:

```java
public class MyCommand implements Command {
    @Override
    public void execute(String[] args) {
        // Логика команды
    }

    @Override
    public List<String> getSuggestions(String[] args) {
        return Collections.emptyList();
    }
}
```

## Работа с темами

Используйте `ThemeBuilder` для создания новых тем:

```java
ThemeBuilder builder = new ThemeBuilder()
    .setAuthor("My Name")
    .setBackground("#1E1E1E")
    .setForeground("#FFFFFF");
String theme = builder.build();
```

## События

Доступные типы событий:

- STATE_CHANGED
- COMMAND_COMPLETED
- COMMAND_FAILED
- OUTPUT_UPDATED
- THEME_CHANGED

Подписка на события:

```java
EventManager.getInstance().subscribe(EventType.STATE_CHANGED, event -> {
    // Обработка события
});
```

## Логирование

```java
Logger.info("MyPlugin", "Plugin initialized");
Logger.error("MyPlugin", "Error occurred: " + e.getMessage());
Logger.warning("MyPlugin", "Warning message");
```
