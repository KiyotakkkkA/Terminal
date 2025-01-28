package com.terminal.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class WifiScanCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;
    private final Style promptStyle;

    public WifiScanCommand(StyledDocument doc, Style style, Style promptStyle, CurrentPathHolder pathHolder) {
        super(doc, style, pathHolder, "wifiscan", "Сканирование WiFi сетей", "NETWORK");
        this.pathHolder = pathHolder;
        this.promptStyle = promptStyle;
    }

    @Override
    public void executeCommand(String... args) {
        try {
            OutputFormatter.printBeautifulSection(doc, promptStyle, "СКАНИРОВАНИЕ WI-FI СЕТЕЙ");
            
            String[] headers = {"SSID", "BSSID", "Сигнал", "Канал", "Частота", "Безопасность", "Скорость"};
            List<String[]> dataList = new ArrayList<>();
            
            String command = System.getProperty("os.name").toLowerCase().contains("windows") 
                ? "netsh wlan show networks mode=Bssid" 
                : "nmcli -f SSID,BSSID,SIGNAL,CHAN,FREQ,SECURITY,RATE dev wifi list";

            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), 
                    System.getProperty("os.name").toLowerCase().contains("windows") ? "CP866" : "UTF-8")
            );

            String line;
            String currentSSID = null;
            String bssid = null;
            String signal = null;
            String channel = null;
            String frequency = null;
            String security = null;
            String rate = null;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    if (line.startsWith("SSID")) {
                        if (currentSSID != null) {
                            dataList.add(new String[]{
                                currentSSID,
                                bssid != null ? bssid : "-",
                                signal != null ? signal : "-",
                                channel != null ? channel : "-",
                                frequency != null ? frequency : "-",
                                security != null ? security : "-",
                                rate != null ? rate : "-"
                            });
                        }
                        currentSSID = line.substring(line.indexOf(":") + 1).trim();
                        bssid = null;
                        signal = null;
                        channel = null;
                        frequency = null;
                        security = null;
                        rate = null;
                    } else if (line.contains("BSSID")) {
                        bssid = line.substring(line.indexOf(":") + 1).trim();
                    } else if (line.contains("Signal")) {
                        signal = line.substring(line.indexOf(":") + 1).trim();
                    } else if (line.contains("Channel")) {
                        channel = line.substring(line.indexOf(":") + 1).trim();
                        try {
                            int ch = Integer.parseInt(channel);
                            frequency = String.format("%.1f GHz", (2.412 + 0.005 * (ch - 1)));
                        } catch (Exception e) {
                            frequency = "-";
                        }
                    } else if (line.contains("Authentication")) {
                        security = line.substring(line.indexOf(":") + 1).trim();
                    } else if (line.contains("Basic rates")) {
                        rate = line.substring(line.indexOf(":") + 1).trim().split("\\s+")[0];
                    }
                } else {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 7 && !line.contains("SSID")) {
                        dataList.add(new String[]{
                            parts[0], // SSID
                            parts[1], // BSSID
                            parts[2] + "%", // Signal
                            parts[3], // Channel
                            parts[4], // Frequency
                            parts[5], // Security
                            parts[6]  // Rate
                        });
                    }
                }
            }
            
            // Add last network for Windows
            if (System.getProperty("os.name").toLowerCase().contains("windows") && currentSSID != null) {
                dataList.add(new String[]{
                    currentSSID,
                    bssid != null ? bssid : "-",
                    signal != null ? signal : "-",
                    channel != null ? channel : "-",
                    frequency != null ? frequency : "-",
                    security != null ? security : "-",
                    rate != null ? rate : "-"
                });
            }

            if (dataList.isEmpty()) {
                OutputFormatter.printBeautifulSection(doc, style, "Сети не найдены");
            } else {
                String[][] data = dataList.toArray(new String[0][]);
                OutputFormatter.printBeautifulTable(doc, style, headers, data);
            }

        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при сканировании Wi-Fi: " + e.getMessage());
            }
        }
    }

    @Override
    public String getDescription() {
        return "сканирование доступных Wi-Fi сетей";
    }

    @Override
    public String executeAndGetOutput(String... args) {
        StringBuilder output = new StringBuilder();
        try {
            String command = System.getProperty("os.name").toLowerCase().contains("windows") 
                ? "netsh wlan show networks mode=Bssid" 
                : "nmcli dev wifi list";

            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), 
                    System.getProperty("os.name").toLowerCase().contains("windows") ? "CP866" : "UTF-8")
            );

            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        } catch (Exception e) {
            output.append("Ошибка: ").append(e.getMessage()).append("\n");
        }
        return output.toString();
    }
} 