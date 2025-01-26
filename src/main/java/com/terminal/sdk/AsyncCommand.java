package com.terminal.sdk;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.commands.AbstractCommand;

/**
 * Базовый класс для асинхронных команд с анимацией загрузки.
 */
public abstract class AsyncCommand extends AbstractCommand {
    private static final String[] FRAMES = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
    private static final int FRAME_DELAY = 80;
    private String currentAnimationId;
    private int animationPos;
    private volatile boolean isInterrupted = false;
    
    protected AsyncCommand(StyledDocument doc, Style style) {
        super(doc, style);
        this.isLongRunning = true;
        
        EventManager.getInstance().subscribe(EventType.COMMAND_INTERRUPTED, event -> {
            isInterrupted = true;
        });
    }
    
    /**
     * Начинает новый блок вывода с анимацией
     */
    protected void startOutputBlock() {
        appendWithStyle("\n", style);
        currentAnimationId = "cmd_" + System.currentTimeMillis();
        Animation animation = new Animation() {
            private boolean running = true;
            private int frameIndex = 0;
            
            @Override
            public String getCurrentFrame() {
                return FRAMES[frameIndex % FRAMES.length] + " \n";
            }
            
            @Override
            public void update() {
                frameIndex++;
                EventManager.getInstance().emit(new TerminalEvent(EventType.ANIMATION_FRAME, 
                    new AnimationFrame(currentAnimationId, getCurrentFrame())));
            }
            
            @Override
            public int getFrameDelay() {
                return FRAME_DELAY;
            }
            
            @Override
            public boolean isFinished() {
                return !running;
            }
            
            @Override
            public void start() {
                running = true;
            }
            
            @Override
            public void stop() {
                running = false;
            }
            
            @Override
            public void reset() {
                frameIndex = 0;
            }
        };
        
        animationPos = doc.getLength();
        appendWithStyle(FRAMES[0] + " \n", style);
        
        Consumer<TerminalEvent> animationListener = event -> {
            if (event.getData() instanceof AnimationFrame) {
                AnimationFrame frame = (AnimationFrame) event.getData();
                if (frame.getAnimationId().equals(currentAnimationId)) {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            doc.remove(animationPos, 3);
                            doc.insertString(animationPos, frame.getFrame(), style);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        };
        
        EventManager.getInstance().subscribe(EventType.ANIMATION_FRAME, animationListener);
        AnimationManager.getInstance().registerAnimation(currentAnimationId, animation);
        AnimationManager.getInstance().startAnimation(currentAnimationId);
    }
    
    /**
     * Добавляет новый пакет вывода
     */
    protected void appendOutputPacket(String output) {
        SwingUtilities.invokeLater(() -> {
            try {
                doc.remove(animationPos, 3);
                doc.insertString(animationPos, output + "\n", style);
                animationPos = doc.getLength();
                doc.insertString(animationPos, FRAMES[0] + " \n", style);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    /**
     * Завершает блок вывода
     */
    protected void endOutputBlock() {
        if (currentAnimationId != null) {
            AnimationManager.getInstance().stopAnimation(currentAnimationId);
            SwingUtilities.invokeLater(() -> {
                try {
                    doc.remove(animationPos, 3);
                    appendWithStyle("\n\n", style);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            currentAnimationId = null;
        }
    }
    
    @Override
    public CompletableFuture<Void> executeAsync(String... args) {
        resetInterrupted();
        return super.executeAsync(args);
    }
    
    @Override
    public boolean isLongRunning() {
        return true;
    }
    
    protected boolean isInterrupted() {
        return isInterrupted;
    }
    
    protected void resetInterrupted() {
        isInterrupted = false;
    }
    
    protected void appendWithStyle(String text, Style style) {
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
} 