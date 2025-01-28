package com.terminal.sdk.core;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.system.CurrentPathHolder;

/**
 * Простая реализация команды
 */
public class SimpleCommand extends Command {
    private final String name;
    private final String description;
    private final String category;
    
    public SimpleCommand(String name, String description, String category,
                        StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style, pathHolder);
        this.name = name;
        this.description = description;
        this.category = category;
    }
    
    @Override
    public void execute(String[] args) {
    }
    
    @Override
    public String[] getSuggestions(String[] args) {
        return new String[0];
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getCategory() {
        return category;
    }
    
    @Override
    public CommandInfo getInfo() {
        return new CommandInfo(name, description, category, this);
    }
} 