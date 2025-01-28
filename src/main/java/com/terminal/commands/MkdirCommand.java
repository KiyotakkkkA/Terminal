package com.terminal.commands;

import java.io.File;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.core.CommandContext;
import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class MkdirCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;

    public MkdirCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style, pathHolder, "mkdir", "Создание директории", "FILE_OPERATIONS");
        this.pathHolder = pathHolder;
    }

    @Override
    public void execute(CommandContext context) {
        try {
            String[] args = context.getArgs();
            if (args.length < 1) {
                showUsage(context);
                return;
            }

            String dirName = args[0];
            File dir = new File(pathHolder.getCurrentPath(), dirName);

            OutputFormatter.printBeautifulSection(context.getDoc(), context.getStyle(), "Создание папки");
            OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(), "Папка: " + dir.getCanonicalPath());

            if (dir.exists()) {
                OutputFormatter.printError(context.getDoc(), context.getStyle(), "Папка уже существует: " + dirName);
                return;
            }

            boolean success = dir.mkdirs();
            if (success) {
                OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(), "Папка успешно создана");
            } else {
                OutputFormatter.printError(context.getDoc(), context.getStyle(), "Не удалось создать папку");
            }
        } catch (Exception e) {
            try {
                OutputFormatter.printError(context.getDoc(), context.getStyle(), "Ошибка при создании папки: " + e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при создании папки: " + e.getMessage());
            }
        }
    }

    private void showUsage(CommandContext context) throws Exception {
        OutputFormatter.printBeautifulSection(context.getDoc(), context.getStyle(), "Использование команды mkdir");
        OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(),
            "mkdir <имя_папки>\n" +
            "Создает новую папку в текущей директории");
    }

    @Override
    public void executeCommand(String... args) throws Exception {
        CommandContext context = new CommandContext("", args, doc, style, pathHolder);
        execute(context);
    }

    @Override
    public String getDescription() {
        return "создание папок";
    }
} 