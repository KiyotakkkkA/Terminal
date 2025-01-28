package com.terminal.commands;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.core.Command;
import com.terminal.sdk.core.CommandInfo;
import com.terminal.sdk.formatting.DefaultBeautifulFormatter;
import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

/**
 * Базовый класс для команд терминала.
 * Предоставляет общую функциональность для всех команд.
 */
public abstract class AbstractCommand extends Command {
    protected final StyledDocument doc;
    protected final Style style;
    private ByteArrayOutputStream outputStream;
    private PrintStream originalOut;
    private StyledDocument originalDoc;
    private Style originalStyle;
    protected final Map<String, SubCommand> subCommands;
    protected final CurrentPathHolder pathHolder;
    protected boolean isLongRunning = false;
    private final String name;
    private final String description;
    private final String category;
    protected static final DefaultBeautifulFormatter formatter = new DefaultBeautifulFormatter();
    private boolean isRedirected = false;

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
        this(doc, style, null, "", "", "OTHER");
    }

    public AbstractCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        this(doc, style, pathHolder, "", "", "OTHER");
    }

    protected AbstractCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder, 
                            String name, String description, String category) {
        super(doc, style, pathHolder);
        this.doc = doc;
        this.style = style;
        this.pathHolder = pathHolder;
        this.subCommands = new HashMap<>();
        this.name = name;
        this.description = description;
        this.category = category;
        initializeSubCommands();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public CommandInfo getInfo() {
        return new CommandInfo(name, description, category, this);
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public String getDescription() {
        return description;
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
    public String[] getSuggestions(String[] args) {
        List<String> suggestions = new ArrayList<>(subCommands.keySet());
        return suggestions.toArray(new String[0]);
    }

    @Override
    public void execute(String[] args) {
        try {
            executeCommand(args);
        } catch (Exception e) {
            printError(e.getMessage());
        }
    }

    protected abstract void executeCommand(String... args) throws Exception;

    @Override
    public String executeAndGetOutput(String... args) {
        OutputFormatter.startOutputCapture();
        try {
            executeCommand(args);
            return OutputFormatter.getCapturedOutput();
        } catch (Exception e) {
            return "Ошибка: " + e.getMessage() + "\n";
        } finally {
            OutputFormatter.stopOutputCapture();
        }
    }

    protected boolean shouldRunAsync() {
        return isLongRunning;
    }

    protected void printError(String message) {
        try {
            formatter.printMessage(doc, style, "Ошибка: " + message);
        } catch (Exception e) {
            System.err.println("Ошибка при выводе сообщения: " + e.getMessage());
        }
    }
    
    protected void printMessage(String message) {
        try {
            formatter.printMessage(doc, style, message);
        } catch (Exception e) {
            System.err.println("Ошибка при выводе сообщения: " + e.getMessage());
        }
    }
    
    protected void printSection(String title) {
        try {
            formatter.printSection(doc, style, title);
        } catch (Exception e) {
            System.err.println("Ошибка при выводе секции: " + e.getMessage());
        }
    }
    
    protected void printTable(String[] headers, String[][] data) {
        try {
            formatter.printTable(doc, style, headers, data);
        } catch (Exception e) {
            System.err.println("Ошибка при выводе таблицы: " + e.getMessage());
        }
    }
    
    protected void printCompactTable(String[] headers, String[][] data) {
        try {
            formatter.printCompactTable(doc, style, headers, data);
        } catch (Exception e) {
            System.err.println("Ошибка при выводе таблицы: " + e.getMessage());
        }
    }
    
    protected void printMinimalTable(String[] headers, String[][] data) {
        try {
            formatter.printMinimalTable(doc, style, headers, data);
        } catch (Exception e) {
            System.err.println("Ошибка при выводе таблицы: " + e.getMessage());
        }
    }
    
    protected void printSectionEnd() {
        try {
            formatter.printSectionEnd(doc, style);
        } catch (Exception e) {
            System.err.println("Ошибка при завершении секции: " + e.getMessage());
        }
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
    protected void appendLine(String text) {
        try {
            doc.insertString(doc.getLength(), text + "\n", style);
        } catch (Exception e) {
            System.err.println("Ошибка при выводе строки: " + e.getMessage());
        }
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
} 