package com.terminal.sdk.system;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Держатель текущего пути в терминале.
 * Реализует паттерн Singleton для централизованного управления текущим путем.
 */
public class CurrentPathHolder {
    private static CurrentPathHolder instance;
    private Path currentPath;
    private final List<PathChangeListener> listeners;
    private final List<String> pathHistory;
    private static final int MAX_HISTORY_SIZE = 100;
    
    private CurrentPathHolder() {
        this.currentPath = Paths.get(System.getProperty("user.dir"));
        this.listeners = new ArrayList<>();
        this.pathHistory = new ArrayList<>();
        pathHistory.add(currentPath.toString());
    }
    
    public static CurrentPathHolder getInstance() {
        if (instance == null) {
            instance = new CurrentPathHolder();
        }
        return instance;
    }
    
    public String getCurrentPath() {
        return currentPath.toString();
    }
    
    public void setCurrentPath(String path) {
        Path newPath = validatePath(path);
        if (newPath != null) {
            Path oldPath = currentPath;
            currentPath = newPath;
            addToHistory(newPath.toString());
            notifyListeners(oldPath, newPath);
        }
    }
    
    private Path validatePath(String path) {
        try {
            Path newPath;
            if (path.startsWith("/") || path.contains(":")) {
                newPath = Paths.get(path);
            } else {
                newPath = currentPath.resolve(path).normalize();
            }
            
            File file = newPath.toFile();
            if (!file.exists() || !file.isDirectory()) {
                Logger.error(getClass().getSimpleName(), "Путь не существует или не является директорией: " + path);
                return null;
            }
            
            return newPath;
        } catch (Exception e) {
            Logger.error(getClass().getSimpleName(), "Ошибка валидации пути: " + e.getMessage());
            return null;
        }
    }
    
    private void addToHistory(String path) {
        if (!pathHistory.isEmpty() && pathHistory.get(pathHistory.size() - 1).equals(path)) {
            return;
        }
        
        pathHistory.add(path);
        if (pathHistory.size() > MAX_HISTORY_SIZE) {
            pathHistory.remove(0);
        }
    }
    
    public List<String> getPathHistory() {
        return new ArrayList<>(pathHistory);
    }
    
    public void addListener(PathChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(PathChangeListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyListeners(Path oldPath, Path newPath) {
        for (PathChangeListener listener : listeners) {
            try {
                listener.onPathChanged(oldPath, newPath);
            } catch (Exception e) {
                Logger.error(getClass().getSimpleName(), "Ошибка в слушателе пути: " + e.getMessage());
            }
        }
    }
    
    public interface PathChangeListener {
        void onPathChanged(Path oldPath, Path newPath);
    }
} 