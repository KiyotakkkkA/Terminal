package com.terminal.utils;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.formatting.BoxCharacters;
import com.terminal.sdk.formatting.DefaultBeautifulFormatter;

/**
 * Фасад для форматированного вывода в терминал.
 * Предоставляет упрощенный интерфейс для различных типов форматирования.
 */
public class OutputFormatter {
    private static final int DEFAULT_WIDTH = 65;
    private static final DefaultBeautifulFormatter beautifulFormatter = new DefaultBeautifulFormatter();
    

    public static void startOutputCapture() {
        DefaultBeautifulFormatter.startOutputCapture();
    }

    public static void stopOutputCapture() {
        DefaultBeautifulFormatter.stopOutputCapture();
    }

    public static String getCapturedOutput() {
        return DefaultBeautifulFormatter.getOutputBuffer().toString();
    }

    /**
     * Добавляет текст в документ с указанным стилем
     */
    public static void appendText(StyledDocument doc, String text, Style style) {
        if (DefaultBeautifulFormatter.isCapturingOutput()) {
            DefaultBeautifulFormatter.getOutputBuffer().append(text);
        } else {
            try {
                doc.insertString(doc.getLength(), text, style);
            } catch (BadLocationException ex) {
            }
        }
    }

    /**
     * Выводит заголовок в рамке
     */
    public static void printBoxedHeader(StyledDocument doc, Style style, String title) {
        appendText(doc, BoxCharacters.TOP_LEFT + "─ " + title + " " + repeat("─", DEFAULT_WIDTH - title.length() - 1) + BoxCharacters.TOP_RIGHT + "\n", style);
    }

    /**
     * Выводит нижнюю границу рамки
     */
    public static void printBoxedFooter(StyledDocument doc, Style style) {
        appendText(doc, BoxCharacters.BOTTOM_LEFT + repeat("─", DEFAULT_WIDTH + 2) + BoxCharacters.BOTTOM_RIGHT + "\n", style);
    }

    /**
     * Выводит строку в рамке
     */
    public static void printBoxedLine(StyledDocument doc, Style style, String text) {
        appendText(doc, BoxCharacters.VERTICAL + " " + text + repeat(" ", DEFAULT_WIDTH - text.length() - 3) + "\n", style);
    }

    /**
     * Выводит сообщение об ошибке
     */
    public static void printError(StyledDocument doc, Style style, String error) {
        appendText(doc, BoxCharacters.VERTICAL + " Ошибка: " + error + "\n" +
            BoxCharacters.BOTTOM_LEFT + repeat("─", DEFAULT_WIDTH) + BoxCharacters.BOTTOM_RIGHT + "\n", style);
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

    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
} 