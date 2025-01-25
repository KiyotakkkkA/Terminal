package com.terminal;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TerminalFrame frame = new TerminalFrame();
            frame.setVisible(true);
        });
    }
} 