package com.terminal.sdk.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncTaskManager {
    private static AsyncTaskManager instance;
    private final Map<Integer, List<CompletableFuture<Void>>> terminalTasks;
    private final Map<Integer, AtomicBoolean> terminalStates;
    
    private AsyncTaskManager() {
        this.terminalTasks = new ConcurrentHashMap<>();
        this.terminalStates = new ConcurrentHashMap<>();
    }
    
    public static synchronized AsyncTaskManager getInstance() {
        if (instance == null) {
            instance = new AsyncTaskManager();
        }
        return instance;
    }
    
    public void registerTask(CompletableFuture<Void> task, int terminalId) {
        synchronized(this) {
            List<CompletableFuture<Void>> tasks = terminalTasks.computeIfAbsent(terminalId, 
                k -> new ArrayList<>());
            
            tasks.add(task);
            
            terminalStates.computeIfAbsent(terminalId, 
                k -> new AtomicBoolean()).set(true);
            
            task.whenComplete((result, ex) -> {
                synchronized(AsyncTaskManager.this) {
                    List<CompletableFuture<Void>> currentTasks = terminalTasks.get(terminalId);
                    if (currentTasks != null) {
                        currentTasks.remove(task);
                        if (currentTasks.isEmpty()) {
                            terminalTasks.remove(terminalId);
                            AtomicBoolean state = terminalStates.get(terminalId);
                            if (state != null) {
                                state.set(false);
                            }
                        }
                    }
                }
            });
        }
    }
    
    public boolean hasActiveTask(int terminalId) {
        AtomicBoolean state = terminalStates.get(terminalId);
        return state != null && state.get();
    }
    
    public void cancelAllTasks(int terminalId) {
        synchronized(this) {
            AtomicBoolean state = terminalStates.get(terminalId);
            if (state != null) {
                state.set(false);
            }
            
            List<CompletableFuture<Void>> tasks = terminalTasks.get(terminalId);
            if (tasks != null) {
                List<CompletableFuture<Void>> tasksCopy = new ArrayList<>(tasks);
                
                terminalTasks.remove(terminalId);
                
                for (CompletableFuture<Void> task : tasksCopy) {
                    if (!task.isDone() && !task.isCancelled()) {
                        task.cancel(true);
                        
                        try {
                            task.handle((result, ex) -> null).get();
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }
} 