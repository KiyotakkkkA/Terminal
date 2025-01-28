package com.terminal.sdk.core.handlers;

import com.terminal.sdk.core.BaseCommandHandler;
import com.terminal.sdk.core.CommandContext;
import com.terminal.sdk.system.SystemFacade;

/**
 * Тестовый обработчик для логирования выполнения команд
 */
public class LoggingCommandHandler extends BaseCommandHandler {
    private final SystemFacade systemFacade = SystemFacade.getInstance();
    private static final String CLASS_NAME = LoggingCommandHandler.class.getSimpleName();

    @Override
    public boolean handle(CommandContext context) {
        systemFacade.logInfo(CLASS_NAME, 
            String.format("Выполняется команда: %s с аргументами: %s", 
                context.getCommandName(), 
                String.join(" ", context.getArgs())
            )
        );
        return handleNext(context);
    }
} 