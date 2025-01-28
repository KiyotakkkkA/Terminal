package com.terminal.sdk.core;

import java.nio.file.Path;
import java.nio.file.Paths;

import com.terminal.sdk.system.Logger;

/**
 * Фасад для системных компонентов терминала
 */
public class SystemFacade {
    private static SystemFacade instance;
    private static final String CLASS_NAME = SystemFacade.class.getSimpleName();
    private Path currentPath;

    private SystemFacade() {
        this.currentPath = Paths.get(System.getProperty("user.dir"));
    }

    public static SystemFacade getInstance() {
        if (instance == null) {
            instance = new SystemFacade();
        }
        return instance;
    }

    /**
     * Получает текущий рабочий путь
     */
    public Path getCurrentPath() {
        return currentPath;
    }

    /**
     * Устанавливает текущий рабочий путь
     */
    public void setCurrentPath(Path path) {
        if (path != null && path.toFile().exists()) {
            this.currentPath = path.normalize();
            Logger.info(CLASS_NAME, "Установлен новый рабочий путь: " + path);
        } else {
            Logger.error(CLASS_NAME, "Попытка установить некорректный путь: " + path);
        }
    }

    /**
     * Устанавливает текущий рабочий путь по строковому представлению
     */
    public void setCurrentPath(String path) {
        try {
            Path newPath = Paths.get(path);
            setCurrentPath(newPath);
        } catch (Exception e) {
            Logger.error(CLASS_NAME, "Ошибка при установке пути: " + e.getMessage());
        }
    }

    /**
     * Возвращает путь относительно текущего рабочего пути
     */
    public Path resolvePath(String relativePath) {
        try {
            return currentPath.resolve(relativePath).normalize();
        } catch (Exception e) {
            Logger.error(CLASS_NAME, "Ошибка при разрешении пути: " + e.getMessage());
            return currentPath;
        }
    }

    /**
     * Возвращает относительный путь от текущего рабочего пути
     */
    public String getRelativePath(Path path) {
        try {
            return currentPath.relativize(path).toString();
        } catch (Exception e) {
            Logger.error(CLASS_NAME, "Ошибка при получении относительного пути: " + e.getMessage());
            return path.toString();
        }
    }
} 