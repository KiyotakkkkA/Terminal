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
import com.terminal.sdk.formatting.TableBuilder.Alignment;
import com.terminal.sdk.formatting.TableBuilder.TableFormat;
import com.terminal.sdk.formatting.TableBuilder.TableStyle;

public class DefaultBeautifulFormatter {
    
    protected String currentSectionTitle;
    protected final TableBuilder.TableStyle defaultTableStyle;
    protected final TableBuilder.TableStyle compactTableStyle;
    
    protected static final int DEFAULT_WIDTH = 80;

    public static StringBuilder getOutputBuffer() {
        return TableBuilder.getOutputBuffer();
    }

    public static Boolean isCapturingOutput() {
        return TableBuilder.isCapturingOutput();
    }
    
    public DefaultBeautifulFormatter() {
        this.defaultTableStyle = new TableBuilder.TableStyle.Builder()
            .topLeft("┌")
            .topRight("┐")
            .bottomLeft("└")
            .bottomRight("┘")
            .horizontal("─")
            .vertical("│")
            .tDown("┬")
            .tUp("┴")
            .tLeft("┤")
            .tRight("├")
            .cross("┼")
            .showHeader(true)
            .showBorder(true)
            .padding(1)
            .headerSeparator("─")
            .build();
            
        this.compactTableStyle = new TableBuilder.TableStyle.Builder()
            .topLeft("╭")
            .topRight("╮")
            .bottomLeft("╰")
            .bottomRight("╯")
            .horizontal("─")
            .vertical("│")
            .tDown("┬")
            .tUp("┴")
            .tLeft("┤")
            .tRight("├")
            .cross("┼")
            .showHeader(true)
            .showBorder(true)
            .padding(1)
            .headerSeparator("─")
            .build();
    }
    
    public static void startOutputCapture() {
        TableBuilder.startOutputCapture();
    }

    public static void stopOutputCapture() {
        TableBuilder.stopOutputCapture();
    }

    public static String getCapturedOutput() {
        return TableBuilder.getOutputBuffer().toString();
    }
    
    protected void appendText(StyledDocument doc, Style style, String text) {
        if (TableBuilder.isCapturingOutput()) {
            TableBuilder.getOutputBuffer().append(text);
        }
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    protected void insertText(StyledDocument doc, Style style, String text) {
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    protected int calculateWidth(StyledDocument doc, String[][] data) {
        int maxWidth = 0;
        for (String[] row : data) {
            int rowLength = 0;
            for (String cell : row) {
                rowLength += cell.length();
            }
            rowLength += (row.length - 1) * 3 + 4;
            maxWidth = Math.max(maxWidth, rowLength);
        }
        return maxWidth;
    }
    
    protected void handleError(Exception e) {
        e.printStackTrace();
    }
    
    public void printSection(StyledDocument doc, Style style, String title) {
        try {
            currentSectionTitle = title;
            String[][] data = new String[][] {
                {title}
            };
            
            TableStyle tableStyle = new TableStyle.Builder()
                .showHeader(false)
                .topLeft(TOP_LEFT)
                .topRight(TOP_RIGHT)
                .bottomLeft(BOTTOM_LEFT)
                .bottomRight(BOTTOM_RIGHT)
                .build();
                
            TableFormat format = new TableFormat.Builder()
                .columnAlignments(Alignment.CENTER)
                .build();
                
            TableBuilder.create(new String[] {""}, data, tableStyle, format)
                .insertIntoDocument(doc, style);
        } catch (BadLocationException e) {
            handleError(e);
        }
    }
    
    public void printSectionContent(StyledDocument doc, Style style, String[] headers, String[][] data) {
        try {
            int titleWidth = currentSectionTitle != null ? currentSectionTitle.length() : 0;
            int contentWidth = calculateWidth(doc, data);
            int width = Math.max(titleWidth + 4, contentWidth);
            
            TableStyle tableStyle = new TableStyle.Builder()
                .topLeft(T_RIGHT)
                .topRight(T_LEFT)
                .bottomLeft(BOTTOM_LEFT)
                .bottomRight(BOTTOM_RIGHT)
                .build();
                
            TableBuilder builder = TableBuilder.create(headers, data, tableStyle, new TableFormat.Builder().build());
            builder.setWidth(width);
            builder.insertIntoDocument(doc, style);
        } catch (BadLocationException e) {
            handleError(e);
        }
    }
    
    public void printTable(StyledDocument doc, Style style, String[] headers, String[][] data) {
        TableBuilder.TableFormat tableFormat = new TableBuilder.TableFormat.Builder()
            .columnAlignments(TableBuilder.Alignment.LEFT, TableBuilder.Alignment.LEFT)
            .columnSeparators(true)
            .autoWidth(true)
            .wrapText(false)
            .build();
            
        printTable(doc, style, headers, data, defaultTableStyle, tableFormat);
    }
    
    public void printCompactTable(StyledDocument doc, Style style, String[] headers, String[][] data) {
        TableBuilder.TableFormat tableFormat = new TableBuilder.TableFormat.Builder()
            .columnAlignments(TableBuilder.Alignment.RIGHT, TableBuilder.Alignment.LEFT)
            .columnSeparators(true)
            .autoWidth(true)
            .wrapText(false)
            .build();
            
        printTable(doc, style, headers, data, compactTableStyle, tableFormat);
    }
    
    public void printMinimalTable(StyledDocument doc, Style style, String[] headers, String[][] data) {
        TableBuilder.TableStyle tableStyle = new TableBuilder.TableStyle.Builder()
            .horizontal(" ")
            .vertical(VERTICAL)
            .showHeader(false)
            .showBorder(true)
            .padding(1)
            .build();
            
        TableBuilder.TableFormat tableFormat = new TableBuilder.TableFormat.Builder()
            .columnAlignments(TableBuilder.Alignment.LEFT, TableBuilder.Alignment.LEFT)
            .columnSeparators(true)
            .autoWidth(true)
            .wrapText(false)
            .build();
            
        printTable(doc, style, headers, data, tableStyle, tableFormat);
    }
    
    private void printTable(StyledDocument doc, Style style, String[] headers, String[][] data, 
            TableBuilder.TableStyle tableStyle, TableBuilder.TableFormat tableFormat) {
        try {
            int width = calculateWidth(doc, data);
            TableBuilder builder = TableBuilder.create(headers, data, tableStyle, tableFormat);
            builder.setWidth(width);
            builder.insertIntoDocument(doc, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    public void printMessage(StyledDocument doc, Style style, String message) {
        StringBuilder sb = new StringBuilder()
            .append(TOP_LEFT).append(repeat(HORIZONTAL, message.length() + 2)).append(TOP_RIGHT).append('\n')
            .append(VERTICAL).append(" ").append(message).append(" ").append(VERTICAL).append('\n')
                .append(BOTTOM_LEFT).append(repeat(HORIZONTAL, message.length() + 2)).append(BOTTOM_RIGHT).append('\n');
            
            insertText(doc, style, sb.toString());
    }
    
    public void printSectionEnd(StyledDocument doc, Style style) {
        try {
            String[][] dummyData = {{""}};
            int width = calculateWidth(doc, dummyData);
            appendText(doc, style, BOTTOM_LEFT);
            appendText(doc, style, repeat(HORIZONTAL, width - 2));
            appendText(doc, style, BOTTOM_RIGHT + "\n");
        } catch (Exception e) {
            handleError(e);
        }
    }
    
    private String repeat(String str, int count) {
        if (count <= 0) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }
    
    public void printSectionWithContent(StyledDocument doc, Style style, String title, String[] headers, String[][] data) {
        try {
            String[][] fullData = new String[data.length + 2][2];
            
            fullData[0] = new String[] {title, ""};
            
            fullData[1] = headers;
            
            for (int i = 0; i < data.length; i++) {
                fullData[i + 2] = data[i];
            }
            
            TableStyle tableStyle = new TableStyle.Builder()
                .topLeft(TOP_LEFT)
                .topRight(TOP_RIGHT)
                .bottomLeft(BOTTOM_LEFT)
                .bottomRight(BOTTOM_RIGHT)
                .horizontal(HORIZONTAL)
                .vertical(VERTICAL)
                .tDown(T_DOWN)
                .tUp(T_UP)
                .tLeft(T_LEFT)
                .tRight(T_RIGHT)
                .cross(CROSS)
                .showHeader(false)
                .showBorder(true)
                .padding(1)
                .build();
                
            boolean[] rowSeparators = new boolean[fullData.length];
            rowSeparators[0] = true;
            rowSeparators[1] = true;
            
            TableFormat format = new TableFormat.Builder()
                .columnAlignments(Alignment.LEFT, Alignment.LEFT)
                .columnSeparators(true)
                .rowSeparators(rowSeparators)
                .mergeColumns(true)
                .autoWidth(true)
                .build();
                
            TableBuilder.create(new String[]{"", ""}, fullData, tableStyle, format)
                .insertIntoDocument(doc, style);
        } catch (BadLocationException e) {
            handleError(e);
        }
    }
} 