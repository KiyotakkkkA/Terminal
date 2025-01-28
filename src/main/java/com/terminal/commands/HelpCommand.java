package com.terminal.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.core.Command;
import com.terminal.sdk.core.CommandCategory;
import com.terminal.sdk.core.CommandInfo;
import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class HelpCommand extends AbstractCommand {
    private final Map<String, CommandInfo> commands;
    private final Style promptStyle;
    public HelpCommand(StyledDocument doc, Style style, Style promptStyle, CurrentPathHolder pathHolder, Map<String, CommandInfo> commands) {
        super(doc, style, pathHolder, "help", "Показать справку по командам", "SYSTEM");
        this.commands = commands;
        this.promptStyle = promptStyle;
    }

    @Override
    public String[] getSuggestions(String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return commands.values().stream()
                .map(cmd -> cmd.getName())
                .filter(name -> name.toLowerCase().startsWith(prefix))
                .toArray(String[]::new);
        }
        return new String[0];
    }

    @Override
    public void executeCommand(String... args) {
        try {
            if (args.length == 0) {
                for (CommandCategory category : CommandCategory.values()) {
                    List<CommandInfo> categoryCommands = commands.values().stream()
                        .filter(cmd -> cmd.getCommand().getCategory().equals(category.toString()))
                        .collect(Collectors.toList());
                        
                    if (!categoryCommands.isEmpty()) {
                        String[][] data = new String[categoryCommands.size()][2];
                        
                        String[] headers = {"Команда", "Описание"};
                        
                        for (int i = 0; i < categoryCommands.size(); i++) {
                            CommandInfo cmd = categoryCommands.get(i);
                            data[i][0] = cmd.getName();
                            data[i][1] = cmd.getCommand().getDescription();
                        }
                        OutputFormatter.printBeautifulSection(doc, promptStyle, category.getDescription());
                        OutputFormatter.printBeautifulTable(doc, style, headers, data);
                    }
                }
                return;
            }

            String commandName = args[0];
            CommandInfo commandInfo = commands.get(commandName);
            
            if (commandInfo == null) {
                printError("Команда не найдена: " + commandName);
                return;
            }
            
            Command command = commandInfo.getCommand();
            printSection("Справка по команде: " + commandName);
            
            printCompactTable(
                new String[]{"Параметр", "Значение"},
                new String[][]{
                    {"Описание", command.getDescription()},
                    {"Категория", command.getCategory().toString()}
                }
            );
            
            String[] suggestions = command.getSuggestions(new String[0]);
            if (suggestions.length > 0) {
                appendLine("\nПримеры использования:");
                String[][] examples = Arrays.stream(suggestions)
                    .map(s -> new String[]{"•", s})
                    .toArray(String[][]::new);
                    
                printMinimalTable(new String[]{"", "Пример"}, examples);
            }
            
            printSectionEnd();
            
        } catch (Exception e) {
            printError(e.getMessage());
        }
    }

    @Override
    public String executeAndGetOutput(String... args) {
        StringBuilder output = new StringBuilder();
        
        if (args.length == 0) {
            output.append("Справочник команд:\n\n");
            
            for (CommandCategory category : CommandCategory.values()) {
                List<CommandInfo> categoryCommands = commands.values().stream()
                    .filter(cmd -> cmd.getCommand().getCategory().equals(category.toString()))
                    .collect(Collectors.toList());
                    
                if (!categoryCommands.isEmpty()) {
                    output.append(category.getDescription()).append(":\n");
                    for (CommandInfo cmd : categoryCommands) {
                        output.append(String.format("  %-15s %s\n", 
                            cmd.getName(), cmd.getCommand().getDescription()));
                    }
                    output.append("\n");
                }
            }
            
            output.append("Для получения подробной информации используйте: help <команда>\n");
            return output.toString();
        }

        String commandName = args[0];
        CommandInfo commandInfo = commands.get(commandName);
        
        if (commandInfo == null) {
            return "Ошибка: Команда не найдена: " + commandName + "\n";
        }
        
        Command command = commandInfo.getCommand();
        output.append("Справка по команде: ").append(commandName).append("\n");
        output.append("Описание: ").append(command.getDescription()).append("\n");
        output.append("Категория: ").append(command.getCategory().toString()).append("\n");
        
        String[] suggestions = command.getSuggestions(new String[0]);
        if (suggestions.length > 0) {
            output.append("\nПримеры использования:\n");
            for (String suggestion : suggestions) {
                output.append("  • ").append(suggestion).append("\n");
            }
        }
        
        return output.toString();
    }

    @Override
    public String getDescription() {
        return "показать справку по командам";
    }
} 