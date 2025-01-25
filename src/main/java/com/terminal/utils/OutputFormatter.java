package com.terminal.utils;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

public class OutputFormatter {
    private static final int DEFAULT_WIDTH = 65;

    public static void appendText(StyledDocument doc, Style style, String text) {
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public static void printBoxedHeader(StyledDocument doc, Style style, String title) {
        try {
            doc.insertString(doc.getLength(), 
                "╭─ " + title + " " + repeat("─", DEFAULT_WIDTH - title.length()) + "╮\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public static void printBoxedFooter(StyledDocument doc, Style style) {
        try {
            doc.insertString(doc.getLength(),
                "╰─" + repeat("─", DEFAULT_WIDTH + 2) + "╯\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public static void printBoxedLine(StyledDocument doc, Style style, String text) {
        try {
            doc.insertString(doc.getLength(),
                "│ " + text + repeat(" ", DEFAULT_WIDTH - text.length() - 3) + "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public static void printError(StyledDocument doc, Style style, String error) {
        try {
            doc.insertString(doc.getLength(),
                "│ Ошибка: " + error + "\n" +
                "╰" + repeat("─", DEFAULT_WIDTH) + "╯\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private static String repeat(String str, int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(str);
        }
        return result.toString();
    }
} 