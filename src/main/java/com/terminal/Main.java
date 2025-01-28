package com.terminal;

import javax.swing.SwingUtilities;

import com.terminal.sdk.events.EventManager;
import com.terminal.sdk.services.IEventManager;
import com.terminal.sdk.services.IPluginManager;
import com.terminal.sdk.services.IThemeManager;
import com.terminal.sdk.services.ServiceLocator;
import com.terminal.utils.PluginManager;
import com.terminal.utils.ThemeManager;

public class Main {

    public static void main(String[] args) {
        initializeServices();
        
        SwingUtilities.invokeLater(() -> {
            TerminalFrame frame = new TerminalFrame(getVersion());
            frame.setVisible(true);
        });
    }
    
    private static void initializeServices() {
        ServiceLocator locator = ServiceLocator.getInstance();
        
        locator.register(IEventManager.class, EventManager.getInstance());
        locator.register(IThemeManager.class, ThemeManager.getInstance());
        locator.register(IPluginManager.class, PluginManager.getInstance());
    }
    
    private static String getVersion() {
        try (java.util.Scanner scanner = new java.util.Scanner(
                Main.class.getResourceAsStream("/version.txt"), "UTF-8")) {
            return scanner.useDelimiter("\\A").next().trim();
        } catch (Exception e) {
            return "unknown";
        }
    }
} 