package com.terminal.commands;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.TerminalFrame;

public class SplitCommand extends AbstractCommand {
    private final TerminalFrame frame;

    public SplitCommand(StyledDocument doc, Style style, TerminalFrame frame) {
        super(doc, style);
        this.frame = frame;
        
        addSubCommand("v", "Разделить экран вертикально");
        addSubCommand("vertical", "Разделить экран вертикально");
        addSubCommand("h", "Разделить экран горизонтально");
        addSubCommand("horizontal", "Разделить экран горизонтально");
        addSubCommand("close", "Закрыть текущую панель");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            printHelp();
            return;
        }

        switch (args[0].toLowerCase()) {
            case "v":
            case "vertical":
                frame.splitVertically();
                writeToOutput("Создано вертикальное разделение\n");
                return;
            case "h":
            case "horizontal":
                frame.splitHorizontally();
                writeToOutput("Создано горизонтальное разделение\n");
                return;
            case "close":
                frame.closePanel(frame.getActivePanel());
                writeToOutput("Панель закрыта\n");
                return;
            default:
                printHelp();
        }
    }

    private void printHelp() {
        String help = "Использование команды split:\n" +
                     "  split v|vertical   - разделить экран вертикально\n" +
                     "  split h|horizontal - разделить экран горизонтально\n" +
                     "  split close       - закрыть текущую панель\n";
        writeToOutput(help);
    }

    @Override
    public String getDescription() {
        return "Управление разделением экрана";
    }
} 