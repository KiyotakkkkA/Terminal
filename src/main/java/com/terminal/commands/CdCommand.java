package com.terminal.commands;

import java.io.File;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class CdCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;

    public CdCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style, pathHolder, "cd", "Смена текущей директории", "FILE_OPERATIONS");
        this.pathHolder = pathHolder;
    }

    @Override
    public void executeCommand(String... args) {
        try {
            if (args.length < 1) {
                OutputFormatter.printBoxedHeader(doc, style, "Использование: cd <путь>");
                OutputFormatter.printBoxedLine(doc, style, "Примеры:");
                OutputFormatter.printBoxedLine(doc, style, "  cd ..           перейти в родительскую директорию");
                OutputFormatter.printBoxedLine(doc, style, "  cd /path/to/dir перейти по абсолютному пути");
                OutputFormatter.printBoxedLine(doc, style, "  cd dir         перейти в поддиректорию");
                OutputFormatter.printBoxedFooter(doc, style);
                return;
            }

            String newPath = args[0];
            File newDir = new File(pathHolder.getCurrentPath(), newPath).getCanonicalFile();

            if (!newDir.exists() || !newDir.isDirectory()) {
                OutputFormatter.printError(doc, style, "Директория не существует: " + newDir.getPath());
                return;
            }

            pathHolder.setCurrentPath(newDir.getPath());
            OutputFormatter.printBoxedHeader(doc, style, "Текущая директория");
            OutputFormatter.printBoxedLine(doc, style, newDir.getPath());
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
        return "сменить директорию";
    }

    public CurrentPathHolder getPathHolder() {
        return pathHolder;
    }
} 