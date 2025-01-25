package com.terminal.commands;

import java.io.File;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.CurrentPathHolder;

public class RmCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;

    public RmCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style);
        this.pathHolder = pathHolder;
    }

    @Override
    public void execute(String... args) {
        try {
            if (args.length < 1) {
                doc.insertString(doc.getLength(), 
                    "╭─ Использование: rm <файл> [опции] ───────────────────╮\n" +
                    "│ Опции:                                               │\n" +
                    "│  -r    рекурсивное удаление директорий              │\n" +
                    "│  -f    принудительное удаление                      │\n" +
                    "╰───────────────────────────────────────────────────────╯\n", style);
                return;
            }

            File file = new File(pathHolder.getCurrentPath(), args[0]);
            if (!file.exists()) {
                doc.insertString(doc.getLength(), 
                    "╭─ Ошибка ──────────────────────────────────────────────╮\n" +
                    "│ Файл не существует: " + file.getName() + "\n" +
                    "╰───────────────────────────────────────────────────────╯\n", style);
                return;
            }

            boolean recursive = args.length > 1 && args[1].contains("r");
            if (file.isDirectory() && !recursive) {
                doc.insertString(doc.getLength(), 
                    "╭─ Ошибка ──────────────────────────────────────────────╮\n" +
                    "│ Для удаления директории используйте опцию -r\n" +
                    "╰───────────────────────────────────────────────────────╯\n", style);
                return;
            }

            if (deleteFile(file, recursive)) {
                doc.insertString(doc.getLength(), 
                    "╭─ Удаление ────────────────────────────────────────────╮\n" +
                    "│ Успешно удалено: " + file.getName() + "\n" +
                    "╰───────────────────────────────────────────────────────╯\n", style);
            } else {
                doc.insertString(doc.getLength(), 
                    "╭─ Ошибка ──────────────────────────────────────────────╮\n" +
                    "│ Не удалось удалить: " + file.getName() + "\n" +
                    "╰───────────────────────────────────────────────────────╯\n", style);
            }
        } catch (Exception e) {
            try {
                doc.insertString(doc.getLength(), 
                    "│ Ошибка: " + e.getMessage() + "\n" +
                    "╰───────────────────────────────────────────────────────╯\n", style);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private boolean deleteFile(File file, boolean recursive) {
        if (file.isDirectory()) {
            if (!recursive) {
                return false;
            }
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    deleteFile(f, true);
                }
            }
        }
        return file.delete();
    }

    @Override
    public String getDescription() {
        return "удалить файл";
    }
} 