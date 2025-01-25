package com.terminal.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.utils.OutputFormatter;

public class WifiScanCommand extends AbstractCommand {

    public WifiScanCommand(StyledDocument doc, Style style) {
        super(doc, style);
    }

    @Override
    public void execute(String... args) {
        try {
            OutputFormatter.printBoxedHeader(doc, style, "Сканирование Wi-Fi сетей");
            OutputFormatter.printBoxedLine(doc, style, "Доступные сети:");
            
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
                OutputFormatter.printBoxedLine(doc, style, line);
            }

            OutputFormatter.printBoxedFooter(doc, style);
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