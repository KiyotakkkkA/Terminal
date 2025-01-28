package com.terminal.sdk.animating;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.terminal.sdk.events.EventManager;
import com.terminal.sdk.events.EventType;
import com.terminal.sdk.events.TerminalEvent;

/**
 * Менеджер анимаций в консоли.
 * Поддерживает как объектные, так и функциональные анимации.
 */
public class AnimationManager implements AutoCloseable {
    private static volatile AnimationManager instance;
    private final ConcurrentHashMap<String, Animation> animations;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> animationTasks;
    private final ScheduledExecutorService executor;
    private final EventManager eventManager;
    
    private AnimationManager() {
        this.animations = new ConcurrentHashMap<>();
        this.animationTasks = new ConcurrentHashMap<>();
        this.executor = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        this.eventManager = EventManager.getInstance();
    }
    
    public static AnimationManager getInstance() {
        AnimationManager instance = AnimationManager.instance;
        if (instance == null) {
            synchronized (AnimationManager.class) {
                if (instance == null) {
                    AnimationManager.instance = instance = new AnimationManager();
                }
            }
        }
        return instance;
    }
    
    /**
     * Регистрирует новую функциональную анимацию
     */
    public void registerAnimation(String id, String[] frames, int frameDelay, Consumer<String> frameHandler) {
        Animation animation = new AbstractAnimation(id, frameDelay) {
            private int frameIndex = 0;
            
            @Override
            public String getCurrentFrame() {
                return frames[frameIndex % frames.length];
            }
            
            @Override
            public void update() {
                frameIndex++;
                frameHandler.accept(getCurrentFrame());
            }
            
            @Override
            protected void onReset() {
                frameIndex = 0;
            }
        };
        
        animations.put(id, animation);
    }
    
    /**
     * Регистрирует новую объектную анимацию
     */
    public void registerAnimation(Animation animation) {
        if (animation instanceof AbstractAnimation) {
            String name = ((AbstractAnimation) animation).getName();
            animations.put(name, animation);
        } else {
            throw new IllegalArgumentException("Animation must extend AbstractAnimation");
        }
    }
    
    public void startAnimation(String name) {
        Animation animation = animations.get(name);
        if (animation != null && !isAnimationRunning(name)) {
            animation.start();
            ScheduledFuture<?> task = executor.scheduleAtFixedRate(() -> {
                try {
                    if (!animation.isFinished()) {
                        animation.update();
                        EventManager.getInstance().emit(new TerminalEvent(EventType.ANIMATION_FRAME, 
                            new AnimationFrame(name, animation.getCurrentFrame())));
                    } else {
                        stopAnimation(name);
                    }
                } catch (Exception e) {
                    stopAnimation(name);
                    EventManager.getInstance().emit(new TerminalEvent(EventType.ANIMATION_ERROR, 
                        new AnimationError(name, e.getMessage())));
                }
            }, 0, animation.getFrameDelay(), TimeUnit.MILLISECONDS);
            animationTasks.put(name, task);
        }
    }
    
    public void stopAnimation(String name) {
        Animation animation = animations.get(name);
        if (animation != null) {
            animation.stop();
            ScheduledFuture<?> task = animationTasks.remove(name);
            if (task != null) {
                task.cancel(false);
            }
            animations.remove(name);
        }
    }
    
    public boolean isAnimationRunning(String name) {
        return animationTasks.containsKey(name);
    }
    
    public Animation getAnimation(String name) {
        return animations.get(name);
    }
    
    @Override
    public void close() {
        animations.keySet().forEach(this::stopAnimation);
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        animations.clear();
        animationTasks.clear();
        instance = null;
    }
} 