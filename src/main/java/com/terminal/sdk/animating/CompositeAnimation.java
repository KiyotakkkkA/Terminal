package com.terminal.sdk.animating;

import java.util.ArrayList;
import java.util.List;

/**
 * Композитная анимация, объединяющая несколько анимаций в одну.
 */
public class CompositeAnimation extends AbstractAnimation {
    private final List<Animation> animations;
    private StringBuilder currentFrame;

    public CompositeAnimation(String name, int frameDelay, List<Animation> animations) {
        super(name, frameDelay);
        this.animations = new ArrayList<>(animations);
        this.currentFrame = new StringBuilder();
    }

    @Override
    public String getCurrentFrame() {
        currentFrame.setLength(0);
        for (Animation animation : animations) {
            currentFrame.append(animation.getCurrentFrame());
        }
        return currentFrame.toString();
    }

    @Override
    public void update() {
        if (running) {
            for (Animation animation : animations) {
                animation.update();
            }
        }
    }

    @Override
    protected void onStart() {
        for (Animation animation : animations) {
            animation.start();
        }
    }

    @Override
    protected void onStop() {
        for (Animation animation : animations) {
            animation.stop();
        }
    }

    @Override
    protected void onReset() {
        for (Animation animation : animations) {
            animation.reset();
        }
    }

    @Override
    public boolean isFinished() {
        return !running || animations.stream().allMatch(Animation::isFinished);
    }
} 