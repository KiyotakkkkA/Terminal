package com.terminal.sdk.output;

/**
 * Интерфейс для управления анимированным выводом в консоль.
 * Обеспечивает контроль над анимацией и выводом текста.
 */
public interface IAnimatedOutput {
    /**
     * Начинает анимацию с указанными параметрами
     * @param frames кадры анимации
     * @param frameDelay задержка между кадрами в мс
     */
    void startAnimation(String[] frames, int frameDelay);
    
    /**
     * Останавливает анимацию
     */
    void stopAnimation();
    
    /**
     * Добавляет текст к текущему выводу, сохраняя анимацию
     * @param text текст для вывода
     */
    void appendText(String text);
    
    /**
     * Останавливает анимацию и завершает вывод
     */
    void complete();
    
    /**
     * Останавливает анимацию и сообщает об ошибке
     * @param error текст ошибки
     */
    void completeWithError(String error);
} 