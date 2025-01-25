package com.terminal;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

public class TerminalFrame extends JFrame {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final String DEFAULT_TITLE = "Терминал";
    
    private final JSplitPane mainSplitPane;
    private final List<TerminalPanel> terminalPanels;
    private TerminalPanel activePanel;

    public TerminalFrame() {
        setTitle(DEFAULT_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        
        terminalPanels = new ArrayList<>();
        
        TerminalPanel firstPanel = new TerminalPanel(this);
        activePanel = firstPanel;
        terminalPanels.add(firstPanel);
        
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setLeftComponent(firstPanel);
        mainSplitPane.setDividerSize(5);
        mainSplitPane.setBorder(null);
        
        add(mainSplitPane);
        setLocationRelativeTo(null);
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                firstPanel.requestFocusInWindow();
            }
        });
    }

    public void setEditMode(boolean isEdit) {
        if (isEdit) {
            setTitle(DEFAULT_TITLE + " - Режим редактирования (Ctrl+D для сохранения и выхода)");
        } else {
            setTitle(DEFAULT_TITLE);
        }
    }
    
    public void splitVertically() {
        TerminalPanel newPanel = new TerminalPanel(this);
        terminalPanels.add(newPanel);
        
        if (mainSplitPane.getRightComponent() == null) {
            mainSplitPane.setRightComponent(newPanel);
        } else {
            JSplitPane newSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            newSplit.setLeftComponent(mainSplitPane.getRightComponent());
            newSplit.setRightComponent(newPanel);
            newSplit.setDividerSize(5);
            newSplit.setBorder(null);
            mainSplitPane.setRightComponent(newSplit);
        }
        
        mainSplitPane.setDividerLocation(0.5);
        revalidate();
    }
    
    public void splitHorizontally() {
        TerminalPanel newPanel = new TerminalPanel(this);
        terminalPanels.add(newPanel);
        
        if (mainSplitPane.getRightComponent() == null) {
            JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            verticalSplit.setTopComponent(mainSplitPane.getLeftComponent());
            verticalSplit.setBottomComponent(newPanel);
            verticalSplit.setDividerSize(5);
            verticalSplit.setBorder(null);
            mainSplitPane.setLeftComponent(verticalSplit);
        } else {
            JSplitPane verticalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            verticalSplit.setTopComponent(mainSplitPane.getRightComponent());
            verticalSplit.setBottomComponent(newPanel);
            verticalSplit.setDividerSize(5);
            verticalSplit.setBorder(null);
            mainSplitPane.setRightComponent(verticalSplit);
        }
        
        mainSplitPane.setDividerLocation(0.5);
        revalidate();
    }
    
    public void setActivePanel(TerminalPanel panel) {
        if (activePanel != null && activePanel != panel) {
            activePanel.setBorder(null);
        }
        this.activePanel = panel;
        if (panel != null) {
            panel.requestFocusInWindow();
        }
    }
    
    public TerminalPanel getActivePanel() {
        if (activePanel == null && !terminalPanels.isEmpty()) {
            activePanel = terminalPanels.get(0);
        }
        return activePanel;
    }
    
    public void closePanel(TerminalPanel panel) {
        if (terminalPanels.size() <= 1) {
            return;
        }
        
        terminalPanels.remove(panel);
        
        if (panel == activePanel) {
            activePanel = terminalPanels.get(0);
            activePanel.requestFocusInWindow();
        }
        
        rebuildUI();
        revalidate();
        repaint();
    }
    
    private void rebuildUI() {
        mainSplitPane.removeAll();
        
        if (terminalPanels.isEmpty()) {
            return;
        }
        
        mainSplitPane.setLeftComponent(terminalPanels.get(0));
        
        if (terminalPanels.size() > 1) {
            JSplitPane currentSplit = mainSplitPane;
            for (int i = 1; i < terminalPanels.size(); i++) {
                JSplitPane newSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                newSplit.setLeftComponent(terminalPanels.get(i));
                newSplit.setDividerSize(5);
                newSplit.setBorder(null);
                currentSplit.setRightComponent(newSplit);
                currentSplit = newSplit;
            }
        }
        
        mainSplitPane.setDividerLocation(0.5);
    }
} 