package com.terminal.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

public abstract class SystemCommandBase extends AbstractCommand {
    
    public SystemCommandBase(StyledDocument doc, Style style) {
        super(doc, style);
    }

    protected String executeSystemCommand(String command) throws Exception {
        StringBuilder output = new StringBuilder();
        Process process = Runtime.getRuntime().exec(command);
        
        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream(), 
                System.getProperty("os.name").toLowerCase().contains("windows") ? "CP866" : "UTF-8"))) {
            
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        return output.toString();
    }

    protected boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    protected void appendToDoc(String text) throws Exception {
        doc.insertString(doc.getLength(), text, style);
    }
} 