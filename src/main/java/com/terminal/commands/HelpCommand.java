package com.terminal.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.core.Command;
import com.terminal.sdk.core.CommandCategory;
import com.terminal.sdk.core.CommandContext;
import com.terminal.sdk.core.CommandInfo;
import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class HelpCommand extends Command {
    private final Style promptStyle;
    private final Map<String, CommandInfo> commands;

    public HelpCommand(StyledDocument doc, Style style, Style promptStyle, CurrentPathHolder pathHolder, Map<String, CommandInfo> commands) {
        super(doc, style, pathHolder);
        this.promptStyle = promptStyle;
        this.commands = commands;
    }

    @Override
    public void execute(CommandContext context) {
        try {
            if (context.getArgs().length == 0) {
                for (CommandCategory category : CommandCategory.values()) {
                    List<CommandInfo> categoryCommands = commands.values().stream()
                        .filter(cmd -> cmd.getCategory().equals(category.name()))
                        .collect(Collectors.toList());
                        
                    if (!categoryCommands.isEmpty()) {
                        String[][] data = new String[categoryCommands.size()][2];
                        String[] headers = {"Команда", "Описание"};
                        
                        for (int i = 0; i < categoryCommands.size(); i++) {
                            CommandInfo cmd = categoryCommands.get(i);
                            data[i][0] = cmd.getName();
                            data[i][1] = cmd.getDescription();
                        }
                        
                        OutputFormatter.printBeautifulSection(doc, promptStyle, category.getDescription());
                        OutputFormatter.printBeautifulTable(doc, style, headers, data);
                    }
                }
                return;
            }

            String commandName = context.getArgs()[0];
            CommandInfo commandInfo = commands.get(commandName);
            
            if (commandInfo == null) {
                OutputFormatter.printError(doc, style, "Команда не найдена: " + commandName);
                return;
            }
            
            Command command = commandInfo.getCommand();
            OutputFormatter.printBeautifulSection(doc, promptStyle, "Справка по команде: " + commandName);
            
            OutputFormatter.printBeautifulTable(doc, style,
                new String[]{"Параметр", "Значение"},
                new String[][]{
                    {"Описание", command.getDescription()},
                    {"Категория", command.getCategory()}
                }
            );
            
            String[] suggestions = command.getSuggestions(new String[0]);
            if (suggestions.length > 0) {
                OutputFormatter.printBeautifulMessage(doc, style, "\nПримеры использования:");
                OutputFormatter.printBeautifulTable(doc, style,
                    new String[]{"", "Пример"},
                    Arrays.stream(suggestions)
                        .map(s -> new String[]{"•", s})
                        .toArray(String[][]::new)
                );
            }
            
            OutputFormatter.printBeautifulSectionEnd(doc, style);
            
        } catch (Exception e) {
            e.printStackTrace();
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public String[] getSuggestions(String[] args) {
        if (args.length == 1) {
            String prefix = args[0].toLowerCase();
            return commands.keySet().stream()
                .filter(cmd -> cmd.startsWith(prefix))
                .toArray(String[]::new);
        }
        return new String[0];
    }

    @Override
    public String getDescription() {
        return "Показать справку по командам";
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getCategory() {
        return "SYSTEM";
    }

    @Override
    public CommandInfo getInfo() {
        return new CommandInfo(getName(), getDescription(), getCategory(), this);
    }
} 