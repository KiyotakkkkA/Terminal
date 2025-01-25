package com.terminal.commands;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.utils.OutputFormatter;

public class PsCommand extends SystemCommandBase {

    public PsCommand(StyledDocument doc, Style style) {
        super(doc, style);
    }

    @Override
    public void execute(String... args) {
        try {
            String command = isWindows() ? "tasklist" : "ps aux";
            
            OutputFormatter.printBoxedHeader(doc, style, "Список процессов");
            String output = executeSystemCommand(command);
            appendToDoc(output);
            OutputFormatter.printBoxedFooter(doc, style);
            
        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public String getDescription() {
        return "список запущенных процессов";
    }

    @Override
    public String executeAndGetOutput(String... args) {
        try {
            String command = isWindows() ? "tasklist" : "ps aux";
            return executeSystemCommand(command);
        } catch (Exception e) {
            return "Ошибка: " + e.getMessage() + "\n";
        }
    }
} 