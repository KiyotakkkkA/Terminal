package com.terminal.sdk.animating;

/**
 * Представляет ошибку анимации.
 */
public class AnimationError {
    private final String animationName;
    private final String errorMessage;

    public AnimationError(String animationName, String errorMessage) {
        this.animationName = animationName;
        this.errorMessage = errorMessage;
    }

    public String getAnimationName() {
        return animationName;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
} 