package com.terminal.sdk.core;

import com.terminal.sdk.core.handlers.AsyncCommandHandler;
import com.terminal.sdk.core.handlers.DefaultCommandHandler;
import com.terminal.sdk.core.handlers.SystemCommandHandler;

/**
 * Менеджер цепочки обработчиков команд
 */
public class CommandChainManager {
    private static CommandChainManager instance;
    private CommandHandler chain;

    private CommandChainManager() {
        // Создаем цепочку обработчиков
        chain = new AsyncCommandHandler()
            .setNext(new SystemCommandHandler()
                .setNext(new DefaultCommandHandler()));
    }

    public static CommandChainManager getInstance() {
        if (instance == null) {
            instance = new CommandChainManager();
        }
        return instance;
    }

    /**
     * Обработать команду через цепочку обработчиков
     */
    public boolean processCommand(CommandContext context) {
        return chain.handle(context);
    }

    /**
     * Добавить новый обработчик в начало цепочки
     */
    public void addHandler(CommandHandler handler) {
        handler.setNext(chain);
        chain = handler;
    }
} 
