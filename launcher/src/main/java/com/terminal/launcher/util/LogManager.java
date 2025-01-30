package com.terminal.launcher.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LogManager {
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "terminal.log";

    public static void setupLogging() {
        try {
            // Создаем директорию для логов, если её нет
            new File(LOG_DIR).mkdirs();
            
            // Настраиваем файловый handler
            FileHandler fileHandler = new FileHandler(LOG_DIR + File.separator + LOG_FILE, true);
            fileHandler.setFormatter(new SimpleFormatter());
            
            // Получаем корневой logger и добавляем handler
            Logger rootLogger = Logger.getLogger("");
            rootLogger.addHandler(fileHandler);
            
            // Устанавливаем уровень логирования
            rootLogger.setLevel(Level.INFO);
        } catch (IOException e) {
            System.err.println("Не удалось настроить логирование: " + e.getMessage());
        }
    }
} 