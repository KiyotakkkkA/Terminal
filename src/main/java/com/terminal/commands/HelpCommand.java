package com.terminal.commands;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.CommandCategory;
import com.terminal.sdk.CommandInfo;
import com.terminal.utils.OutputFormatter;

public class HelpCommand extends AbstractCommand {
    private final Map<String, CommandInfo> commands;

    public HelpCommand(StyledDocument doc, Style style, Map<String, CommandInfo> commands) {
        super(doc, style);
        this.commands = commands;
    }

    @Override
    public void execute(String... args) {
        try {
            OutputFormatter.printBoxedHeader(doc, style, "Доступные команды");

            Map<CommandCategory, List<CommandInfo>> categorizedCommands = commands.values().stream()
                .collect(Collectors.groupingBy(CommandInfo::getCategory));

            for (CommandCategory category : CommandCategory.values()) {
                List<CommandInfo> categoryCommands = categorizedCommands.get(category);
                if (categoryCommands != null && !categoryCommands.isEmpty()) {
                    OutputFormatter.printBoxedLine(doc, style, category.getDescription() + ":");

                    categoryCommands.stream()
                        .sorted(Comparator.comparing(CommandInfo::getName))
                        .forEach(cmd -> {
                            try {
                                String name = cmd.getName();
                                String description = cmd.getCommand().getDescription();
                                
                                OutputFormatter.printBoxedLine(doc, style, 
                                    String.format("  %-12s %s", name, description));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    
                    OutputFormatter.printBoxedLine(doc, style, "");
                }
            }

            OutputFormatter.printBoxedLine(doc, style, "Для подробной справки используйте: <команда> help");
            OutputFormatter.printBoxedFooter(doc, style);

        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, "Ошибка при выполнении команды: " + e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при выполнении команды: " + e.getMessage());
            }

        }
    }

    @Override
    public String getDescription() {
        return "показать список доступных команд";
    }
} 