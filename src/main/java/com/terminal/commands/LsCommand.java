package com.terminal.commands;

import java.io.File;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class LsCommand extends AbstractCommand {
    private final Style directoryStyle;
    private final CurrentPathHolder pathHolder;

    public LsCommand(StyledDocument doc, Style style, Style directoryStyle, CurrentPathHolder pathHolder) {
        super(doc, style, pathHolder, "ls", "Просмотр содержимого директории", "FILE_OPERATIONS");
        this.directoryStyle = directoryStyle;
        this.pathHolder = pathHolder;
    }

    @Override
    public void executeCommand(String... args) {
        try {
            File dir = args.length > 0 
                ? new File(pathHolder.getCurrentPath(), args[0])
                : new File(pathHolder.getCurrentPath());

            if (!dir.exists() || !dir.isDirectory()) {
                OutputFormatter.printError(doc, style, "Директория не существует: " + dir.getPath());
                return;
            }

            OutputFormatter.printBoxedHeader(doc, style, "Содержимое директории");
            OutputFormatter.printBoxedLine(doc, style, "Путь: " + dir.getCanonicalPath());

            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    String type = file.isDirectory() ? "DIR" : "FILE";
                    try {
                        OutputFormatter.printBoxedLine(doc, file.isDirectory() ? directoryStyle : style,
                            String.format("%-4s %-40s %8d bytes",
                                type,
                                file.getName(),
                                file.length()));
                    } catch (Exception e) {
                        OutputFormatter.printError(doc, style, "Ошибка при обработке файла " + file.getName() + ": " + e.getMessage());
                    }
                }
            }

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
        return "список файлов в текущей директории";
    }

    @Override
    public String executeAndGetOutput(String... args) {
        StringBuilder output = new StringBuilder();
        try {
            File dir = args.length > 0 
                ? new File(pathHolder.getCurrentPath(), args[0])
                : new File(pathHolder.getCurrentPath());

            if (!dir.exists() || !dir.isDirectory()) {
                output.append("Ошибка: Директория не существует: ").append(dir.getPath()).append("\n");
                return output.toString();
            }

            output.append("Содержимое директории\n");
            output.append("Путь: ").append(dir.getCanonicalPath()).append("\n");

            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    String type = file.isDirectory() ? "DIR" : "FILE";
                    output.append(String.format("%-4s %-40s %8d bytes\n",
                        type,
                        file.getName(),
                        file.length()));
                }
            }

        } catch (Exception e) {
            output.append("Ошибка: ").append(e.getMessage()).append("\n");
        }
        return output.toString();
    }
} 