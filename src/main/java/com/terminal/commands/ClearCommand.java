package com.terminal.commands;

import javax.swing.JTextPane;

public class ClearCommand extends AbstractCommand {
    private final JTextPane textPane;

    public ClearCommand(JTextPane textPane) {
        super(null, null);
        this.textPane = textPane;
    }

    @Override
    public void execute(String... args) {
        try {
            textPane.setText("");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDescription() {
        return "очистить экран";
    }
} 