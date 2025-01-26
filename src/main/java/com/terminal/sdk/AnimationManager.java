package com.terminal.sdk;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Менеджер анимаций в консоли.
 * Управляет жизненным циклом анимаций и их обновлением.
 */
public class AnimationManager {
    private static AnimationManager instance;
    private final ConcurrentHashMap<String, Animation> animations;
    private final ConcurrentHashMap<String, ScheduledFuture<?>> animationTasks;
    private final ScheduledExecutorService executor;
    private final EventManager eventManager;
    
    private AnimationManager() {
        this.animations = new ConcurrentHashMap<>();
        this.animationTasks = new ConcurrentHashMap<>();
        this.executor = Executors.newScheduledThreadPool(1);
        this.eventManager = EventManager.getInstance();
    }
    
    /**
     * Возвращает единственный экземпляр менеджера анимаций.
     */
    public static synchronized AnimationManager getInstance() {
        if (instance == null) {
            instance = new AnimationManager();
        }
        return instance;
    }
    
    /**
     * Регистрирует новую анимацию.
     * @param id уникальный идентификатор анимации
     * @param animation объект анимации
     */
    public void registerAnimation(String id, Animation animation) {
        animations.put(id, animation);
    }
    
    /**
     * Запускает анимацию по идентификатору.
     * @param id идентификатор анимации
     */
    public void startAnimation(String id) {
        Animation animation = animations.get(id);
        if (animation != null && !isAnimationRunning(id)) {
            animation.start();
            ScheduledFuture<?> task = executor.scheduleAtFixedRate(() -> {
                if (!animation.isFinished()) {
                    animation.update();
                    eventManager.emit(new TerminalEvent(EventType.ANIMATION_FRAME, 
                        new AnimationFrame(id, animation.getCurrentFrame())));
                } else {
                    stopAnimation(id);
                }
            }, 0, animation.getFrameDelay(), TimeUnit.MILLISECONDS);
            animationTasks.put(id, task);
        }
    }
    
    /**
     * Останавливает анимацию по идентификатору.
     * @param id идентификатор анимации
     */
    public void stopAnimation(String id) {
        Animation animation = animations.get(id);
        if (animation != null) {
            animation.stop();
            ScheduledFuture<?> task = animationTasks.remove(id);
            if (task != null) {
                task.cancel(false);
            }
            animations.remove(id);
        }
    }
    
    /**
     * Проверяет, запущена ли анимация.
     * @param id идентификатор анимации
     * @return true, если анимация запущена
     */
    public boolean isAnimationRunning(String id) {
        return animationTasks.containsKey(id);
    }
    
    /**
     * Останавливает все анимации и освобождает ресурсы.
     */
    public void shutdown() {
        animations.keySet().forEach(this::stopAnimation);
        executor.shutdown();
        animations.clear();
        animationTasks.clear();
    }
}