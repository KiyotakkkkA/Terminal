package com.terminal.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.Command;
import com.terminal.sdk.CommandInfo;
import com.terminal.sdk.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class CatCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;
    private final Map<String, CommandInfo> commands;

    public CatCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder, Map<String, CommandInfo> commands) {
        super(doc, style);
        this.pathHolder = pathHolder;
        this.commands = commands;
    }

    @Override
    public void execute(String... args) {
        try {
            if (args.length < 1) {
                showUsage();
                return;
            }

            String outputFile = null;
            boolean appendMode = false;
            int lastArgIndex = args.length - 1;

            if (args.length >= 3) {
                if (args[args.length - 2].equals(">")) {
                    outputFile = args[lastArgIndex];
                    lastArgIndex -= 2;
                } else if (args[args.length - 2].equals(">>")) {
                    outputFile = args[lastArgIndex];
                    appendMode = true;
                    lastArgIndex -= 2;
                }
            }

            String content;
            String source = args[0];
            
            Path path = Paths.get(pathHolder.getCurrentPath(), source);
            if (Files.exists(path)) {
                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                }
                content = output.toString();
            } else {
                Command cmd = findCommand(source);
                if (cmd != null) {
                    String[] cmdArgs = new String[lastArgIndex];
                    if (lastArgIndex > 0) {
                        System.arraycopy(args, 1, cmdArgs, 0, lastArgIndex);
                    }
                    content = cmd.executeAndGetOutput(cmdArgs);
                    if (content == null) {
                        OutputFormatter.printError(doc, style, "Команда не поддерживает перенаправление: " + source);
                        return;
                    }
                } else {
                    OutputFormatter.printError(doc, style, "Файл не найден и команда не существует: " + source);
                    return;
                }
            }

            if (outputFile != null) {
                Path outputPath = Paths.get(pathHolder.getCurrentPath(), outputFile);
                if (appendMode) {
                    Files.write(outputPath, content.getBytes("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } else {
                    Files.write(outputPath, content.getBytes("UTF-8"), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                }
                OutputFormatter.printBoxedLine(doc, style, "Результат " + (appendMode ? "добавлен в" : "сохранен в") + " " + outputFile);
            } else {
                    doc.insertString(doc.getLength(), content, style);
            }

        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public String executeAndGetOutput(String... args) {
        if (args.length < 1) {
            return "Использование: cat <файл/команда>\n";
        }

        try {
            String source = args[0];
            
            Path path = Paths.get(pathHolder.getCurrentPath(), source);
            if (Files.exists(path)) {
                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(path.toFile()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                }
                return output.toString();
            } else {
                Command cmd = findCommand(source);
                if (cmd != null) {
                    return cmd.executeAndGetOutput(args.length > 1 ? 
                        java.util.Arrays.copyOfRange(args, 1, args.length) : new String[0]);
                }
                return "Ошибка: Файл не найден и команда не существует: " + source + "\n";
            }
        } catch (Exception e) {
            return "Ошибка: " + e.getMessage() + "\n";
        }
    }

    private void showUsage() throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Использование: cat <файл/команда>");
        OutputFormatter.printBoxedLine(doc, style, "Выводит содержимое файла или результат команды");
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Примеры:");
        OutputFormatter.printBoxedLine(doc, style, "  cat file.txt              - вывести содержимое файла");
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Перенаправление вывода:");
        OutputFormatter.printBoxedLine(doc, style, "  cat file.txt > out.txt    - записать файл в out.txt");
        OutputFormatter.printBoxedLine(doc, style, "  cat file.txt >> log.txt   - дописать файл в log.txt");
        OutputFormatter.printBoxedLine(doc, style, "  cat <имя команды> > <имя файла>.txt     - записать результат команды в файл");
        OutputFormatter.printBoxedLine(doc, style, "  cat <имя команды> >> <имя файла>.txt    - дописать результат команды в файл");
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedFooter(doc, style);
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
    public List<String> getSuggestions(String[] args) {
        List<String> suggestions = super.getSuggestions(args);
        
        if (args.length >= 1) {
            Command cmd = findCommand(args[0]);
            if (cmd != null) {
                return cmd.getSuggestions(args.length > 1 ? 
                    java.util.Arrays.copyOfRange(args, 1, args.length) : new String[0]);
            }
        }
        
        File currentDir = new File(pathHolder.getCurrentPath());
        File[] files = currentDir.listFiles();
        if (files != null) {
            String prefix = args.length > 0 ? args[0].toLowerCase() : "";
            for (File file : files) {
                if (file.isFile() && (prefix.isEmpty() || file.getName().toLowerCase().startsWith(prefix))) {
                    suggestions.add(file.getName());
                }
            }
        }
        
        return suggestions;
    }

    @Override
    protected void initializeSubCommands() {
        // Системные команды
        addSubCommand("sys", "системная информация");
        addSubCommand("ps", "список процессов");
        addSubCommand("con", "сетевые подключения");
        addSubCommand("procanalyze", "анализ процессов");
        addSubCommand("cls", "очистка экрана");
        addSubCommand("help", "справка по командам");
        addSubCommand("exit", "выход из терминала");
        addSubCommand("plugins", "управление плагинами");

        // Файловые операции
        addSubCommand("ls", "список файлов");
        addSubCommand("pwd", "текущая директория");
        addSubCommand("cd", "смена директории");
        addSubCommand("mkdir", "создание директории");
        addSubCommand("rmdir", "удаление директории");
        addSubCommand("touch", "создание файла");
        addSubCommand("rm", "удаление файла");
        addSubCommand("nano", "редактирование файла");

        // Сетевые команды
        addSubCommand("ping", "проверка доступности хоста");
        addSubCommand("netstat", "сетевые соединения");
        addSubCommand("trace", "трассировка маршрута");
        addSubCommand("dns", "DNS-запросы");
        addSubCommand("nmap", "сканирование портов");
        addSubCommand("portscan", "сканирование портов");
        addSubCommand("wifi", "сканирование Wi-Fi");
        addSubCommand("web", "веб-инструменты");

        // Поиск и обработка
        addSubCommand("find", "поиск файлов");
        addSubCommand("grep", "поиск в файлах");
        addSubCommand("zip", "архивация");
        addSubCommand("unzip", "разархивация");
        addSubCommand("hash", "хеширование");
        addSubCommand("crypto", "шифрование");
        addSubCommand("fuzz", "фаззинг");
        addSubCommand("reverse", "реверс файлов");
    }
} 