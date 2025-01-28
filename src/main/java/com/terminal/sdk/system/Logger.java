package com.terminal.sdk.system;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Централизованная система логирования Terminal.
 */
public class Logger {
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "terminal.log";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private static Logger instance;
    private PrintWriter writer;
    private boolean isEnabled = true;
    private File logFile;
    private SimpleDateFormat dateFormat;
    
    private Logger() {
        try {
            File logDir = new File("logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            logFile = new File(logDir, "terminal.log");
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            
            writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(logFile, true), 
                "UTF-8"
            ));
            
            dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        } catch (Exception e) {
            System.err.println("Ошибка инициализации логгера: " + e.getMessage());
        }
    }
    
    public static Logger getInstance() {
        if (instance == null) {
            instance = new Logger();
        }
        return instance;
    }
    
    public static void info(String tag, String message) {
        log("INFO", tag, message);
    }
    
    public static void warning(String tag, String message) {
        log("WARNING", tag, message);
    }
    
    public static void error(String tag, String message) {
        log("ERROR", tag, message);
    }
    
    public static void debug(String tag, String message) {
        log("DEBUG", tag, message);
    }
    
    private static void log(String level, String tag, String message) {
        if (!getInstance().isEnabled) {
            return;
        }
        
        String timestamp = DATE_FORMAT.format(new Date());
        String logMessage = String.format("[%s] %s/%s: %s", timestamp, level, tag, message);
        
        try {
            getInstance().writer.println(logMessage);
        } catch (Exception e) {
            System.err.println("Ошибка записи в лог: " + e.getMessage());
        }
    }
    
    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }
    
    public void close() {
        if (writer != null) {
            writer.close();
        }
    }
} 