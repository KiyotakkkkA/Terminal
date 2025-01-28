package com.terminal.commands;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.utils.OutputFormatter;

public class ExitCommand extends AbstractCommand {
    private final Runnable exitCallback;

    public ExitCommand(StyledDocument doc, Style style, Runnable exitCallback) {
        super(doc, style, null, "exit", "Выход из терминала", "SYSTEM");
        this.exitCallback = exitCallback;
    }

    @Override
    public void executeCommand(String... args) {
        try {
            OutputFormatter.printBoxedHeader(doc, style, "Выход из терминала");
            OutputFormatter.printBoxedLine(doc, style, "До свидания!");
            OutputFormatter.printBoxedFooter(doc, style);
            if (exitCallback != null) {
                exitCallback.run();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDescription() {
        return "выход из терминала";
    }
} 