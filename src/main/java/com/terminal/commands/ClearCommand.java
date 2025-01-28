package com.terminal.commands;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class ClearCommand extends AbstractCommand {

    public ClearCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style, pathHolder, "cls", "Очистка терминала", "SYSTEM");
    }

    @Override
    public void executeCommand(String... args) {
        try {
            doc.remove(0, doc.getLength());
            OutputFormatter.appendText(doc, "\n", style);
        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, "Ошибка при очистке терминала: " + e.getMessage());
                OutputFormatter.appendText(doc, "\n", style);
            } catch (Exception ex) {
                System.err.println("Ошибка при очистке терминала: " + e.getMessage());
            }
        }
    }

    @Override
    public String getDescription() {
        return "очистка терминала";
    }
} 