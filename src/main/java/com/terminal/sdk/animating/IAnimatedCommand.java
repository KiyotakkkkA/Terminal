package com.terminal.sdk.animating;

/**
 * Интерфейс для команд с поддержкой анимации
 */
public interface IAnimatedCommand {
    /**
     * Получить менеджер анимаций
     */
    AnimationManager getAnimationManager();
    
    /**
     * Обработать кадр анимации
     */
    void updateFrame(AnimationFrame frame);
} 