package com.terminal.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.utils.OutputFormatter;

public class NetstatCommand extends AbstractCommand {
    private final Style promptStyle;

    public NetstatCommand(StyledDocument doc, Style style, Style promptStyle) {
        super(doc, style, null, "netstat", "Просмотр сетевых подключений", "NETWORK");
        this.promptStyle = promptStyle;
    }

    @Override
    public void executeCommand(String... args) {
        try {
            OutputFormatter.printBeautifulSection(doc, promptStyle, "СЕТЕВЫЕ СОЕДИНЕНИЯ");
            
            String[] headers = {"Протокол", "Локальный адрес", "Удаленный адрес", "Состояние"};
            List<String[]> dataList = new ArrayList<>();
            
            Process process = Runtime.getRuntime().exec(
                System.getProperty("os.name").toLowerCase().contains("windows") 
                ? "netstat -an" 
                : "netstat -tuln"
            );
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean skipHeader = true;
            
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    if (line.contains("Proto") || line.trim().isEmpty()) {
                        skipHeader = false;
                    }
                    continue;
                }
                
                String[] parts = line.trim().split("\\s+");
                if (parts.length >= 4) {
                    String protocol = parts[0];
                    String localAddr = parts[1];
                    String remoteAddr = parts[2];
                    String state = parts.length > 3 ? parts[3] : "-";
                    
                    dataList.add(new String[]{protocol, localAddr, remoteAddr, state});
                }
            }
            
            String[][] data = dataList.toArray(new String[0][]);
            OutputFormatter.printBeautifulTable(doc, style, headers, data);
            
        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public String getDescription() {
        return "информация о сетевых соединениях и открытых портах";
    }

    private static class NetworkConnection {
        String protocol;
        String localAddress;
        String remoteAddress;
        String state;

        NetworkConnection(String protocol, String localAddress, String remoteAddress, String state) {
            this.protocol = protocol;
            this.localAddress = localAddress;
            this.remoteAddress = remoteAddress;
            this.state = state;
        }
    }

    private List<NetworkConnection> getNetworkConnections() throws Exception {
        List<NetworkConnection> connections = new ArrayList<>();
        
        Process process = Runtime.getRuntime().exec(
            System.getProperty("os.name").toLowerCase().contains("windows") 
            ? "netstat -ano" 
            : "netstat -tulpn"
        );
        
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream(), 
                System.getProperty("os.name").toLowerCase().contains("windows") 
                ? "CP866" 
                : "UTF-8")
        );

        String line;
        boolean skipHeader = true;
        while ((line = reader.readLine()) != null) {
            if (skipHeader) {
                if (line.contains("Proto") || line.trim().isEmpty()) {
                    continue;
                }
                skipHeader = false;
            }
            
            String[] parts = line.trim().split("\\s+");
            if (parts.length >= 4) {
                connections.add(new NetworkConnection(
                    parts[0],
                    parts[1],
                    parts[2],
                    parts[3]
                ));
            }
        }
        
        return connections;
    }
} 