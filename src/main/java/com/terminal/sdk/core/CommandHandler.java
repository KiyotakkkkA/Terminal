package com.terminal.sdk.core;

/**
 * Базовый интерфейс для обработчиков команд в цепочке ответственности
 */
public interface CommandHandler {
    /**
     * Обработать команду
     * @param context Контекст выполнения команды
     * @return true если команда обработана, false если нужно передать следующему обработчику
     */
    boolean handle(CommandContext context);
    
    /**
     * Установить следующий обработчик в цепочке
     * @param next Следующий обработчик
     * @return Текущий обработчик для цепочки вызовов
     */
    CommandHandler setNext(CommandHandler next);
    
    /**
     * Получить следующий обработчик
     */
    CommandHandler getNext();
} 