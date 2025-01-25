# Terminal Plugin Development Guide

## Создание плагина

### Основной класс плагина

```java
public class YourPlugin implements TerminalPlugin {
    @Override
    public void initialize() {
        // Инициализация плагина
    }

    @Override
    public void shutdown() {
        // Очистка ресурсов при выключении
    }

    @Override
    public String getName() {
        return "YourPlugin";
    }

    @Override
    public String getDescription() {
        return "Описание вашего плагина";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public String getAuthor() {
        return "Ваше имя";
    }

    @Override
    public Map<String, Command> getCommands() {
        Map<String, Command> commands = new HashMap<>();
        // Регистрация команд плагина
        return commands;
    }
}
```

### Создание команды

```java
public class YourCommand extends AbstractCommand {
    public YourCommand(StyledDocument doc, Style style) {
        super(doc, style);
    }

    @Override
    protected void initializeSubCommands() {
        addSubCommand("subcommand", "описание подкоманды");
    }

    @Override
    public void execute(String... args) {
        // Реализация команды
    }

    @Override
    public String getDescription() {
        return "Описание команды";
    }
}
```

## Создание тем

### Структура темы

```json
{
  "version": "1.0",
  "author": "Ваше имя",
  "colors": {
    "background": "#1E1E1E",
    "foreground": "#D4D4D4",
    "selection": "#264F78",
    "cursor": "#FFFFFF",
    "error": "#F14C4C",
    "success": "#89D185",
    "warning": "#CCA700",
    "info": "#75BEFF",
    "username": "#4EC9B0",
    "directory": "#DCDCAA",
    "suggestion": "#808080",
    "prompt": "#608B4E"
  },
  "fonts": {
    "primary": "Consolas",
    "size": 14,
    "lineHeight": 1.5
  },
  "spacing": {
    "padding": 8,
    "margin": 4
  }
}
```

### Использование ThemeBuilder

```java
String myTheme = new ThemeBuilder()
    .setAuthor("Ваше имя")
    .setBackground("#000000")
    .setForeground("#FFFFFF")
    .setSelection("#404040")
    .setCursor("#FFFFFF")
    .setError("#FF0000")
    .setSuccess("#00FF00")
    .setWarning("#FFFF00")
    .setInfo("#0000FF")
    .setUsername("#FF00FF")
    .setDirectory("#00FFFF")
    .setSuggestion("#808080")
    .setPrompt("#00FF00")
    .build();

// Регистрация темы
ThemeManager.getInstance().registerPluginTheme("My Theme", myTheme);
```

### Команды для работы с темами

- `theme list` - показать список доступных тем
- `theme set <имя>` - установить тему
- `theme current` - показать текущую тему

## События

Плагины могут подписываться на события терминала:

```java
EventManager.getInstance().subscribe(TerminalEvent.EventType.THEME_CHANGED, event -> {
    // Обработка события смены темы
});
```

Доступные типы событий:

- `STATE_CHANGED` - изменение состояния терминала
- `COMMAND_COMPLETED` - команда успешно выполнена
- `COMMAND_FAILED` - ошибка при выполнении команды
- `OUTPUT_UPDATED` - обновлен вывод в терминале
- `THEME_CHANGED` - изменена тема оформления
