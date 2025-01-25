package com.terminal.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.utils.OutputFormatter;

public class NetstatCommand extends AbstractCommand {

    public NetstatCommand(StyledDocument doc, Style style) {
        super(doc, style);
    }

    @Override
    public void execute(String... args) {
        try {
            OutputFormatter.printBoxedHeader(doc, style, "Активные сетевые соединения");
            OutputFormatter.printBoxedLine(doc, style, "Протокол  Локальный адрес      Внешний адрес     Состояние");
            OutputFormatter.printBoxedLine(doc, style, "");

            List<NetworkConnection> connections = getNetworkConnections();
            for (NetworkConnection conn : connections) {
                OutputFormatter.printBoxedLine(doc, style, String.format(
                    "%-9s %-20s %-17s %-9s",
                    conn.protocol,
                    conn.localAddress,
                    conn.remoteAddress,
                    conn.state));
            }

            OutputFormatter.printBoxedLine(doc, style, "");
            OutputFormatter.printBoxedLine(doc, style, "Сетевые интерфейсы:");
            OutputFormatter.printBoxedLine(doc, style, "");

            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                OutputFormatter.printBoxedLine(doc, style, String.format(
                    "%-20s %-30s",
                    ni.getName(),
                    ni.getDisplayName()));
            }

            OutputFormatter.printBoxedFooter(doc, style);

        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private String getServiceName(int port) {
        switch (port) {
            case 80: return "HTTP";
            case 443: return "HTTPS";
            case 21: return "FTP";
            case 22: return "SSH";
            case 23: return "Telnet";
            case 25: return "SMTP";
            case 53: return "DNS";
            case 3306: return "MySQL";
            case 3389: return "RDP";
            case 5432: return "PostgreSQL";
            default: return "Unknown";
        }
    }

    @Override
    public String getDescription() {
        return "информация о сетевых соединениях и открытых портах";
    }

    @Override
    public String executeAndGetOutput(String... args) {
        StringBuilder output = new StringBuilder();
        try {
            output.append("Сканирование сетевых соединений...\n\n");

            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                Process process = Runtime.getRuntime().exec("netstat -ano");
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "CP866"));
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            } else {
                Process process = Runtime.getRuntime().exec("netstat -tulpn");
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            output.append("Ошибка: ").append(e.getMessage()).append("\n");
        }
        return output.toString();
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