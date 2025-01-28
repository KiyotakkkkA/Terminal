package com.terminal.sdk.core;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.system.CurrentPathHolder;

/**
 * Контекст выполнения команды, содержащий все необходимые данные
 */
public class CommandContext {
    private final String commandName;
    private final String[] args;
    private final StyledDocument doc;
    private final Style style;
    private final CurrentPathHolder pathHolder;
    private Command command;
    private boolean handled;
    private String result;

    public CommandContext(String commandName, String[] args, StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        this.commandName = commandName;
        this.args = args;
        this.doc = doc;
        this.style = style;
        this.pathHolder = pathHolder;
        this.handled = false;
    }

    public String getCommandName() {
        return commandName;
    }

    public String[] getArgs() {
        return args;
    }

    public StyledDocument getDoc() {
        return doc;
    }

    public Style getStyle() {
        return style;
    }

    public CurrentPathHolder getPathHolder() {
        return pathHolder;
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public boolean isHandled() {
        return handled;
    }

    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
} 