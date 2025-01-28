package com.terminal.sdk.factories;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.core.AsyncCommand;
import com.terminal.sdk.core.Command;
import com.terminal.sdk.core.CommandFacade;
import com.terminal.sdk.services.ICommandFactory;
import com.terminal.sdk.system.CurrentPathHolder;

/**
 * Реализация фабрики команд по умолчанию
 */
public class DefaultCommandFactory implements ICommandFactory {
    private final CommandFacade commandFacade;
    
    public DefaultCommandFactory(CommandFacade commandFacade) {
        this.commandFacade = commandFacade;
    }
    
    @Override
    public Command createCommand(String name, StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        return commandFacade.createCommand(name, "Default command", "SYSTEM", doc, style, pathHolder);
    }
    
    @Override
    public AsyncCommand createAsyncCommand(String name, StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        return commandFacade.createAsyncCommand(name, "Default async command", "SYSTEM", doc, style, pathHolder);
    }
    
    @Override
    public Command createCommandWithExtraStyle(String name, StyledDocument doc, Style style, Style extraStyle, CurrentPathHolder pathHolder) {
        return commandFacade.createCommandWithExtraStyle(name, "Default styled command", "SYSTEM", doc, style, extraStyle, pathHolder);
    }
    
    @Override
    public Command createCommandWithParams(String name, StyledDocument doc, Style style, CurrentPathHolder pathHolder, Object... params) {
        return commandFacade.createCommandWithParams(name, "Default parameterized command", "SYSTEM", doc, style, pathHolder, params);
    }
} 