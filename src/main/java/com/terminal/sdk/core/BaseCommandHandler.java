package com.terminal.sdk.core;

/**
 * Базовый абстрактный класс для обработчиков команд
 */
public abstract class BaseCommandHandler implements CommandHandler {
    protected CommandHandler next;

    @Override
    public CommandHandler setNext(CommandHandler next) {
        this.next = next;
        return this;
    }

    @Override
    public CommandHandler getNext() {
        return next;
    }

    /**
     * Передать обработку следующему обработчику в цепочке
     */
    protected boolean handleNext(CommandContext context) {
        if (next != null) {
            return next.handle(context);
        }
        return false;
    }
} 