package com.terminal.commands;

import java.io.File;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class TouchCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;

    public TouchCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
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

            String filename = args[0];
            File file = new File(pathHolder.getCurrentPath(), filename);

            OutputFormatter.printBoxedHeader(doc, style, "Создание файла");
            OutputFormatter.printBoxedLine(doc, style, "Файл: " + file.getCanonicalPath());

            if (file.exists()) {
                if (file.setLastModified(System.currentTimeMillis())) {
                    OutputFormatter.printBoxedLine(doc, style, "Время доступа обновлено");
                } else {
                    OutputFormatter.printError(doc, style, "Не удалось обновить время доступа");
                }
            } else {
                if (file.createNewFile()) {
                    OutputFormatter.printBoxedLine(doc, style, "Файл успешно создан");
                } else {
                    OutputFormatter.printError(doc, style, "Не удалось создать файл");
                }
            }

            OutputFormatter.printBoxedFooter(doc, style);

        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при создании файла: " + e.getMessage());
            }
        }
    }

    private void showUsage() throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Использование: touch <файл>");
        OutputFormatter.printBoxedLine(doc, style, "Создает пустой файл или обновляет время доступа");
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Примеры:");
        OutputFormatter.printBoxedLine(doc, style, "  touch new.txt     - создать файл new.txt");
        OutputFormatter.printBoxedLine(doc, style, "  touch test.log    - обновить время доступа test.log");
        OutputFormatter.printBoxedFooter(doc, style);
    }

    @Override
    public String getDescription() {
        return "создание пустых файлов";
    }
} 