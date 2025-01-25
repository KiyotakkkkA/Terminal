package com.terminal.commands;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class PwdCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;

    public PwdCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style);
        this.pathHolder = pathHolder;
    }

    @Override
    public void execute(String... args) {
        try {
            OutputFormatter.printBoxedHeader(doc, style, "Текущая папка");
            OutputFormatter.printBoxedLine(doc, style, pathHolder.getCurrentPath());
            OutputFormatter.printBoxedFooter(doc, style);
        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при получении текущей папки: " + e.getMessage());
            }
        }
    }

    @Override
    public String getDescription() {
        return "показать текущую папку";
    }

    @Override
    public String executeAndGetOutput(String... args) {
        return pathHolder.getCurrentPath() + "\n";
    }
} 