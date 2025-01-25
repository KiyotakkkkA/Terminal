package com.terminal;

import javax.swing.SwingUtilities;

public class Main {

    private static final String VERSION = "1.1 - ALPHA";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TerminalFrame frame = new TerminalFrame(VERSION);
            frame.setVisible(true);
        });
    }
} 