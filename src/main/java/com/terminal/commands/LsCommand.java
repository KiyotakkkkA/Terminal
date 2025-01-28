package com.terminal.commands;

import java.io.File;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.core.CommandContext;
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
    public void execute(CommandContext context) {
        try {
            String[] args = context.getArgs();
            File dir = args.length > 0 
                ? new File(pathHolder.getCurrentPath(), args[0])
                : new File(pathHolder.getCurrentPath());

            if (!dir.exists() || !dir.isDirectory()) {
                OutputFormatter.printError(context.getDoc(), context.getStyle(), "Директория не существует: " + dir.getPath());
                return;
            }

            OutputFormatter.printBeautifulSection(context.getDoc(), context.getStyle(), "Содержимое директории: " + dir.getCanonicalPath());
            
            File[] files = dir.listFiles();
            if (files != null) {
                String[][] data = new String[files.length][3];
                String[] headers = {"Имя", "Размер", "Тип"};
                
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    data[i][0] = file.getName();
                    data[i][1] = file.isDirectory() ? "<DIR>" : String.format("%,d bytes", file.length());
                    data[i][2] = getFileType(file);
                }
                
                OutputFormatter.printBeautifulTable(context.getDoc(), context.getStyle(), headers, data);
            }
        } catch (Exception e) {
            try {
                OutputFormatter.printError(context.getDoc(), context.getStyle(), "Ошибка при чтении директории: " + e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при чтении директории: " + e.getMessage());
            }
        }
    }

    private String getFileType(File file) {
        if (file.isDirectory()) return "Папка";
        String name = file.getName().toLowerCase();
        if (name.endsWith(".txt")) return "Текст";
        if (name.endsWith(".exe")) return "Программа";
        if (name.endsWith(".dll")) return "Библиотека";
        if (name.endsWith(".zip") || name.endsWith(".rar")) return "Архив";
        if (name.endsWith(".jpg") || name.endsWith(".png")) return "Изображение";
        if (name.endsWith(".doc") || name.endsWith(".docx")) return "Документ Word";
        if (name.endsWith(".pdf")) return "Документ PDF";
        return "Файл";
    }

    @Override
    public void executeCommand(String... args) throws Exception {
        CommandContext context = new CommandContext("", args, doc, style, pathHolder);
        execute(context);
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