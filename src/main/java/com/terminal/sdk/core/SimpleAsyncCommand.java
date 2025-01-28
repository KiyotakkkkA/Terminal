package com.terminal.sdk.core;

import java.util.concurrent.CompletableFuture;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.sdk.services.TerminalService;

/**
 * Простая реализация асинхронной команды
 */
public class SimpleAsyncCommand extends AsyncCommand {
    private final String name;
    private final String description;
    private final String category;
    
    public SimpleAsyncCommand(String name, String description, String category,
                            StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style, pathHolder, TerminalService.getInstance().getTerminalPanel());
        this.name = name;
        this.description = description;
        this.category = category;
    }
    
    @Override
    public CompletableFuture<Void> executeAsync(String[] args) {
        CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
            try {
                System.out.println("Starting async task for terminal " + terminal.getTerminalId());
                while (!isCancelled()) {
                    try {
                        Thread.sleep(100); // Имитация работы
                    } catch (InterruptedException e) {
                        if (isCancelled()) {
                            break;
                        }
                    }
                }
            } finally {
                System.out.println("Async task finished for terminal " + terminal.getTerminalId());
            }
        });
        
        return registerTask(task);
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