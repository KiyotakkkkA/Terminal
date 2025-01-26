package com.terminal.sdk;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

public interface BeautifulFormatter {
    void printSection(StyledDocument doc, Style style, String title);
    void printTable(StyledDocument doc, Style style, String[] headers, String[][] data);
    void printMessage(StyledDocument doc, Style style, String message);
    void printSectionEnd(StyledDocument doc, Style style);
} 