package com.terminal.commands;

import java.io.File;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class MkdirCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;

    public MkdirCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style, pathHolder, "mkdir", "Создание директории", "FILE_OPERATIONS");
        this.pathHolder = pathHolder;
    }

    @Override
    public void executeCommand(String... args) {
        try {
            if (args.length < 1) {
                showUsage();
                return;
            }

            String dirName = args[0];
            File dir = new File(pathHolder.getCurrentPath(), dirName);

            OutputFormatter.printBoxedHeader(doc, style, "Создание папки");
            OutputFormatter.printBoxedLine(doc, style, "Папка: " + dir.getCanonicalPath());

            if (dir.exists()) {
                OutputFormatter.printError(doc, style, "Папка уже существует: " + dirName);
                return;
            }

            boolean createParents = args.length > 1 && args[1].equals("-p");
            boolean success = createParents ? dir.mkdirs() : dir.mkdir();

            if (success) {
                OutputFormatter.printBoxedLine(doc, style, "Папка успешно создана");
            } else {
                OutputFormatter.printError(doc, style, "Не удалось создать папку");
            }

            OutputFormatter.printBoxedFooter(doc, style);

        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при создании папки: " + e.getMessage());
            }
        }
    }

    private void showUsage() throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Использование: mkdir <папка> [-p]");
        OutputFormatter.printBoxedLine(doc, style, "Создает новую папку");
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Параметры:");
        OutputFormatter.printBoxedLine(doc, style, "  -p    создать родительские папки при необходимости");
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Примеры:");
        OutputFormatter.printBoxedLine(doc, style, "  mkdir test         - создать папку test");
        OutputFormatter.printBoxedLine(doc, style, "  mkdir a/b/c -p     - создать папки a, b и c");
        OutputFormatter.printBoxedFooter(doc, style);
    }

    @Override
    public String getDescription() {
        return "создание папок";
    }
} 