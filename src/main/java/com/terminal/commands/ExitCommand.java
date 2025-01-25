package com.terminal.commands;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.utils.OutputFormatter;

public class ExitCommand extends AbstractCommand {
    private final Runnable exitCallback;

    public ExitCommand(StyledDocument doc, Style style, Runnable exitCallback) {
        super(doc, style);
        this.exitCallback = exitCallback;
    }

    @Override
    public void execute(String... args) {
        try {
            OutputFormatter.printBoxedHeader(doc, style, "Завершение работы");
            OutputFormatter.printBoxedLine(doc, style, "До свидания!");
            OutputFormatter.printBoxedFooter(doc, style);
            
            if (exitCallback != null) {
                exitCallback.run();
            }
        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при завершении работы: " + e.getMessage());
            }
        }
    }

    @Override
    public String getDescription() {
        return "завершить работу терминала";
    }
} 