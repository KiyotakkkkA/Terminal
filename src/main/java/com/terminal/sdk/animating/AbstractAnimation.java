package com.terminal.sdk.animating;

/**
 * Абстрактный базовый класс для анимаций.
 */
public abstract class AbstractAnimation implements Animation {
    protected boolean running = false;
    protected int frameDelay;
    protected String name;

    protected AbstractAnimation(String name, int frameDelay) {
        this.name = name;
        this.frameDelay = frameDelay;
    }

    @Override
    public void start() {
        running = true;
        onStart();
    }

    @Override
    public void stop() {
        running = false;
        onStop();
    }

    @Override
    public void reset() {
        onReset();
    }

    @Override
    public boolean isFinished() {
        return !running;
    }

    @Override
    public int getFrameDelay() {
        return frameDelay;
    }

    public String getName() {
        return name;
    }

    protected void onStart() {}
    protected void onStop() {}
    protected void onReset() {}
} 