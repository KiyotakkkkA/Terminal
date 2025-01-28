package com.terminal.sdk.formatting;

import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import static com.terminal.sdk.formatting.BoxCharacters.BOTTOM_LEFT;
import static com.terminal.sdk.formatting.BoxCharacters.BOTTOM_RIGHT;
import static com.terminal.sdk.formatting.BoxCharacters.CROSS;
import static com.terminal.sdk.formatting.BoxCharacters.HORIZONTAL;
import static com.terminal.sdk.formatting.BoxCharacters.TOP_LEFT;
import static com.terminal.sdk.formatting.BoxCharacters.TOP_RIGHT;
import static com.terminal.sdk.formatting.BoxCharacters.T_DOWN;
import static com.terminal.sdk.formatting.BoxCharacters.T_LEFT;
import static com.terminal.sdk.formatting.BoxCharacters.T_RIGHT;
import static com.terminal.sdk.formatting.BoxCharacters.T_UP;
import static com.terminal.sdk.formatting.BoxCharacters.VERTICAL;

public class TableBuilder {
    private final String[] headers;
    private final String[][] data;
    private final int[] columnWidths;
    private final StringBuilder buffer;
    private final TableStyle style;
    private final TableFormat format;
    private int width;

    protected static StringBuilder outputBuffer = new StringBuilder();
    protected static Boolean isCapturingOutput = false;

    public static StringBuilder getOutputBuffer() {
        return outputBuffer;
    }

    public static Boolean isCapturingOutput() {
        return isCapturingOutput;
    }

    public static void startOutputCapture() {
        outputBuffer.setLength(0);
        isCapturingOutput = true;
    }

    public static void stopOutputCapture() {
        isCapturingOutput = false;
    }

    public static String getCapturedOutput() {
        return outputBuffer.toString();
    }
    
    public static class TableStyle {
        private final String topLeft;
        private final String topRight;
        private final String bottomLeft;
        private final String bottomRight;
        private final String horizontal;
        private final String vertical;
        private final String tDown;
        private final String tUp;
        private final String tLeft;
        private final String tRight;
        private final String cross;
        private final boolean showHeader;
        private final boolean showBorder;
        private final int padding;
        private final String headerSeparator;
        
        private TableStyle(Builder builder) {
            this.topLeft = builder.topLeft;
            this.topRight = builder.topRight;
            this.bottomLeft = builder.bottomLeft;
            this.bottomRight = builder.bottomRight;
            this.horizontal = builder.horizontal;
            this.vertical = builder.vertical;
            this.tDown = builder.tDown;
            this.tUp = builder.tUp;
            this.tLeft = builder.tLeft;
            this.tRight = builder.tRight;
            this.cross = builder.cross;
            this.showHeader = builder.showHeader;
            this.showBorder = builder.showBorder;
            this.padding = builder.padding;
            this.headerSeparator = builder.headerSeparator;
        }
        
        public static class Builder {
            private String topLeft = TOP_LEFT;
            private String topRight = TOP_RIGHT;
            private String bottomLeft = BOTTOM_LEFT;
            private String bottomRight = BOTTOM_RIGHT;
            private String horizontal = HORIZONTAL;
            private String vertical = VERTICAL;
            private String tDown = T_DOWN;
            private String tUp = T_UP;
            private String tLeft = T_LEFT;
            private String tRight = T_RIGHT;
            private String cross = CROSS;
            private boolean showHeader = true;
            private boolean showBorder = true;
            private int padding = 1;
            private String headerSeparator = HORIZONTAL;
            
            public Builder topLeft(String val) { topLeft = val; return this; }
            public Builder topRight(String val) { topRight = val; return this; }
            public Builder bottomLeft(String val) { bottomLeft = val; return this; }
            public Builder bottomRight(String val) { bottomRight = val; return this; }
            public Builder horizontal(String val) { horizontal = val; return this; }    
            public Builder vertical(String val) { vertical = val; return this; }
            public Builder tDown(String val) { tDown = val; return this; }
            public Builder tUp(String val) { tUp = val; return this; }
            public Builder tLeft(String val) { tLeft = val; return this; }
            public Builder tRight(String val) { tRight = val; return this; }
            public Builder cross(String val) { cross = val; return this; }
            public Builder showHeader(boolean val) { showHeader = val; return this; }
            public Builder showBorder(boolean val) { showBorder = val; return this; }
            public Builder padding(int val) { padding = val; return this; }
            public Builder headerSeparator(String val) { headerSeparator = val; return this; }
            
            public TableStyle build() {
                return new TableStyle(this);
            }
        }
    }
    
    public enum Alignment {
        LEFT, CENTER, RIGHT
    }
    
    public static class TableFormat {
        private final Alignment[] columnAlignments;
        private final boolean[] columnSeparators;
        private final boolean[] rowSeparators;
        private final int[] columnMinWidths;
        private final int[] columnMaxWidths;
        private final boolean autoWidth;
        private final boolean mergeColumns;
        private final boolean wrapText;
        
        private TableFormat(Builder builder) {
            this.columnAlignments = builder.columnAlignments;
            this.columnSeparators = builder.columnSeparators;
            this.rowSeparators = builder.rowSeparators;
            this.columnMinWidths = builder.columnMinWidths;
            this.columnMaxWidths = builder.columnMaxWidths;
            this.autoWidth = builder.autoWidth;
            this.mergeColumns = builder.mergeColumns;
            this.wrapText = builder.wrapText;
        }
        
        public static class Builder {
            private Alignment[] columnAlignments;
            private boolean[] columnSeparators;
            private boolean[] rowSeparators;
            private int[] columnMinWidths;
            private int[] columnMaxWidths;
            private boolean autoWidth = true;
            private boolean mergeColumns = false;
            private boolean wrapText = false;
            
            public Builder columnAlignments(Alignment... alignments) {
                this.columnAlignments = alignments;
                return this;
            }
            
            public Builder columnSeparators(boolean... separators) {
                this.columnSeparators = separators;
                return this;
            }
            
            public Builder rowSeparators(boolean... separators) {
                this.rowSeparators = separators;
                return this;
            }
            
            public Builder columnMinWidths(int... widths) {
                this.columnMinWidths = widths;
                return this;
            }
            
            public Builder columnMaxWidths(int... widths) {
                this.columnMaxWidths = widths;
                return this;
            }
            
            public Builder autoWidth(boolean autoWidth) {
                this.autoWidth = autoWidth;
                return this;
            }
            
            public Builder mergeColumns(boolean mergeColumns) {
                this.mergeColumns = mergeColumns;
                return this;
            }
            
            public Builder wrapText(boolean wrapText) {
                this.wrapText = wrapText;
                return this;
            }
            
            public TableFormat build() {
                return new TableFormat(this);
            }
        }
    }
    
    private TableBuilder(String[] headers, String[][] data, TableStyle style, TableFormat format) {
        this.headers = headers != null ? headers : new String[0];
        this.data = data != null ? data : new String[0][0];
        this.style = style;
        this.format = format;
        this.columnWidths = calculateColumnWidths();
        this.buffer = new StringBuilder();
        this.width = 0;
    }
    
    private int[] calculateColumnWidths() {
        int columns = headers.length;
        int[] widths = new int[columns];

        for (int i = 0; i < columns; i++) {
            widths[i] = format.columnMinWidths != null && i < format.columnMinWidths.length 
                ? format.columnMinWidths[i] : 0;
        }

        for (int i = 0; i < columns; i++) {
            if (headers[i] != null) {
                widths[i] = Math.max(widths[i], headers[i].length());
            }
        }

        for (String[] row : data) {
            for (int i = 0; i < columns && i < row.length; i++) {
                if (row[i] != null) {
                    widths[i] = Math.max(widths[i], row[i].length());
                }
            }
        }

        if (format.columnMaxWidths != null) {
            for (int i = 0; i < columns && i < format.columnMaxWidths.length; i++) {
                if (format.columnMaxWidths[i] > 0) {
                    widths[i] = Math.min(widths[i], format.columnMaxWidths[i]);
                }
            }
        }

        return widths;
    }
    
    private String alignText(String text, int width, Alignment alignment) {
        if (text == null) text = "";
        if (text.length() > width) {
            return format.wrapText ? text.substring(0, width) : text;
        }

        int padding = width - text.length();
        switch (alignment) {
            case RIGHT:
                return repeat(" ", padding) + text;
            case CENTER:
                int leftPad = padding / 2;
                int rightPad = padding - leftPad;
                return repeat(" ", leftPad) + text + repeat(" ", rightPad);
            default:
                return text + repeat(" ", padding);
        }
    }
    
    private void appendRow(String[] row, boolean isHeader) {
        if (style.showBorder) buffer.append(style.vertical);

        for (int i = 0; i < headers.length; i++) {
            String value = i < row.length ? row[i] : "";
            Alignment alignment = format.columnAlignments != null && i < format.columnAlignments.length 
                ? format.columnAlignments[i] 
                : Alignment.LEFT;

            buffer.append(repeat(" ", style.padding))
                  .append(alignText(value, columnWidths[i], alignment))
                  .append(repeat(" ", style.padding));

            boolean showSeparator = style.showBorder || i < headers.length - 1;
            if (showSeparator && (!format.mergeColumns || format.columnSeparators == null || 
                i >= format.columnSeparators.length || format.columnSeparators[i])) {
                buffer.append(style.vertical);
            } else {
                buffer.append(" ");
            }
        }
        buffer.append('\n');
    }
    
    public TableBuilder appendTopBorder() {
        if (!style.showBorder) return this;
        
        buffer.append(style.topLeft);
        int totalWidth = 0;
        for (int i = 0; i < columnWidths.length; i++) {
            totalWidth += columnWidths[i] + style.padding * 2;
        }
        if (columnWidths.length > 1) {
            totalWidth += columnWidths.length - 1;
        }
        buffer.append(repeat(style.horizontal, totalWidth));
        buffer.append(style.topRight).append('\n');
        return this;
    }
    
    public TableBuilder appendHeaders() {
        if (!style.showHeader) return this;
        
        if (style.showBorder) buffer.append(style.vertical);
        for (int i = 0; i < headers.length; i++) {
            buffer.append(repeat(" ", style.padding))
                  .append(padRight(headers[i], columnWidths[i]))
                  .append(repeat(" ", style.padding));
            if (style.showBorder || i < headers.length - 1) {
                buffer.append(style.vertical);
            }
        }
        buffer.append('\n');
        return this;
    }
    
    public TableBuilder appendSeparator() {
        if (!style.showHeader) return this;
        
        buffer.append(style.tRight);
        int totalWidth = 0;
        for (int i = 0; i < columnWidths.length; i++) {
            totalWidth += columnWidths[i] + style.padding * 2;
        }
        if (columnWidths.length > 1) {
            totalWidth += columnWidths.length - 1;
        }
        buffer.append(repeat(style.headerSeparator, totalWidth));
        buffer.append(style.tLeft).append('\n');
        return this;
    }
    
    public TableBuilder appendData() {
        for (String[] row : data) {
            appendRow(row, false);
        }
        return this;
    }
    
    public TableBuilder appendBottomBorder() {
        if (!style.showBorder) return this;
        
        buffer.append(style.bottomLeft);
        int totalWidth = 0;
        for (int i = 0; i < columnWidths.length; i++) {
            totalWidth += columnWidths[i] + style.padding * 2;
        }
        if (columnWidths.length > 1) {
            totalWidth += columnWidths.length - 1;
        }
        buffer.append(repeat(style.horizontal, totalWidth));
        buffer.append(style.bottomRight).append('\n');
        return this;
    }
    
    public void insertIntoDocument(StyledDocument doc, Style docStyle) throws BadLocationException {
        if (DefaultBeautifulFormatter.isCapturingOutput()) {
            DefaultBeautifulFormatter.getOutputBuffer().append(buffer.toString());
        } else {
            doc.insertString(doc.getLength(), buffer.toString(), docStyle);
        }
    }
    
    private static String repeat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    private static String padRight(String str, int length) {
        return String.format("%-" + length + "s", str != null ? str : "");
    }
    
    public TableBuilder setWidth(int width) {
        this.width = width;
        return this;
    }
    
    public static TableBuilder create(String[] headers, String[][] data, TableStyle style, TableFormat format) {
        return new TableBuilder(headers, data, style, format)
            .appendTopBorder()
            .appendHeaders()
            .appendSeparator()
            .appendData()
            .appendBottomBorder();
    }
    
    public static TableBuilder create(String[] headers, String[][] data) {
        return create(headers, data, new TableStyle.Builder().build(), new TableFormat.Builder().build());
    }
} 