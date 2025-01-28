package com.terminal.sdk.services;

import com.terminal.TerminalPanel;

public interface ITerminalService {
    TerminalPanel getTerminalPanel();
    void setTerminalPanel(TerminalPanel panel);
} 