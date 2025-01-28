package com.terminal.commands;

import java.io.File;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.system.CurrentPathHolder;

public class RmCommand extends AbstractCommand {
    public RmCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style, pathHolder, "rm", "Удаляет файлы и директории", "FILE_OPERATIONS");
    }

    @Override
    protected void executeCommand(String... args) throws Exception {
        if (args.length < 1) {
            appendLine("╭─ Использование: rm <файл> [опции] ───────────────────╮");
            appendLine("│ Опции:                                               │");
            appendLine("│  -r    рекурсивное удаление директорий              │");
            appendLine("│  -f    принудительное удаление                      │");
            appendLine("╰───────────────────────────────────────────────────────╯");
            return;
        }

        File file = new File(getCurrentPath(), args[0]);
        if (!file.exists()) {
            appendLine("╭─ Ошибка ──────────────────────────────────────────────╮");
            appendLine("│ Файл не существует: " + file.getName());
            appendLine("╰───────────────────────────────────────────────────────╯");
            return;
        }

        boolean recursive = args.length > 1 && args[1].contains("r");
        if (file.isDirectory() && !recursive) {
            appendLine("╭─ Ошибка ──────────────────────────────────────────────╮");
            appendLine("│ Для удаления директории используйте опцию -r");
            appendLine("╰───────────────────────────────────────────────────────╯");
            return;
        }

        if (deleteFile(file, recursive)) {
            appendLine("╭─ Удаление ────────────────────────────────────────────╮");
            appendLine("│ Успешно удалено: " + file.getName());
            appendLine("╰───────────────────────────────────────────────────────╯");
        } else {
            appendLine("╭─ Ошибка ──────────────────────────────────────────────╮");
            appendLine("│ Не удалось удалить: " + file.getName());
            appendLine("╰───────────────────────────────────────────────────────╯");
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
} 