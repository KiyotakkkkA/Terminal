package com.terminal.sdk;

import com.google.gson.JsonObject;

/**
 * Строитель для создания тем оформления терминала.
 * Позволяет настраивать цвета, шрифты и отступы темы в fluent-стиле.
 */
public class ThemeBuilder {
    private final JsonObject theme;
    private final JsonObject colors;
    private final JsonObject fonts;
    private final JsonObject spacing;

    /**
     * Создает новый экземпляр строителя тем с настройками по умолчанию.
     * Инициализирует базовые параметры темы:
     * - версия: 1.0
     * - шрифт: Consolas, 14pt
     * - отступы: padding 8px, margin 4px
     */
    public ThemeBuilder() {
        theme = new JsonObject();
        colors = new JsonObject();
        fonts = new JsonObject();
        spacing = new JsonObject();
        
        theme.addProperty("version", "1.0");
        
        fonts.addProperty("primary", "Consolas");
        fonts.addProperty("size", 14);
        fonts.addProperty("lineHeight", 1.5);
        
        spacing.addProperty("padding", 8);
        spacing.addProperty("margin", 4);
    }

    /**
     * Устанавливает автора темы.
     * @param author имя автора темы
     * @return текущий экземпляр строителя
     */
    public ThemeBuilder setAuthor(String author) {
        theme.addProperty("author", author);
        return this;
    }

    /**
     * Устанавливает цвет фона терминала.
     * @param color цвет в формате HEX (#RRGGBB)
     * @return текущий экземпляр строителя
     */
    public ThemeBuilder setBackground(String color) {
        colors.addProperty("background", color);
        return this;
    }

    /**
     * Устанавливает основной цвет текста.
     * @param color цвет в формате HEX (#RRGGBB)
     * @return текущий экземпляр строителя
     */
    public ThemeBuilder setForeground(String color) {
        colors.addProperty("foreground", color);
        return this;
    }

    /**
     * Устанавливает цвет выделения текста.
     * @param color цвет в формате HEX (#RRGGBB)
     * @return текущий экземпляр строителя
     */
    public ThemeBuilder setSelection(String color) {
        colors.addProperty("selection", color);
        return this;
    }

    /**
     * Устанавливает цвет курсора.
     * @param color цвет в формате HEX (#RRGGBB)
     * @return текущий экземпляр строителя
     */
    public ThemeBuilder setCursor(String color) {
        colors.addProperty("cursor", color);
        return this;
    }

    /**
     * Устанавливает цвет для сообщений об ошибках.
     * @param color цвет в формате HEX (#RRGGBB)
     * @return текущий экземпляр строителя
     */
    public ThemeBuilder setError(String color) {
        colors.addProperty("error", color);
        return this;
    }

    /**
     * Устанавливает цвет для сообщений об успешном выполнении.
     * @param color цвет в формате HEX (#RRGGBB)
     * @return текущий экземпляр строителя
     */
    public ThemeBuilder setSuccess(String color) {
        colors.addProperty("success", color);
        return this;
    }

    /**
     * Устанавливает цвет для предупреждений.
     * @param color цвет в формате HEX (#RRGGBB)
     * @return текущий экземпляр строителя
     */
    public ThemeBuilder setWarning(String color) {
        colors.addProperty("warning", color);
        return this;
    }

    /**
     * Устанавливает цвет для информационных сообщений.
     * @param color цвет в формате HEX (#RRGGBB)
     * @return текущий экземпляр строителя
     */
    public ThemeBuilder setInfo(String color) {
        colors.addProperty("info", color);
        return this;
    }

    /**
     * Устанавливает цвет для отображения имени пользователя.
     * @param color цвет в формате HEX (#RRGGBB)
     * @return текущий экземпляр строителя
     */
    public ThemeBuilder setUsername(String color) {
        colors.addProperty("username", color);
        return this;
    }

    /**
     * Устанавливает цвет для отображения пути директории.
     * @param color цвет в формате HEX (#RRGGBB)
     * @return текущий экземпляр строителя
     */
    public ThemeBuilder setDirectory(String color) {
        colors.addProperty("directory", color);
        return this;
    }

    /**
     * Устанавливает цвет для подсказок автодополнения.
     * @param color цвет в формате HEX (#RRGGBB)
     * @return текущий экземпляр строителя
     */
    public ThemeBuilder setSuggestion(String color) {
        colors.addProperty("suggestion", color);
        return this;
    }

    /**
     * Устанавливает цвет для символа приглашения ввода.
     * @param color цвет в формате HEX (#RRGGBB)
     * @return текущий экземпляр строителя
     */
    public ThemeBuilder setPrompt(String color) {
        colors.addProperty("prompt", color);
        return this;
    }

    /**
     * Собирает и возвращает тему в формате JSON-строки.
     * Включает все настроенные параметры: цвета, шрифты и отступы.
     * @return JSON-строка с настройками темы
     */
    public String build() {
        theme.add("colors", colors);
        theme.add("fonts", fonts);
        theme.add("spacing", spacing);
        return theme.toString();
    }
} 