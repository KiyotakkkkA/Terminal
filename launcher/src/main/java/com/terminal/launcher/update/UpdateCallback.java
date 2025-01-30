package com.terminal.launcher.update;

public interface UpdateCallback {
    void onUpdateStart();
    void onUpdateProgress(float progress, String status);
    void onUpdateComplete();
    void onUpdateError(String error);
} 