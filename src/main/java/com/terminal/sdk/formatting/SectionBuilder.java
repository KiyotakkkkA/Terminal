package com.terminal.sdk.formatting;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import static com.terminal.sdk.formatting.BoxCharacters.BOTTOM_LEFT;
import static com.terminal.sdk.formatting.BoxCharacters.BOTTOM_RIGHT;
import static com.terminal.sdk.formatting.BoxCharacters.HORIZONTAL;
import static com.terminal.sdk.formatting.BoxCharacters.TOP_LEFT;
import static com.terminal.sdk.formatting.BoxCharacters.TOP_RIGHT;
import static com.terminal.sdk.formatting.BoxCharacters.T_RIGHT;
import static com.terminal.sdk.formatting.BoxCharacters.VERTICAL;

public class SectionBuilder {
    private final String title;
    private final StringBuilder buffer;
    private final boolean isFirstSection;
    
    public SectionBuilder(String title, boolean isFirstSection) {
        this.title = title;
        this.isFirstSection = isFirstSection;
        this.buffer = new StringBuilder();
    }
    
    public SectionBuilder appendTitle() {
        buffer.append('\n');
        if (isFirstSection) {
            buffer.append(TOP_LEFT).append(repeat(HORIZONTAL, title.length() + 2)).append(TOP_RIGHT).append('\n');
            buffer.append(VERTICAL).append(" ").append(title).append(" ").append(VERTICAL).append('\n');
            buffer.append(BOTTOM_LEFT).append(repeat(HORIZONTAL, title.length() + 2)).append(BOTTOM_RIGHT).append('\n');
        } else {
            buffer.append(T_RIGHT).append("── ").append(title).append(" ");
            buffer.append(repeat(HORIZONTAL, 80 - title.length() - 5)).append('\n');
        }
        return this;
    }
    
    public void insertIntoDocument(StyledDocument doc, Style style) throws BadLocationException {
        doc.insertString(doc.getLength(), buffer.toString(), style);
    }
    
    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    public static SectionBuilder create(String title, boolean isFirstSection) {
        return new SectionBuilder(title, isFirstSection).appendTitle();
    }
} 