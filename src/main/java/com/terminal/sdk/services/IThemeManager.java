package com.terminal.sdk.services;

import java.util.Set;

import com.google.gson.JsonObject;

public interface IThemeManager {
    void loadThemes();
    void setTheme(String themeName);
    String getThemeColor(String colorKey);
    Set<String> getAvailableThemes();
    JsonObject getCurrentTheme();
    void registerPluginTheme(String themeName, String themeContent);
} 