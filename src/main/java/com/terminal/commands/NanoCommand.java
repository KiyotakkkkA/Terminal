package com.terminal.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class NanoCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;

    public NanoCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style);
        this.pathHolder = pathHolder;
    }

    @Override
    public void executeCommand(String... args) {
        try {
            if (args.length < 1) {
                showUsage();
                return;
            }

            String filename = args[0];
            File file = new File(pathHolder.getCurrentPath(), filename);

            if (!file.exists()) {
                OutputFormatter.printBoxedHeader(doc, style, "Создание нового файла");
                OutputFormatter.printBoxedLine(doc, style, "Файл: " + filename);
                OutputFormatter.printBoxedLine(doc, style, "");
                OutputFormatter.printBoxedLine(doc, style, "Нажмите Enter для начала редактирования");
                OutputFormatter.printBoxedFooter(doc, style);
            } else {
                OutputFormatter.printBoxedHeader(doc, style, "Редактирование файла");
                OutputFormatter.printBoxedLine(doc, style, "Файл: " + filename);
                OutputFormatter.printBoxedLine(doc, style, "");
                
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        OutputFormatter.printBoxedLine(doc, style, line);
                    }
                }
                
                OutputFormatter.printBoxedFooter(doc, style);
            }

        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при открытии файла: " + e.getMessage());
            }
        }
    }

    private void showUsage() throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Использование: nano <файл>");
        OutputFormatter.printBoxedLine(doc, style, "Открывает файл для редактирования");
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Примеры:");
        OutputFormatter.printBoxedLine(doc, style, "  nano text.txt    - редактировать text.txt");
        OutputFormatter.printBoxedLine(doc, style, "  nano new.txt     - создать и редактировать new.txt");
        OutputFormatter.printBoxedFooter(doc, style);
    }

    @Override
    public String getDescription() {
        return "редактирование текстовых файлов";
    }
} 