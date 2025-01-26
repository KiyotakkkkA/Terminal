package com.terminal.utils;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.BeautifulFormatter;
import com.terminal.sdk.DefaultBeautifulFormatter;

public class OutputFormatter {
    private static final int DEFAULT_WIDTH = 65;
    private static final String BOX_HORIZONTAL = "─";
    private static final String BOX_VERTICAL = "│";
    private static final String BOX_TOP_LEFT = "┌";
    private static final String BOX_TOP_RIGHT = "┐";
    private static final String BOX_BOTTOM_LEFT = "└";
    private static final String BOX_BOTTOM_RIGHT = "┘";
    private static final String BOX_T_DOWN = "┬";
    private static final String BOX_T_UP = "┴";
    private static final String BOX_T_RIGHT = "├";
    private static final String BOX_T_LEFT = "┤";
    private static final String BOX_CROSS = "┼";
    
    private static final BeautifulFormatter beautifulFormatter = new DefaultBeautifulFormatter();

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

    public static void printBoxedTable(StyledDocument doc, Style style, String title, String[][] data) throws Exception {
        int[] colWidths = new int[2];
        colWidths[0] = "Параметр".length();
        colWidths[1] = "Значение".length();
        
        for (String[] row : data) {
            colWidths[0] = Math.max(colWidths[0], row[0].length());
            colWidths[1] = Math.max(colWidths[1], row[1].length());
        }
        
        int totalWidth = colWidths[0] + colWidths[1] + 7;
        printBoxedHeader(doc, style, title);
        
        String headerLine = BOX_VERTICAL + " " + padRight("Параметр", colWidths[0]) + " " + BOX_VERTICAL + " " 
                        + padRight("Значение", colWidths[1]) + " " + BOX_VERTICAL;
        doc.insertString(doc.getLength(), headerLine + "\n", style);
        
        String separator = BOX_T_RIGHT + repeat(BOX_HORIZONTAL, colWidths[0] + 2) + BOX_CROSS 
                        + repeat(BOX_HORIZONTAL, colWidths[1] + 2) + BOX_T_LEFT;
        doc.insertString(doc.getLength(), separator + "\n", style);
        
        for (String[] row : data) {
            String line = BOX_VERTICAL + " " + padRight(row[0], colWidths[0]) + " " + BOX_VERTICAL + " " 
                       + padRight(row[1], colWidths[1]) + " " + BOX_VERTICAL;
            doc.insertString(doc.getLength(), line + "\n", style);
        }
        
        String bottomLine = BOX_BOTTOM_LEFT + repeat(BOX_HORIZONTAL, colWidths[0] + 2) + BOX_T_UP 
                        + repeat(BOX_HORIZONTAL, colWidths[1] + 2) + BOX_BOTTOM_RIGHT;
        doc.insertString(doc.getLength(), bottomLine + "\n", style);
    }

    public static void printTable(StyledDocument doc, Style style, String[] headers, String[][] data) throws Exception {
        int[] colWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            colWidths[i] = headers[i].length();
            for (String[] row : data) {
                colWidths[i] = Math.max(colWidths[i], row[i].length());
            }
        }
        
        StringBuilder headerLine = new StringBuilder(BOX_VERTICAL + " ");
        for (int i = 0; i < headers.length; i++) {
            headerLine.append(padRight(headers[i], colWidths[i])).append(" ").append(BOX_VERTICAL).append(" ");
        }
        doc.insertString(doc.getLength(), headerLine.toString() + "\n", style);
        
        StringBuilder separator = new StringBuilder(BOX_T_RIGHT);
        for (int i = 0; i < headers.length; i++) {
            separator.append(repeat(BOX_HORIZONTAL, colWidths[i] + 2));
            separator.append(i < headers.length - 1 ? BOX_CROSS : BOX_T_LEFT);
        }
        doc.insertString(doc.getLength(), separator.toString() + "\n", style);
        
        for (String[] row : data) {
            StringBuilder line = new StringBuilder(BOX_VERTICAL + " ");
            for (int i = 0; i < row.length; i++) {
                line.append(padRight(row[i], colWidths[i])).append(" ").append(BOX_VERTICAL).append(" ");
            }
            doc.insertString(doc.getLength(), line.toString() + "\n", style);
        }
        
        StringBuilder bottomLine = new StringBuilder(BOX_BOTTOM_LEFT);
        for (int i = 0; i < headers.length; i++) {
            bottomLine.append(repeat(BOX_HORIZONTAL, colWidths[i] + 2));
            bottomLine.append(i < headers.length - 1 ? BOX_T_UP : BOX_BOTTOM_RIGHT);
        }
        doc.insertString(doc.getLength(), bottomLine.toString() + "\n", style);
    }

    private static String repeat(String str, int count) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < count; i++) {
            result.append(str);
        }
        return result.toString();
    }

    private static String padRight(String s, int n) {
        return String.format("%-" + n + "s", s);
    }

    private static int getMaxWidth() {
        return 80;
    }

    public static void printBeautifulSection(StyledDocument doc, Style style, String title) {
        beautifulFormatter.printSection(doc, style, title);
    }

    public static void printBeautifulTable(StyledDocument doc, Style style, String[] headers, String[][] data) {
        beautifulFormatter.printTable(doc, style, headers, data);
    }

    public static void printBeautifulMessage(StyledDocument doc, Style style, String message) {
        beautifulFormatter.printMessage(doc, style, message);
    }

    public static void printBeautifulSectionEnd(StyledDocument doc, Style style) {
        beautifulFormatter.printSectionEnd(doc, style);
    }
} 