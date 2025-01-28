package com.terminal.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.core.Command;
import com.terminal.sdk.core.CommandInfo;
import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class CatCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;
    private final Map<String, CommandInfo> commands;

    public CatCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder, Map<String, CommandInfo> commands) {
        super(doc, style, pathHolder, "cat", "Вывод содержимого файла или результата команды", "FILE_OPERATIONS");
        this.pathHolder = pathHolder;
        this.commands = commands;
    }

    @Override
    public void executeCommand(String... args) {
        try {
            if (args.length < 1) {
                showUsage();
                return;
            }

            String outputFile = null;
            boolean appendMode = false;

            String[] commandArgs = args;
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals(">") || args[i].equals(">>")) {
                    appendMode = args[i].equals(">>");
                    if (i + 1 < args.length) {
                        outputFile = args[i + 1];
                        commandArgs = Arrays.copyOfRange(args, 0, i);
                    }
                    break;
                }
            }

            CommandInfo commandInfo = commands.get(commandArgs[0]);
            if (commandInfo != null) {
                final Command command = commandInfo.getCommand();
                
                OutputFormatter.startOutputCapture();
                
                try {
                    command.executeAndGetOutput(Arrays.copyOfRange(commandArgs, 1, commandArgs.length));
                    
                    String output = OutputFormatter.getCapturedOutput();
                    
                    if (outputFile != null) {
                        Path path = Paths.get(pathHolder.getCurrentPath(), outputFile);
                        if (appendMode) {
                            Files.write(path, output.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        } else {
                            Files.write(path, output.getBytes(StandardCharsets.UTF_8));
                        }
                        formatter.printMessage(doc, style, "Вывод сохранен в файл: " + outputFile);
                    }
                } finally {
                    OutputFormatter.stopOutputCapture();
                }
                return;
            }

            File file = new File(pathHolder.getCurrentPath(), args[0]);
            if (!file.exists()) {
                formatter.printMessage(doc, style, "Файл не найден: " + args[0]);
                return;
            }

            if (file.isDirectory()) {
                formatter.printMessage(doc, style, "Это директория: " + args[0]);
                return;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                StringBuilder content = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                
                String output = content.toString();
                if (outputFile != null) {
                    Path path = Paths.get(pathHolder.getCurrentPath(), outputFile);
                    if (appendMode) {
                        Files.write(path, output.getBytes(StandardCharsets.UTF_8), 
                            StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    } else {
                        Files.write(path, output.getBytes(StandardCharsets.UTF_8));
                    }
                    formatter.printMessage(doc, style, "Вывод сохранен в файл: " + outputFile);
                } else {
                    formatter.printMessage(doc, style, output);
                }
            }
        } catch (Exception e) {
            formatter.printMessage(doc, style, "Ошибка: " + e.getMessage());
        }
    }

    @Override
    public String executeAndGetOutput(String... args) {
        try {
            if (args.length < 1) {
                return "Использование: cat <файл или команда> [> файл_для_записи]\n";
            }

            File file = new File(pathHolder.getCurrentPath(), args[0]);
            if (!file.exists()) {
                return "Файл не найден: " + args[0] + "\n";
            }

            if (file.isDirectory()) {
                return "Это директория: " + args[0] + "\n";
            }

            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            return content.toString();
        } catch (Exception e) {
            return "Ошибка: " + e.getMessage() + "\n";
        }
    }

    private void showUsage() throws Exception {
        formatter.printMessage(doc, style, "Использование: cat <файл или команда> [> файл_для_записи]\n");
        formatter.printMessage(doc, style, "Примеры:\n");
        formatter.printMessage(doc, style, "  cat file.txt         - вывести содержимое файла\n");
        formatter.printMessage(doc, style, "  cat sys             - вывести результат команды sys\n");
        formatter.printMessage(doc, style, "  cat file.txt > out.txt   - записать в файл\n");
        formatter.printMessage(doc, style, "  cat file.txt >> out.txt  - дописать в файл\n");
    }

    private Command findCommand(String name) {
        CommandInfo info = commands.get(name);
        return info != null ? info.getCommand() : null;
    }

    @Override
    public String getDescription() {
        return "вывод содержимого файла или результата команды";
    }

    @Override
    public String[] getSuggestions(String[] args) {
        List<String> suggestions = new ArrayList<>();
        
        if (args.length == 0 || args.length == 1) {
            String prefix = args.length > 0 ? args[0].toLowerCase() : "";
            
            for (String cmdName : commands.keySet()) {
                if (prefix.isEmpty() || cmdName.toLowerCase().startsWith(prefix)) {
                    suggestions.add(cmdName);
                }
            }
            
            File currentDir = new File(pathHolder.getCurrentPath());
            File[] files = currentDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && (prefix.isEmpty() || file.getName().toLowerCase().startsWith(prefix))) {
                        suggestions.add(file.getName());
                    }
                }
            }
            
            return suggestions.toArray(new String[0]);
        }
        
        if (args.length == 2 && (args[0].equals(">") || args[0].equals(">>"))) {
            String prefix = args[1].toLowerCase();
            File currentDir = new File(pathHolder.getCurrentPath());
            File[] files = currentDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (prefix.isEmpty() || file.getName().toLowerCase().startsWith(prefix)) {
                        suggestions.add(file.getName());
                    }
                }
            }
            return suggestions.toArray(new String[0]);
        }
        
        CommandInfo commandInfo = commands.get(args[0]);
        if (commandInfo != null) {
            Command cmd = commandInfo.getCommand();
            String[] cmdSuggestions = cmd.getSuggestions(Arrays.copyOfRange(args, 1, args.length));
            if (cmdSuggestions != null && cmdSuggestions.length > 0) {
                return cmdSuggestions;
            }
        }
        
        return suggestions.toArray(new String[0]);
    }

    @Override
    protected void initializeSubCommands() {
    }
} 