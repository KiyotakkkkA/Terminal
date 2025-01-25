package com.terminal.sdk;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Утилита для логирования событий в терминале.
 * Записывает логи в файл с поддержкой UTF-8 кодировки.
 * Поддерживает различные уровни логирования: INFO, ERROR, WARNING.
 */
public class Logger {
    private static final String LOG_FILE = "logs.txt";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Типы сообщений лога.
     */
    public enum LogType {
        INFO("INFO"),
        ERROR("ERROR"),
        WARNING("WARNING");

        private final String value;

        LogType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Записывает сообщение в лог с указанным уровнем.
     * Формат сообщения: [Дата Время] : [Имя класса] - [Тип] -> Сообщение
     *
     * @param className имя класса, из которого производится логирование
     * @param type тип сообщения (INFO, ERROR, WARNING)
     * @param message текст сообщения
     */
    public static void log(String className, LogType type, String message) {
        String logMessage = String.format("[%s] : [%s] - [%s] -> %s%n",
                LocalDateTime.now().format(formatter),
                className,
                type.getValue(),
                message);

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(LOG_FILE, true), StandardCharsets.UTF_8))) {
            writer.write(logMessage);
            System.out.println(logMessage);
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }

    /**
     * Записывает информационное сообщение в лог.
     *
     * @param className имя класса, из которого производится логирование
     * @param message текст сообщения
     */
    public static void info(String className, String message) {
        log(className, LogType.INFO, message);
    }

    /**
     * Записывает сообщение об ошибке в лог.
     *
     * @param className имя класса, из которого производится логирование
     * @param message текст сообщения об ошибке
     */
    public static void error(String className, String message) {
        log(className, LogType.ERROR, message);
    }

    /**
     * Записывает предупреждение в лог.
     *
     * @param className имя класса, из которого производится логирование
     * @param message текст предупреждения
     */
    public static void warning(String className, String message) {
        log(className, LogType.WARNING, message);
    }
} 