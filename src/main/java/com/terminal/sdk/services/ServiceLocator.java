package com.terminal.sdk.services;

import java.util.HashMap;
import java.util.Map;

public class ServiceLocator {
    private static ServiceLocator instance;
    private final Map<Class<?>, Object> services;

    private ServiceLocator() {
        this.services = new HashMap<>();
    }

    public static synchronized ServiceLocator getInstance() {
        if (instance == null) {
            instance = new ServiceLocator();
        }
        return instance;
    }

    public <T> void register(Class<T> serviceType, T implementation) {
        services.put(serviceType, implementation);
    }

    @SuppressWarnings("unchecked")
    public <T> T resolve(Class<T> serviceType) {
        Object service = services.get(serviceType);
        if (service == null) {
            throw new IllegalStateException("Service not registered: " + serviceType.getName());
        }
        return (T) service;
    }

    public void clear() {
        services.clear();
    }
} 