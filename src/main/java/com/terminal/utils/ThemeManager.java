package com.terminal.utils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.terminal.sdk.events.EventManager;
import com.terminal.sdk.events.EventType;
import com.terminal.sdk.events.TerminalEvent;
import com.terminal.sdk.services.IThemeManager;
import com.terminal.sdk.system.Logger;

public class ThemeManager implements IThemeManager {
    private static ThemeManager instance;
    private final String themesPath = "content/themes.json";
    private final String userConfigPath = "content/user.json";
    private Map<String, JsonObject> themes;
    private JsonObject currentTheme;
    private final Gson gson;
    private final EventManager eventManager;

    private ThemeManager() {
        this.themes = new HashMap<>();
        this.gson = new Gson();
        this.eventManager = EventManager.getInstance();
        loadThemes();
        
        if (currentTheme == null || !currentTheme.has("colors")) {
            Logger.error(getClass().getSimpleName(), "Error: current theme is not loaded or invalid");
            createDefaultTheme();
        }
        
        Logger.info(getClass().getSimpleName(), "Loaded theme: " + getCurrentThemeName());
        Logger.info(getClass().getSimpleName(), "Available themes: " + String.join(", ", getAvailableThemes()));
        eventManager.emit(new TerminalEvent(EventType.THEME_CHANGED, gson.toJson(currentTheme)));
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    @Override
    public void loadThemes() {
        Logger.info(getClass().getSimpleName(), "Loaded theme: " + getCurrentThemeName());
        Logger.info(getClass().getSimpleName(), "Available themes: " + String.join(", ", getAvailableThemes()));
        try {
            JsonObject allThemes = new JsonObject();
            try {
                Logger.info(getClass().getSimpleName(), "Loading themes from: " + themesPath);
                allThemes = JsonParser.parseReader(new FileReader(themesPath)).getAsJsonObject();
                Logger.info(getClass().getSimpleName(), "Found themes: " + String.join(", ", allThemes.keySet()));
            } catch (Exception e) {
                Logger.error(getClass().getSimpleName(), "Creating new themes file: " + e.getMessage());
            }
            
            if (!allThemes.has("default")) {
                Logger.warning(getClass().getSimpleName(), "Default theme not found, creating...");
                JsonObject defaultTheme = createThemeStructure();
                allThemes.add("default", defaultTheme);
                
                try (FileWriter writer = new FileWriter(themesPath)) {
                    gson.toJson(allThemes, writer);
                }
            }

            themes.clear();
            for (String key : allThemes.keySet()) {
                JsonObject theme = allThemes.getAsJsonObject(key);
                if (!isValidTheme(theme)) {
                    Logger.warning(getClass().getSimpleName(), "Warning: theme '" + key + "' has invalid structure, fixing...");
                    theme = fixThemeStructure(theme);
                }
                themes.put(key, theme);
            }
            Logger.info(getClass().getSimpleName(), "Loaded themes in memory: " + String.join(", ", themes.keySet()));

            JsonObject userConfig;
            try {
                Logger.info(getClass().getSimpleName(), "Loading user config from: " + userConfigPath);
                userConfig = JsonParser.parseReader(new FileReader(userConfigPath)).getAsJsonObject();
            } catch (Exception e) {
                Logger.error(getClass().getSimpleName(), "User config not found, creating new: " + e.getMessage());
                userConfig = new JsonObject();
                userConfig.addProperty("current-theme", "default");
                try (FileWriter writer = new FileWriter(userConfigPath)) {
                    gson.toJson(userConfig, writer);
                }
            }
            
            String currentThemeName = userConfig.has("current-theme") ? 
                userConfig.get("current-theme").getAsString() : "default";
            Logger.info(getClass().getSimpleName(), "Current theme from config: " + currentThemeName);
            
            if (!themes.containsKey(currentThemeName)) {
                Logger.warning(getClass().getSimpleName(), "Theme " + currentThemeName + " not found, falling back to default");
                currentThemeName = "default";
                userConfig.addProperty("current-theme", currentThemeName);
                try (FileWriter writer = new FileWriter(userConfigPath)) {
                    gson.toJson(userConfig, writer);
                }
            }
            
            currentTheme = themes.get(currentThemeName);
            if (currentTheme != null) {
                Logger.info(getClass().getSimpleName(), "Successfully loaded theme: " + currentThemeName);
            } else {
                Logger.error(getClass().getSimpleName(), "Error: failed to load theme " + currentThemeName);
            }
        } catch (Exception e) {
            Logger.error(getClass().getSimpleName(), "Error loading themes: " + e.getMessage());
            e.printStackTrace();
            createDefaultTheme();
        }
    }

    private boolean isValidTheme(JsonObject theme) {
        return theme != null && 
               theme.has("colors") && theme.get("colors").isJsonObject() &&
               theme.has("fonts") && theme.get("fonts").isJsonObject() &&
               theme.has("spacing") && theme.get("spacing").isJsonObject();
    }

    private JsonObject fixThemeStructure(JsonObject theme) {
        JsonObject fixedTheme = new JsonObject();
        fixedTheme.addProperty("version", theme.has("version") ? 
            theme.get("version").getAsString() : "1.0");
        fixedTheme.addProperty("author", theme.has("author") ? 
            theme.get("author").getAsString() : "Unknown");

        JsonObject colors = theme.has("colors") ? 
            theme.getAsJsonObject("colors") : new JsonObject();
        fixedTheme.add("colors", colors);

        JsonObject fonts = theme.has("fonts") ? 
            theme.getAsJsonObject("fonts") : createDefaultFonts();
        fixedTheme.add("fonts", fonts);

        JsonObject spacing = theme.has("spacing") ? 
            theme.getAsJsonObject("spacing") : createDefaultSpacing();
        fixedTheme.add("spacing", spacing);

        return fixedTheme;
    }

    private JsonObject createDefaultFonts() {
        JsonObject fonts = new JsonObject();
        fonts.addProperty("primary", "Consolas");
        fonts.addProperty("size", 14);
        fonts.addProperty("lineHeight", 1.5);
        return fonts;
    }

    private JsonObject createDefaultSpacing() {
        JsonObject spacing = new JsonObject();
        spacing.addProperty("padding", 8);
        spacing.addProperty("margin", 4);
        return spacing;
    }

    private String getCurrentThemeName() {
        try {
            JsonObject userConfig = JsonParser.parseReader(new FileReader(userConfigPath)).getAsJsonObject();
            return userConfig.has("current-theme") ? 
                userConfig.get("current-theme").getAsString() : "default";
        } catch (Exception e) {
            return "default";
        }
    }

    private JsonObject createThemeStructure() {
        JsonObject theme = new JsonObject();
        theme.addProperty("version", "1.0");
        theme.addProperty("author", "Terminal");
        
        JsonObject colors = new JsonObject();
        colors.addProperty("background", "#1E1E1E");
        colors.addProperty("foreground", "#FFFFFF");
        colors.addProperty("selection", "#264F78");
        colors.addProperty("cursor", "#FFFFFF");
        colors.addProperty("error", "#F44747");
        colors.addProperty("success", "#6A9955");
        colors.addProperty("warning", "#CCA700");
        colors.addProperty("info", "#569CD6");
        colors.addProperty("username", "#58A6FF");
        colors.addProperty("directory", "#F6B93B");
        colors.addProperty("suggestion", "#8B949E");
        colors.addProperty("prompt", "#7EE787");
        theme.add("colors", colors);
        
        JsonObject fonts = new JsonObject();
        fonts.addProperty("primary", "Consolas");
        fonts.addProperty("size", 14);
        fonts.addProperty("lineHeight", 1.5);
        theme.add("fonts", fonts);
        
        JsonObject spacing = new JsonObject();
        spacing.addProperty("padding", 8);
        spacing.addProperty("margin", 4);
        theme.add("spacing", spacing);
        
        return theme;
    }

    private void createDefaultTheme() {
        JsonObject defaultTheme = new JsonObject();
        defaultTheme.addProperty("version", "1.0");
        defaultTheme.addProperty("author", "Terminal");
        
        JsonObject colors = new JsonObject();
        colors.addProperty("background", "#1E1E1E");
        colors.addProperty("foreground", "#FFFFFF");
        colors.addProperty("selection", "#264F78");
        colors.addProperty("cursor", "#FFFFFF");
        colors.addProperty("error", "#F44747");
        colors.addProperty("success", "#6A9955");
        colors.addProperty("warning", "#CCA700");
        colors.addProperty("info", "#569CD6");
        colors.addProperty("username", "#58A6FF");
        colors.addProperty("directory", "#F6B93B");
        colors.addProperty("suggestion", "#8B949E");
        colors.addProperty("prompt", "#7EE787");
        defaultTheme.add("colors", colors);
        
        JsonObject fonts = new JsonObject();
        fonts.addProperty("primary", "Consolas");
        fonts.addProperty("size", 14);
        fonts.addProperty("lineHeight", 1.5);
        defaultTheme.add("fonts", fonts);
        
        JsonObject spacing = new JsonObject();
        spacing.addProperty("padding", 8);
        spacing.addProperty("margin", 4);
        defaultTheme.add("spacing", spacing);
        
        currentTheme = defaultTheme;
        themes.put("default", defaultTheme);
        
        try {
            JsonObject allThemes = new JsonObject();
            allThemes.add("default", defaultTheme);
            try (FileWriter writer = new FileWriter(themesPath)) {
                gson.toJson(allThemes, writer);
            }
        } catch (IOException e) {
            Logger.error(getClass().getSimpleName(), "Не удалось сохранить тему по умолчанию: " + e.getMessage());
        }
        
        try {
            JsonObject userConfig = new JsonObject();
            userConfig.addProperty("current-theme", "default");
            try (FileWriter writer = new FileWriter(userConfigPath)) {
                gson.toJson(userConfig, writer);
            }
        } catch (IOException e) {
            Logger.error(getClass().getSimpleName(), "Не удалось сохранить настройки пользователя: " + e.getMessage());
        }
    }

    @Override
    public void registerPluginTheme(String themeName, String themeContent) {
        try {
            JsonObject allThemes = JsonParser.parseReader(new FileReader(themesPath)).getAsJsonObject();
            
            JsonObject themeJson = JsonParser.parseString(themeContent).getAsJsonObject();
            allThemes.add(themeName, themeJson);
            themes.put(themeName, themeJson);

            try (FileWriter writer = new FileWriter(themesPath)) {
                gson.toJson(allThemes, writer);
            }
        } catch (Exception e) {
            Logger.error(getClass().getSimpleName(), "Ошибка при регистрации темы плагина: " + e.getMessage());
        }
    }

    @Override
    public Set<String> getAvailableThemes() {
        return new HashSet<>(themes.keySet());
    }

    @Override
    public JsonObject getCurrentTheme() {
        return currentTheme;
    }

    @Override
    public void setTheme(String themeName) {
        if (!themes.containsKey(themeName)) {
            throw new IllegalArgumentException("Тема не найдена: " + themeName);
        }

        try {
            currentTheme = themes.get(themeName);

            JsonObject userConfig = new JsonObject();
            userConfig.addProperty("current-theme", themeName);
            
            try (FileWriter writer = new FileWriter(userConfigPath)) {
                gson.toJson(userConfig, writer);
            }

            eventManager.emit(new TerminalEvent(EventType.THEME_CHANGED, gson.toJson(currentTheme)));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении настроек темы: " + e.getMessage());
        }
    }

    @Override
    public String getThemeColor(String colorKey) {
        try {
            if (currentTheme.has("colors")) {
                JsonObject colors = currentTheme.getAsJsonObject("colors");
                if (colors.has(colorKey)) {
                    return colors.get(colorKey).getAsString();
                }
            }
            Logger.error(getClass().getSimpleName(), "Color " + colorKey + " not found in theme");
            return "#FFFFFF";
        } catch (Exception e) {
            Logger.error(getClass().getSimpleName(), "Error getting color " + colorKey + ": " + e.getMessage());
            return "#FFFFFF";
        }
    }

    public JsonObject getThemeFonts() {
        try {
            return currentTheme.getAsJsonObject("fonts");
        } catch (Exception e) {
            Logger.error(getClass().getSimpleName(), "Ошибка при получении настроек шрифтов: " + e.getMessage());
            return new JsonObject();
        }
    }

    public JsonObject getThemeSpacing() {
        try {
            return currentTheme.getAsJsonObject("spacing");
        } catch (Exception e) {
            Logger.error(getClass().getSimpleName(), "Ошибка при получении настроек отступов: " + e.getMessage());
            return new JsonObject();
        }
    }

    public String getThemeVersion() {
        try {
            return currentTheme.get("version").getAsString();
        } catch (Exception e) {
            return "1.0";
        }
    }

    public String getThemeAuthor() {
        try {
            return currentTheme.get("author").getAsString();
        } catch (Exception e) {
            return "Unknown";
        }
    }
} 