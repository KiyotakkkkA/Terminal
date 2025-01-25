package com.terminal.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.EventManager;
import com.terminal.sdk.EventType;
import com.terminal.sdk.TerminalEvent;
import com.terminal.utils.OutputFormatter;
import com.terminal.utils.ThemeManager;

public class ThemeCommand extends AbstractCommand {
    
    public ThemeCommand(StyledDocument doc, Style style) {
        super(doc, style);
    }

    @Override
    protected void initializeSubCommands() {
        addSubCommand("list", "показать список доступных тем");
        addSubCommand("set", "установить тему");
        addSubCommand("current", "показать текущую тему");
    }

    @Override
    public void execute(String... args) {
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
                    OutputFormatter.appendText(doc, style, "Использование: theme set <имя темы>\n");
                    return;
                }
                String themeName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                setTheme(themeName);
                break;
            case "current":
                showCurrentTheme();
                break;
            default:
                OutputFormatter.appendText(doc, style, "Неизвестная подкоманда. Доступные команды:\n");
                OutputFormatter.appendText(doc, style, "  theme list    - показать список доступных тем\n");
                OutputFormatter.appendText(doc, style, "  theme set     - установить тему\n");
                OutputFormatter.appendText(doc, style, "  theme current - показать текущую тему\n");
        }
    }

    private void listThemes() {
        Set<String> themes = ThemeManager.getInstance().getAvailableThemes();
        OutputFormatter.appendText(doc, style, "Доступные темы:\n");
        for (String theme : themes) {
            OutputFormatter.appendText(doc, style, "  - " + theme + "\n");
        }
    }

    private void setTheme(String themeName) {
        try {
            if (!ThemeManager.getInstance().getAvailableThemes().contains(themeName)) {
                OutputFormatter.appendText(doc, style, "Ошибка: тема '" + themeName + "' не найдена\n");
                return;
            }
            
            ThemeManager.getInstance().setTheme(themeName);
            OutputFormatter.appendText(doc, style, "Тема '" + themeName + "' успешно установлена\n");
            OutputFormatter.appendText(doc, style, "Для применения темы требуется перезапуск терминала\n");
            
            EventManager.getInstance().emit(
                new TerminalEvent(EventType.THEME_CHANGED, themeName)
            );
        } catch (Exception e) {
            OutputFormatter.appendText(doc, style, "Ошибка при установке темы: " + e.getMessage() + "\n");
        }
    }

    private void showCurrentTheme() {
        try {
            String currentTheme = ThemeManager.getInstance().getCurrentTheme().get("name").getAsString();
            OutputFormatter.appendText(doc, style, "Текущая тема: " + currentTheme + "\n");
        } catch (Exception e) {
            OutputFormatter.appendText(doc, style, "Ошибка при получении текущей темы: " + e.getMessage() + "\n");
        }
    }

    @Override
    public String getDescription() {
        return "Управление темами оформления";
    }

    @Override
    public List<String> getSuggestions(String[] args) {
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
        
        return suggestions;
    }
} 