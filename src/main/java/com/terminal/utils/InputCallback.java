package com.terminal.utils;

@FunctionalInterface
public interface InputCallback {
    void onInputComplete(String input);
} 