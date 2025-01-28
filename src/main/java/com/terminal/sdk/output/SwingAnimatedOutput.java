package com.terminal.sdk.output;

import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.animating.AnimationManager;
import com.terminal.sdk.events.EventManager;
import com.terminal.sdk.events.EventType;
import com.terminal.sdk.events.TerminalEvent;

/**
 * Реализация анимированного вывода для Swing.
 * Обеспечивает безопасную работу с документом в EDT и корректную анимацию.
 */
public class SwingAnimatedOutput implements IAnimatedOutput {
    private final StyledDocument doc;
    private final Style style;
    private final JTextPane textPane;
    private final EventManager eventManager;
    private final AnimationManager animationManager;
    
    private volatile int contentPosition;
    private Timer animationTimer;
    private String[] frames;
    private int currentFrame;
    private volatile boolean isAnimating;
    
    public SwingAnimatedOutput(StyledDocument doc, Style style, JTextPane textPane) {
        this.doc = doc;
        this.style = style;
        this.textPane = textPane;
        this.eventManager = EventManager.getInstance();
        this.animationManager = AnimationManager.getInstance();
        this.isAnimating = false;
    }
    
    @Override
    public void startAnimation(String[] frames, int delay) {
        SwingUtilities.invokeLater(() -> {
            if (animationTimer != null) {
                animationTimer.stop();
            }
            
            this.frames = new String[frames.length];
            for (int i = 0; i < frames.length; i++) {
                this.frames[i] = frames[i] + "\n";
            }
            
            contentPosition = doc.getLength();
            currentFrame = 0;
            isAnimating = true;
            
            try {
                doc.insertString(contentPosition, this.frames[0], style);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
            
            animationTimer = new Timer(delay, e -> {
                if (!isAnimating) {
                    ((Timer)e.getSource()).stop();
                    try {
                        doc.remove(contentPosition, this.frames[currentFrame].length());
                    } catch (BadLocationException ex) {
                    }
                    return;
                }
                
                try {
                    doc.remove(contentPosition, this.frames[currentFrame].length());
                    currentFrame = (currentFrame + 1) % this.frames.length;
                    doc.insertString(contentPosition, this.frames[currentFrame], style);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
            });
            
            animationTimer.start();
        });
    }
    
    @Override
    public void appendText(String text) {
        SwingUtilities.invokeLater(() -> {
            try {
                if (isAnimating) {
                    doc.remove(contentPosition, frames[currentFrame].length());
                }
                
                doc.insertString(doc.getLength(), text, style);
                
                if (isAnimating) {
                    contentPosition = doc.getLength();
                    doc.insertString(contentPosition, frames[currentFrame], style);
                }
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        });
    }
    
    @Override
    public void complete() {
        SwingUtilities.invokeLater(() -> {
            stopAnimation();
            eventManager.emit(new TerminalEvent(EventType.COMMAND_COMPLETED, null));
        });
    }
    
    @Override
    public void completeWithError(String error) {
        SwingUtilities.invokeLater(() -> {
            stopAnimation();
            appendText("\nОшибка: " + error + "\n");
            eventManager.emit(new TerminalEvent(EventType.COMMAND_FAILED, error));
        });
    }
    
    @Override
    public void stopAnimation() {
        SwingUtilities.invokeLater(() -> {
            isAnimating = false;
            if (animationTimer != null) {
                animationTimer.stop();
                try {
                    doc.remove(contentPosition, frames[currentFrame].length());
                } catch (BadLocationException ex) {
                }
                animationTimer = null;
            }
        });
    }
} 