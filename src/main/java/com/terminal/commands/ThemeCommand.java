package com.terminal.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.events.EventManager;
import com.terminal.sdk.events.EventType;
import com.terminal.sdk.events.TerminalEvent;
import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;
import com.terminal.utils.ThemeManager;

public class ThemeCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;

    public ThemeCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style, pathHolder, "theme", "Управление темами оформления", "SYSTEM");
        this.pathHolder = pathHolder;
    }

    @Override
    protected void initializeSubCommands() {
        addSubCommand("list", "показать список доступных тем");
        addSubCommand("set", "установить тему");
        addSubCommand("current", "показать текущую тему");
    }

    @Override
    public void executeCommand(String... args) {
        if (args.length == 0) {
            listThemes();
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "list":
                listThemes();
                break;
            case "set":
                if (args.length < 2) {
                    OutputFormatter.appendText(doc, "Использование: theme set <имя темы>\n", style);
                    return;
                }
                String themeName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                setTheme(themeName);
                break;
            case "current":
                showCurrentTheme();
                break;
            default:
                OutputFormatter.appendText(doc, "Неизвестная подкоманда. Доступные команды:\n", style);
                OutputFormatter.appendText(doc, "  theme list    - показать список доступных тем\n", style);
                OutputFormatter.appendText(doc, "  theme set     - установить тему\n", style);
                OutputFormatter.appendText(doc, "  theme current - показать текущую тему\n", style);
        }
    }

    private void listThemes() {
        Set<String> themes = ThemeManager.getInstance().getAvailableThemes();
        OutputFormatter.appendText(doc, "Доступные темы:\n", style);
        for (String theme : themes) {
            OutputFormatter.appendText(doc, "  - " + theme + "\n", style);
        }
    }

    private void setTheme(String themeName) {
        try {
            if (!ThemeManager.getInstance().getAvailableThemes().contains(themeName)) {
                OutputFormatter.appendText(doc, "Ошибка: тема '" + themeName + "' не найдена\n", style);
                return;
            }
            
            ThemeManager.getInstance().setTheme(themeName);
            OutputFormatter.appendText(doc, "Тема '" + themeName + "' успешно установлена\n", style);
            OutputFormatter.appendText(doc, "Для применения темы требуется перезапуск терминала\n", style);
            
            EventManager.getInstance().emit(
                new TerminalEvent(EventType.THEME_CHANGED, themeName)
            );
        } catch (Exception e) {
            OutputFormatter.appendText(doc, "Ошибка при установке темы: " + e.getMessage() + "\n", style);
        }
    }

    private void showCurrentTheme() {
        try {
            String currentTheme = ThemeManager.getInstance().getCurrentTheme().get("name").getAsString();
            OutputFormatter.appendText(doc, "Текущая тема: " + currentTheme + "\n", style);
        } catch (Exception e) {
            OutputFormatter.appendText(doc, "Ошибка при получении текущей темы: " + e.getMessage() + "\n", style);
        }
    }

    @Override
    public String[] getSuggestions(String[] args) {
        List<String> suggestions = new ArrayList<>();
        
        if (args.length == 0 || args.length == 1) {
            suggestions.add("list");
            suggestions.add("set");
            suggestions.add("current");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            String prefix = args[1].toLowerCase();
            for (String theme : ThemeManager.getInstance().getAvailableThemes()) {
                if (theme.toLowerCase().startsWith(prefix)) {
                    suggestions.add(theme);
                }
            }
        }
        
        return suggestions.toArray(new String[0]);
    }
} 