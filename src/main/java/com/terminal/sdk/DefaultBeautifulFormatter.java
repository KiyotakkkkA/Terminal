package com.terminal.sdk;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

public class DefaultBeautifulFormatter implements BeautifulFormatter {
    
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
    
    @Override
    public void printSection(StyledDocument doc, Style style, String title) {
        try {
            doc.insertString(doc.getLength(), BOX_TOP_LEFT + repeat(BOX_HORIZONTAL, 2) + " " + title + " ", style);
            doc.insertString(doc.getLength(), repeat(BOX_HORIZONTAL, 80 - title.length() - 4) + "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void printTable(StyledDocument doc, Style style, String[] headers, String[][] data) {
        try {
            // Вычисляем ширину столбцов
            int[] colWidths = new int[headers.length];
            for (int i = 0; i < headers.length; i++) {
                colWidths[i] = getMaxWidth(getColumn(data, i));
                colWidths[i] = Math.max(colWidths[i], headers[i].length());
            }
            
            // Верхняя граница
            doc.insertString(doc.getLength(), BOX_TOP_LEFT, style);
            for (int i = 0; i < headers.length; i++) {
                doc.insertString(doc.getLength(), repeat(BOX_HORIZONTAL, colWidths[i] + 2), style);
                doc.insertString(doc.getLength(), i < headers.length - 1 ? BOX_T_DOWN : BOX_TOP_RIGHT, style);
            }
            doc.insertString(doc.getLength(), "\n", style);
            
            // Заголовки
            doc.insertString(doc.getLength(), BOX_VERTICAL, style);
            for (int i = 0; i < headers.length; i++) {
                doc.insertString(doc.getLength(), " " + padRight(headers[i], colWidths[i]) + " ", style);
                doc.insertString(doc.getLength(), BOX_VERTICAL, style);
            }
            doc.insertString(doc.getLength(), "\n", style);
            
            // Разделитель
            doc.insertString(doc.getLength(), BOX_T_RIGHT, style);
            for (int i = 0; i < headers.length; i++) {
                doc.insertString(doc.getLength(), repeat(BOX_HORIZONTAL, colWidths[i] + 2), style);
                doc.insertString(doc.getLength(), i < headers.length - 1 ? BOX_CROSS : BOX_T_LEFT, style);
            }
            doc.insertString(doc.getLength(), "\n", style);
            
            // Данные
            for (String[] row : data) {
                doc.insertString(doc.getLength(), BOX_VERTICAL, style);
                for (int i = 0; i < row.length; i++) {
                    doc.insertString(doc.getLength(), " " + padRight(row[i], colWidths[i]) + " ", style);
                    doc.insertString(doc.getLength(), BOX_VERTICAL, style);
                }
                doc.insertString(doc.getLength(), "\n", style);
            }
            
            // Нижняя граница
            doc.insertString(doc.getLength(), BOX_BOTTOM_LEFT, style);
            for (int i = 0; i < headers.length; i++) {
                doc.insertString(doc.getLength(), repeat(BOX_HORIZONTAL, colWidths[i] + 2), style);
                doc.insertString(doc.getLength(), i < headers.length - 1 ? BOX_T_UP : BOX_BOTTOM_RIGHT, style);
            }
            doc.insertString(doc.getLength(), "\n", style);
            
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void printMessage(StyledDocument doc, Style style, String message) {
        try {
            // Верхняя граница
            doc.insertString(doc.getLength(), BOX_TOP_LEFT + repeat(BOX_HORIZONTAL, message.length() + 2) + BOX_TOP_RIGHT + "\n", style);
            
            // Сообщение
            doc.insertString(doc.getLength(), BOX_VERTICAL + " " + message + " " + BOX_VERTICAL + "\n", style);
            
            // Нижняя граница
            doc.insertString(doc.getLength(), BOX_BOTTOM_LEFT + repeat(BOX_HORIZONTAL, message.length() + 2) + BOX_BOTTOM_RIGHT + "\n", style);
            
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void printSectionEnd(StyledDocument doc, Style style) {
        try {
            doc.insertString(doc.getLength(), repeat(BOX_HORIZONTAL, 80) + "\n", style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    private String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    private String padRight(String str, int length) {
        return str + repeat(" ", length - str.length());
    }
    
    private int getMaxWidth(String[] data) {
        int max = 0;
        for (String s : data) {
            max = Math.max(max, s.length());
        }
        return max;
    }
    
    private String[] getColumn(String[][] data, int col) {
        String[] column = new String[data.length];
        for (int i = 0; i < data.length; i++) {
            column[i] = data[i][col];
        }
        return column;
    }
} 