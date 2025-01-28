package com.terminal.sdk.core.handlers;

import com.terminal.sdk.core.BaseCommandHandler;
import com.terminal.sdk.core.CommandContext;
import com.terminal.sdk.core.IAsyncCommand;

/**
 * Обработчик асинхронных команд
 */
public class AsyncCommandHandler extends BaseCommandHandler {
    @Override
    public boolean handle(CommandContext context) {
        if (context.getCommand() instanceof IAsyncCommand) {
            try {
                IAsyncCommand asyncCommand = (IAsyncCommand) context.getCommand();
                asyncCommand.executeAsync(context);
                context.setHandled(true);
                return true;
            } catch (Exception e) {
                context.setResult("Ошибка выполнения асинхронной команды: " + e.getMessage());
                return true;
            }
        }
        return handleNext(context);
    }
} 