package com.terminal.sdk.services;

import java.util.function.Consumer;

import com.terminal.sdk.events.EventType;
import com.terminal.sdk.events.TerminalEvent;

public interface IEventManager {
    void subscribe(EventType type, Consumer<TerminalEvent> listener);
    void unsubscribe(EventType type, Consumer<TerminalEvent> listener);
    void emit(TerminalEvent event);
    void clear();
} 