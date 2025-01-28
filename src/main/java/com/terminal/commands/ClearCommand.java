package com.terminal.commands;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.core.CommandContext;
import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class ClearCommand extends AbstractCommand {

    public ClearCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style, pathHolder, "cls", "Очистка терминала", "SYSTEM");
    }

    @Override
    public void execute(CommandContext context) {
        try {
            context.getDoc().remove(0, context.getDoc().getLength());
            OutputFormatter.appendText(context.getDoc(), "\n", context.getStyle());
        } catch (Exception e) {
            try {
                OutputFormatter.printError(context.getDoc(), context.getStyle(), "Ошибка при очистке терминала: " + e.getMessage());
                OutputFormatter.appendText(context.getDoc(), "\n", context.getStyle());
            } catch (Exception ex) {
                System.err.println("Ошибка при очистке терминала: " + e.getMessage());
            }
        }
    }

    @Override
    public void executeCommand(String... args) throws Exception {
        CommandContext context = new CommandContext("", args, doc, style, pathHolder);
        execute(context);
    }

    @Override
    public String getDescription() {
        return "очистка терминала";
    }
} 