package com.terminal.sdk;

import java.util.concurrent.CompletableFuture;

import javax.swing.SwingUtilities;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.commands.AbstractCommand;
import com.terminal.sdk.core.AsyncTaskManager;
import com.terminal.sdk.core.IAsyncCommand;
import com.terminal.sdk.output.IAnimatedOutput;
import com.terminal.sdk.output.SwingAnimatedOutput;
import com.terminal.sdk.services.TerminalService;
import com.terminal.sdk.system.CurrentPathHolder;

/**
 * Базовый класс для асинхронных команд с анимацией загрузки.
 */
public abstract class AbstractAsyncCommand extends AbstractCommand implements IAsyncCommand {
    protected static final String[] FRAMES = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
    protected static final int FRAME_DELAY = 80;
    protected static final String ANIMATION_SUFFIX = "\n";
    
    protected final IAnimatedOutput output;
    private volatile boolean isInterrupted = false;
    protected boolean isLongRunning = true;
    
    protected AbstractAsyncCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style, pathHolder);
        this.output = new SwingAnimatedOutput(doc, style, TerminalService.getInstance().getTerminalPanel().getTextPane());
    }
    
    /**
     * Начинает асинхронное выполнение команды
     */
    @Override
    public CompletableFuture<Void> executeAsync(String[] args) {
        resetInterrupted();
        output.startAnimation(FRAMES, FRAME_DELAY);
        
        CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
            try {
                if (!isInterrupted) {
                    execute(args);
                }
                if (!isInterrupted) {
                    SwingUtilities.invokeLater(() -> output.complete());
                }
            } catch (Exception e) {
                if (!isInterrupted) {
                    SwingUtilities.invokeLater(() -> output.completeWithError(e.getMessage()));
                    throw new RuntimeException(e);
                }
            }
        });
        
        int terminalId = TerminalService.getInstance().getTerminalPanel().getTerminalId();
        AsyncTaskManager.getInstance().registerTask(task, terminalId);
        return task;
    }
    
    /**
     * Добавляет новый пакет вывода
     */
    protected void appendOutputPacket(String text) {
        output.appendText(text);
    }
    
    @Override
    public boolean isLongRunning() {
        return isLongRunning;
    }
    
    protected boolean isInterrupted() {
        return isInterrupted;
    }
    
    protected void resetInterrupted() {
        isInterrupted = false;
    }
    
    protected void appendWithStyle(String text, Style style) {
        output.appendText(text);
    }
    
    @Override
    public void interrupt() {
        isInterrupted = true;
        SwingUtilities.invokeLater(() -> {
            output.stopAnimation();
        });
    }

    @Override
    public void execute(String[] args) {
        if (!isInterrupted) {
            executeAsync(args);
        }
    }
} 