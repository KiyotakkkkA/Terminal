package com.terminal.sdk.animations;

import com.terminal.sdk.Animation;

public class NetworkScanAnimation implements Animation {
    private static final String[] FRAMES = {"⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏"};
    private static final int FRAME_DELAY = 80;
    private boolean running = true;
    private int frameIndex = 0;

    @Override
    public String getCurrentFrame() {
        return FRAMES[frameIndex % FRAMES.length] + " \n";
    }

    @Override
    public void update() {
        frameIndex++;
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
} 