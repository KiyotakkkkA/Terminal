package com.terminal.sdk.core.handlers;

import com.terminal.commands.SystemCommandBase;
import com.terminal.sdk.core.BaseCommandHandler;
import com.terminal.sdk.core.CommandCategory;
import com.terminal.sdk.core.CommandContext;

/**
 * Обработчик системных команд
 */
public class SystemCommandHandler extends BaseCommandHandler {
    @Override
    public boolean handle(CommandContext context) {
        if (context.getCommand() instanceof SystemCommandBase || 
            (context.getCommand() != null && 
             context.getCommand().getCategory().equals(CommandCategory.SYSTEM.name()))) {
            try {
                context.getCommand().execute(context);
                context.setHandled(true);
                return true;
            } catch (Exception e) {
                context.setResult("Ошибка выполнения системной команды: " + e.getMessage());
                return true;
            }
        }
        return handleNext(context);
    }
} 