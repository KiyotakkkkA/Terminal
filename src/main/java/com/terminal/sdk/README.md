# Terminal SDK

## Обзор

SDK предоставляет полный набор инструментов для создания плагинов и команд терминала с поддержкой:

- Асинхронного выполнения команд
- Анимации загрузки и индикации прогресса
- Системы событий для обмена данными
- Форматированного вывода с таблицами и секциями
- Интерактивного ввода и автодополнения
- Обработки прерываний (Ctrl+C)
- Управления стилями и цветами

## Компоненты SDK

### 1. Command (Базовый интерфейс)

Основной интерфейс для создания команд терминала.

Методы:

```java
void execute(String[] args) throws Exception;
CompletableFuture<Void> executeAsync(String[] args);
List<String> getSuggestions(String[] args);
String getDescription();
boolean isLongRunning();
```

Пример простой команды:

```java
public class HelloCommand implements Command {
    @Override
    public void execute(String[] args) throws Exception {
        System.out.println("Привет, " + (args.length > 0 ? args[0] : "мир") + "!");
    }

    @Override
    public List<String> getSuggestions(String[] args) {
        return Arrays.asList("мир", "пользователь");
    }

    @Override
    public String getDescription() {
        return "Выводит приветствие";
    }
}
```

### 2. AsyncCommand (Асинхронные команды)

Базовый класс для создания команд с длительными операциями, анимацией и пакетным выводом.

Основные методы:

```java
protected void startOutputBlock();           // Начать блок вывода с анимацией
protected void appendOutputPacket(String);   // Добавить пакет вывода
protected void endOutputBlock();             // Завершить блок вывода
protected boolean isInterrupted();           // Проверить прерывание
protected void resetInterrupted();           // Сбросить флаг прерывания
protected void appendWithStyle(String, Style); // Добавить текст со стилем
```

Пример асинхронной команды с анимацией:

```java
public class ScanCommand extends AsyncCommand {
    public ScanCommand(StyledDocument doc, Style style) {
        super(doc, style);
    }

    @Override
    protected void executeAsync(String[] args) throws Exception {
        startOutputBlock();

        appendOutputPacket("Начинаю сканирование...");

        for (int i = 1; i <= 100; i++) {
            if (isInterrupted()) {
                appendOutputPacket("Сканирование прервано");
                break;
            }

            appendOutputPacket("Прогресс: " + i + "%");
            Thread.sleep(100);
        }

        endOutputBlock();
    }
}
```

### 3. EventManager (Система событий)

Менеджер событий для обмена сообщениями между компонентами.

Типы событий:

```java
public enum EventType {
    STATE_CHANGED,        // Изменение состояния
    COMMAND_COMPLETED,    // Команда выполнена
    COMMAND_FAILED,       // Ошибка выполнения
    COMMAND_INTERRUPTED,  // Команда прервана
    OUTPUT_UPDATED,       // Обновлен вывод
    THEME_CHANGED,        // Изменена тема
    ERROR_OCCURRED,       // Возникла ошибка
    ANIMATION_FRAME       // Новый кадр анимации
}
```

Методы:

```java
void subscribe(EventType type, Consumer<TerminalEvent> listener);
void unsubscribe(EventType type, Consumer<TerminalEvent> listener);
void emit(TerminalEvent event);
```

Пример использования:

```java
public class MonitorCommand extends AsyncCommand {
    public MonitorCommand(StyledDocument doc, Style style) {
        super(doc, style);

        // Подписка на события
        EventManager.getInstance().subscribe(EventType.STATE_CHANGED, event -> {
            String newState = (String) event.getData();
            appendOutputPacket("Новое состояние: " + newState);
        });
    }

    @Override
    protected void executeAsync(String[] args) throws Exception {
        startOutputBlock();

        // Отправка события
        EventManager.getInstance().emit(
            new TerminalEvent(EventType.STATE_CHANGED, "Активен")
        );

        endOutputBlock();
    }
}
```

### 4. DefaultBeautifulFormatter (Форматированный вывод)

Утилиты для создания красивого форматированного вывода.

Методы:

```java
void printBeautifulSection(doc, style, title);     // Секция с заголовком
void printBeautifulTable(doc, style, headers, data); // Таблица с данными
void printBeautifulMessage(doc, style, message);    // Сообщение в рамке
void printBeautifulSectionEnd(doc, style);         // Конец секции
```

Пример использования:

```java
public class StatusCommand extends AsyncCommand {
    @Override
    protected void executeAsync(String[] args) throws Exception {
        startOutputBlock();

        // Заголовок секции
        OutputFormatter.printBeautifulSection(doc, style, "СТАТУС СИСТЕМЫ");

        // Таблица с данными
        String[] headers = {"Компонент", "Состояние", "Загрузка"};
        String[][] data = {
            {"CPU", "Активен", "45%"},
            {"RAM", "ОК", "60%"},
            {"Диск", "ОК", "75%"}
        };
        OutputFormatter.printBeautifulTable(doc, style, headers, data);

        // Сообщение
        OutputFormatter.printBeautifulMessage(doc, style, "Система работает нормально");

        // Конец секции
        OutputFormatter.printBeautifulSectionEnd(doc, style);

        endOutputBlock();
    }
}
```

### 5. Animation (Анимации)

Интерфейс для создания пользовательских анимаций.

Методы:

```java
String getCurrentFrame();  // Получить текущий кадр
void update();            // Обновить состояние
int getFrameDelay();      // Задержка между кадрами
boolean isFinished();     // Проверить завершение
void start();            // Запустить анимацию
void stop();             // Остановить анимацию
void reset();            // Сбросить состояние
```

Пример пользовательской анимации:

```java
public class CustomSpinner implements Animation {
    private static final String[] FRAMES = {"◐", "◓", "◑", "◒"};
    private static final int DELAY = 100;
    private boolean running = true;
    private int frameIndex = 0;

    @Override
    public String getCurrentFrame() {
        return FRAMES[frameIndex % FRAMES.length] + " \n";
    }

    @Override
    public void update() {
        frameIndex++;
    }

    @Override
    public int getFrameDelay() {
        return DELAY;
    }

    @Override
    public boolean isFinished() {
        return !running;
    }

    @Override
    public void start() {
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public void reset() {
        frameIndex = 0;
    }
}
```

## Рекомендации по разработке

### 1. Асинхронные команды

- Используйте `AsyncCommand` для длительных операций
- Всегда проверяйте `isInterrupted()` в циклах
- Используйте `appendOutputPacket()` для обновления вывода
- Вызывайте `startOutputBlock()` и `endOutputBlock()`
- Добавляйте информативные сообщения о прогрессе

### 2. Форматированный вывод

- Группируйте связанную информацию в секции
- Используйте таблицы для структурированных данных
- Выделяйте важные сообщения в рамках
- Добавляйте пустые строки для читаемости
- Используйте стили для выделения информации

### 3. Обработка событий

- Подписывайтесь на события в конструкторе
- Отписывайтесь при завершении работы
- Используйте типизированные данные в событиях
- Проверяйте тип данных перед использованием
- Обрабатывайте ошибки в слушателях

### 4. Анимации

- Используйте короткие задержки (80-100 мс)
- Добавляйте пробел после символа анимации
- Завершайте кадр переводом строки
- Останавливайте анимацию при завершении
- Очищайте ресурсы в finally-блоке

## Полные примеры

### 1. Мониторинг системы

```java
public class SystemMonitorCommand extends AsyncCommand {
    private final ExecutorService executor;
    private final Map<String, Double> metrics;

    public SystemMonitorCommand(StyledDocument doc, Style style) {
        super(doc, style);
        this.executor = Executors.newFixedThreadPool(3);
        this.metrics = new ConcurrentHashMap<>();

        // Подписка на обновления метрик
        EventManager.getInstance().subscribe(EventType.STATE_CHANGED, event -> {
            if (event.getData() instanceof MetricUpdate) {
                MetricUpdate update = (MetricUpdate) event.getData();
                metrics.put(update.getName(), update.getValue());
                displayMetrics();
            }
        });
    }

    @Override
    protected void executeAsync(String[] args) throws Exception {
        startOutputBlock();

        try {
            // Запуск сборщиков метрик
            startMetricCollectors();

            // Основной цикл мониторинга
            while (!isInterrupted()) {
                Thread.sleep(1000);
            }

        } finally {
            executor.shutdownNow();
            appendOutputPacket("Мониторинг остановлен");
            endOutputBlock();
        }
    }

    private void startMetricCollectors() {
        // CPU
        executor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                double cpuUsage = getCpuUsage();
                EventManager.getInstance().emit(
                    new TerminalEvent(EventType.STATE_CHANGED,
                        new MetricUpdate("CPU", cpuUsage))
                );
                Thread.sleep(1000);
            }
        });

        // Memory
        executor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                double memUsage = getMemoryUsage();
                EventManager.getInstance().emit(
                    new TerminalEvent(EventType.STATE_CHANGED,
                        new MetricUpdate("Memory", memUsage))
                );
                Thread.sleep(1000);
            }
        });
    }

    private void displayMetrics() {
        // Очистка предыдущего вывода
        appendOutputPacket("\n");

        // Вывод заголовка
        OutputFormatter.printBeautifulSection(doc, style, "СИСТЕМНЫЕ МЕТРИКИ");

        // Вывод таблицы с метриками
        String[] headers = {"Метрика", "Значение", "Статус"};
        String[][] data = metrics.entrySet().stream()
            .map(e -> new String[]{
                e.getKey(),
                String.format("%.1f%%", e.getValue()),
                getStatus(e.getValue())
            })
            .toArray(String[][]::new);

        OutputFormatter.printBeautifulTable(doc, style, headers, data);

        // Вывод предупреждений
        if (metrics.values().stream().anyMatch(v -> v > 90)) {
            OutputFormatter.printBeautifulMessage(doc, style,
                "⚠ Внимание: Высокая нагрузка на систему!");
        }

        OutputFormatter.printBeautifulSectionEnd(doc, style);
    }

    private String getStatus(double value) {
        if (value < 60) return "✓ ОК";
        if (value < 90) return "⚠ Внимание";
        return "❌ Критично";
    }
}
```

### 2. Интерактивный процесс

```java
public class InstallCommand extends AsyncCommand {
    private final List<String> steps;
    private final Map<String, Boolean> completed;

    public InstallCommand(StyledDocument doc, Style style) {
        super(doc, style);
        this.steps = Arrays.asList(
            "Проверка зависимостей",
            "Загрузка компонентов",
            "Установка файлов",
            "Настройка конфигурации",
            "Проверка установки"
        );
        this.completed = new HashMap<>();
        steps.forEach(s -> completed.put(s, false));
    }

    @Override
    protected void executeAsync(String[] args) throws Exception {
        startOutputBlock();

        try {
            // Вывод заголовка
            OutputFormatter.printBeautifulSection(doc, style, "УСТАНОВКА");
            appendOutputPacket("Начинаю процесс установки...\n");

            // Выполнение шагов
            for (String step : steps) {
                if (isInterrupted()) {
                    appendOutputPacket("\n❌ Установка прервана пользователем");
                    break;
                }

                // Обновление статуса
                updateStatus(step, "В процессе...");

                // Имитация работы
                Thread.sleep(2000);

                // Завершение шага
                completed.put(step, true);
                updateStatus(step, "✓ Завершено");
            }

            // Итоговый статус
            if (!isInterrupted()) {
                OutputFormatter.printBeautifulMessage(doc, style,
                    "✨ Установка успешно завершена!");
            }

        } finally {
            OutputFormatter.printBeautifulSectionEnd(doc, style);
            endOutputBlock();
        }
    }

    private void updateStatus(String currentStep, String status) {
        // Формирование таблицы статуса
        String[] headers = {"Шаг", "Статус"};
        String[][] data = steps.stream()
            .map(step -> new String[]{
                step,
                step.equals(currentStep) ? status :
                    completed.get(step) ? "✓ Завершено" : "Ожидание..."
            })
            .toArray(String[][]::new);

        // Вывод обновленного статуса
        appendOutputPacket("\n");
        OutputFormatter.printBeautifulTable(doc, style, headers, data);
    }
}
```

## Заключение

SDK предоставляет богатый набор инструментов для создания интерактивных команд с профессиональным внешним видом и удобным пользовательским интерфейсом. Следуйте рекомендациям и используйте готовые компоненты для создания качественных плагинов.
