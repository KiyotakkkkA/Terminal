package com.terminal.sdk.services;

import com.terminal.TerminalPanel;

public class TerminalService {
    private static TerminalService instance;
    private TerminalPanel terminalPanel;
    
    private TerminalService() {}
    
    public static TerminalService getInstance() {
        if (instance == null) {
            instance = new TerminalService();
        }
        return instance;
    }
    
    public void setTerminalPanel(TerminalPanel panel) {
        this.terminalPanel = panel;
    }
    
    public TerminalPanel getTerminalPanel() {
        return terminalPanel;
    }
}