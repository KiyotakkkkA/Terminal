package com.terminal.sdk;

/**
 * Класс, представляющий кадр анимации.
 */
public class AnimationFrame {
    private final String animationId;
    private final String frame;
    
    public AnimationFrame(String animationId, String frame) {
        this.animationId = animationId;
        this.frame = frame;
    }
    
    public String getAnimationId() {
        return animationId;
    }
    
    public String getFrame() {
        return frame;
    }
} 