package com.terminal.commands;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.Command;
import com.terminal.sdk.CurrentPathHolder;

/**
 * Базовый класс для команд терминала.
 * Предоставляет общую функциональность для всех команд.
 */
public abstract class AbstractCommand implements Command {
    protected final StyledDocument doc;
    protected final Style style;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private StyledDocument originalDoc;
    private Style originalStyle;
    protected final Map<String, SubCommand> subCommands;
    protected final CurrentPathHolder pathHolder;
    protected boolean isLongRunning = false;

    protected static class SubCommand {
        private final String name;
        private final String description;
        private final List<String> subCommands;

        public SubCommand(String name, String description) {
            this(name, description, new ArrayList<>());
        }

        public SubCommand(String name, String description, List<String> subCommands) {
            this.name = name;
            this.description = description;
            this.subCommands = subCommands;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public List<String> getSubCommands() {
            return subCommands;
        }
    }

    public AbstractCommand(StyledDocument doc, Style style) {
        this(doc, style, null);
    }

    public AbstractCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        this.doc = doc;
        this.style = style;
        this.pathHolder = pathHolder;
        this.subCommands = new HashMap<>();
        initializeSubCommands();
    }

    protected void initializeSubCommands() {
    }

    protected void addSubCommand(String name, String description) {
        subCommands.put(name, new SubCommand(name, description));
    }

    protected void addSubCommand(String name, String description, List<String> subCommands) {
        this.subCommands.put(name, new SubCommand(name, description, subCommands));
    }

    @Override
    public List<String> getSuggestions(String[] args) {
        if (args.length == 0) {
            return new ArrayList<>(subCommands.keySet());
        }

        String currentArg = args[args.length - 1].toLowerCase();
        String previousArg = args.length > 1 ? args[args.length - 2].toLowerCase() : "";

        if (currentArg.isEmpty()) {
            SubCommand prevSubCommand = subCommands.get(previousArg);
            if (prevSubCommand != null) {
                return prevSubCommand.getSubCommands();
            }
            return new ArrayList<>(subCommands.keySet());
        }

        if (!previousArg.isEmpty()) {
            SubCommand prevSubCommand = subCommands.get(previousArg);
            if (prevSubCommand != null) {
                return prevSubCommand.getSubCommands().stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(currentArg))
                    .collect(java.util.stream.Collectors.toList());
            }
        }

        return subCommands.keySet().stream()
            .filter(cmd -> cmd.toLowerCase().startsWith(currentArg))
            .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public boolean isLongRunning() {
        return shouldRunAsync();
    }

    protected StyledDocument getDoc() {
        return doc;
    }

    protected Style getStyle() {
        return style;
    }

    @Override
    public String executeAndGetOutput(String... args) {
        try {
            originalOut = System.out;
            originalDoc = doc;
            originalStyle = style;
            
            outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            System.setOut(printStream);
            
            execute(args);
            
            System.setOut(originalOut);
            
            return outputStream.toString("UTF-8");
        } catch (Exception e) {
            if (originalOut != null) {
                System.setOut(originalOut);
            }
            return "Ошибка: " + e.getMessage() + "\n";
        }
    }

    protected void writeToOutput(String text) {
        try {
            if (outputStream != null) {
                outputStream.write(text.getBytes("UTF-8"));
            } else {
                doc.insertString(doc.getLength(), text, style);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected Map<String, SubCommand> getSubCommands() {
        return subCommands;
    }

    public List<String> getSubCommandNames() {
        return new ArrayList<>(subCommands.keySet());
    }

    /**
     * Добавляет текст в документ с указанным стилем.
     */
    protected void appendString(String str) {
        try {
            doc.insertString(doc.getLength(), str, style);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Добавляет текст с новой строки.
     */
    protected void appendLine(String str) {
        appendString(str + "\n");
    }

    /**
     * Возвращает текущий путь из держателя пути.
     */
    protected String getCurrentPath() {
        return pathHolder != null ? pathHolder.getCurrentPath() : System.getProperty("user.dir");
    }

    /**
     * Устанавливает текущий путь в держателе пути.
     */
    protected void setCurrentPath(String path) {
        if (pathHolder != null) {
            pathHolder.setCurrentPath(path);
        }
    }

    /**
     * Проверяет, является ли команда длительной операцией.
     * По умолчанию возвращает true для команд, которые:
     * - Работают с сетью
     * - Выполняют сложные вычисления
     * - Обрабатывают большие файлы
     * - Взаимодействуют с внешними процессами
     */
    protected boolean shouldRunAsync() {
        return isLongRunning;
    }
} 