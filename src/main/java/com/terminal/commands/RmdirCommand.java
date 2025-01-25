package com.terminal.commands;

import java.io.File;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class RmdirCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;

    public RmdirCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style);
        this.pathHolder = pathHolder;
    }

    @Override
    public void execute(String... args) {
        try {
            if (args.length < 1) {
                showUsage();
                return;
            }

            String dirName = args[0];
            File dir = new File(pathHolder.getCurrentPath(), dirName);

            if (!dir.exists()) {
                OutputFormatter.printError(doc, style, "Папка не существует: " + dirName);
                return;
            }

            if (!dir.isDirectory()) {
                OutputFormatter.printError(doc, style, "Не является папкой: " + dirName);
                return;
            }

            if (dir.list().length > 0 && (args.length < 2 || !args[1].equals("-r"))) {
                OutputFormatter.printError(doc, style, 
                    "Папка не пуста. Используйте флаг -r для рекурсивного удаления.");
                return;
            }

            OutputFormatter.printBoxedHeader(doc, style, "Удаление папки");
            OutputFormatter.printBoxedLine(doc, style, "Папка: " + dir.getCanonicalPath());

            if (deleteDirectory(dir)) {
                OutputFormatter.printBoxedLine(doc, style, "Папка успешно удалена");
            } else {
                OutputFormatter.printError(doc, style, "Не удалось удалить папку");
            }

            OutputFormatter.printBoxedFooter(doc, style);

        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при удалении папки: " + e.getMessage());
            }
        }
    }

    private boolean deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return dir.delete();
    }

    private void showUsage() throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Использование: rmdir <папка> [-r]");
        OutputFormatter.printBoxedLine(doc, style, "Удаляет указанную папку");
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Параметры:");
        OutputFormatter.printBoxedLine(doc, style, "  -r    рекурсивное удаление (включая содержимое)");
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Примеры:");
        OutputFormatter.printBoxedLine(doc, style, "  rmdir empty_dir        - удалить пустую папку");
        OutputFormatter.printBoxedLine(doc, style, "  rmdir full_dir -r      - удалить папку с содержимым");
        OutputFormatter.printBoxedFooter(doc, style);
    }

    @Override
    public String getDescription() {
        return "удаление папок";
    }
} 