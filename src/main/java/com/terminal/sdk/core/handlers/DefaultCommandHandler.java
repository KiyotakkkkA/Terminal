package com.terminal.sdk.core.handlers;

import com.terminal.sdk.core.BaseCommandHandler;
import com.terminal.sdk.core.CommandContext;

/**
 * Обработчик обычных команд по умолчанию
 */
public class DefaultCommandHandler extends BaseCommandHandler {
    @Override
    public boolean handle(CommandContext context) {
        if (context.getCommand() != null) {
            try {
                context.getCommand().execute(context);
                context.setHandled(true);
                return true;
            } catch (Exception e) {
                context.setResult("Ошибка выполнения команды: " + e.getMessage());
                return true;
            }
        }
        return handleNext(context);
    }
} 