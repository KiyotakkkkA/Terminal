package com.terminal.sdk.core;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.TerminalPanel;
import com.terminal.sdk.system.CurrentPathHolder;

/**
 * Базовый класс для асинхронных команд
 */
public abstract class AsyncCommand extends Command {
    protected final TerminalPanel terminal;
    private final Style style;
    private final StyledDocument doc;
    protected final AtomicBoolean isCancelled = new AtomicBoolean(false);
    
    public AsyncCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder, TerminalPanel terminal) {
        super(doc, style, pathHolder);
        this.terminal = terminal;
        this.style = style;
        this.doc = doc;
    }
    
    @Override
    public final void execute(String[] args) {
        isCancelled.set(false);
        
        CompletableFuture<Void> task = executeAsync(args);
        
        task.whenComplete((result, ex) -> {
            try {
                if (ex != null && !isCancelled.get()) {
                    handleError(ex);
                }
            } finally {
                SwingUtilities.invokeLater(() -> {
                    if (!isCancelled.get()) {
                        terminal.displayPromptPublic();
                    }
                    terminal.unlock();
                    terminal.getTextPane().setEditable(true);
                    terminal.getTextPane().setEnabled(true);
                });
            }
        });
    }
    
    /**
     * Прерывает выполнение команды
     */
    public void cancel() {
        isCancelled.set(true);
    }
    
    /**
     * Проверяет, была ли команда отменена
     */
    protected boolean isCancelled() {
        return isCancelled.get();
    }
    
    /**
     * Асинхронное выполнение команды
     */
    public abstract CompletableFuture<Void> executeAsync(String[] args);
    
    protected void handleError(Throwable ex) {
        try {
            doc.insertString(doc.getLength(), "Ошибка: " + ex.getMessage() + "\n", style);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected CompletableFuture<Void> registerTask(CompletableFuture<Void> task) {
        AsyncTaskManager.getInstance().registerTask(task, terminal.getTerminalId());
        return task;
    }
} 