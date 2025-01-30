package com.terminal.launcher.plugin;

public interface Plugin {
    String getName();
    void onEnable();
    void onDisable();
} 