package com.terminal.sdk.system;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Фасад для управления системными компонентами Terminal.
 * Предоставляет централизованный интерфейс для работы с системными компонентами.
 */
public class SystemFacade {
    private static SystemFacade instance;
    private final CurrentPathHolder pathHolder;
    private final Logger logger;
    
    private SystemFacade() {
        this.pathHolder = CurrentPathHolder.getInstance();
        this.logger = Logger.getInstance();
    }
    
    public static SystemFacade getInstance() {
        if (instance == null) {
            instance = new SystemFacade();
        }
        return instance;
    }
    
    
    public String getCurrentPath() {
        return pathHolder.getCurrentPath();
    }
    
    public void setCurrentPath(String path) {
        pathHolder.setCurrentPath(path);
    }
    
    public List<String> getPathHistory() {
        return pathHolder.getPathHistory();
    }
    
    public void addPathChangeListener(CurrentPathHolder.PathChangeListener listener) {
        pathHolder.addListener(listener);
    }
    
    public void removePathChangeListener(CurrentPathHolder.PathChangeListener listener) {
        pathHolder.removeListener(listener);
    }
    
    public Path resolvePath(String path) {
        if (path.startsWith("/") || path.contains(":")) {
            return Paths.get(path);
        }
        return Paths.get(getCurrentPath()).resolve(path).normalize();
    }
    
    public boolean isValidPath(String path) {
        try {
            Path resolvedPath = resolvePath(path);
            File file = resolvedPath.toFile();
            return file.exists();
        } catch (Exception e) {
            logger.error(getClass().getSimpleName(), "Ошибка проверки пути: " + e.getMessage());
            return false;
        }
    }
    
    public void logInfo(String tag, String message) {
        Logger.info(tag, message);
    }
    
    public void logWarning(String tag, String message) {
        Logger.warning(tag, message);
    }
    
    public void logError(String tag, String message) {
        Logger.error(tag, message);
    }
    
    public void logDebug(String tag, String message) {
        Logger.debug(tag, message);
    }
    
    public void setLoggingEnabled(boolean enabled) {
        logger.setEnabled(enabled);
    }
    
    
    public void shutdown() {
        logger.close();
    }
    
    public String getSystemProperty(String key) {
        return System.getProperty(key);
    }
    
    public String getSystemProperty(String key, String defaultValue) {
        return System.getProperty(key, defaultValue);
    }
    
    public void setSystemProperty(String key, String value) {
        System.setProperty(key, value);
    }
    
    public String getOperatingSystem() {
        return System.getProperty("os.name");
    }
    
    public String getJavaVersion() {
        return System.getProperty("java.version");
    }
    
    public String getUserHome() {
        return System.getProperty("user.home");
    }
    
    public String getUserName() {
        return System.getProperty("user.name");
    }
    
    public String getTempDirectory() {
        return System.getProperty("java.io.tmpdir");
    }
} 