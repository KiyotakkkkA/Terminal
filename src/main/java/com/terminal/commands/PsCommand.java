package com.terminal.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.utils.OutputFormatter;

public class PsCommand extends SystemCommandBase {
    private final Style promptStyle;

    public PsCommand(StyledDocument doc, Style style, Style promptStyle) {
        super(doc, style);
        this.promptStyle = promptStyle;
    }

    @Override
    public void executeCommand(String... args) {
        try {
            OutputFormatter.printBeautifulSection(doc, promptStyle, "СПИСОК ПРОЦЕССОВ");
            
            String[] headers = {"PID", "Имя", "Память", "CPU %", "Статус"};
            List<String[]> dataList = new ArrayList<>();
            
            String command = System.getProperty("os.name").toLowerCase().contains("windows") 
                ? "tasklist /FO CSV /NH" 
                : "ps -eo pid,comm,pmem,pcpu,state --sort=-pmem";

            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), 
                    System.getProperty("os.name").toLowerCase().contains("windows") ? "CP866" : "UTF-8")
            );

            String line;
            while ((line = reader.readLine()) != null) {
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    String[] parts = line.split("\",\"");
                    if (parts.length >= 5) {
                        String name = parts[0].replace("\"", "");
                        String pid = parts[1].replace("\"", "");
                        String memUsage = parts[4].replace("\"", "").replace(" K", " KB");
                        
                        dataList.add(new String[]{
                            pid,
                            name,
                            memUsage,
                            "-",
                            parts[2].replace("\"", "")
                        });
                    }
                } else {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 5) {
                        dataList.add(new String[]{
                            parts[0], // PID
                            parts[1], // Command
                            String.format("%.1f%%", Double.parseDouble(parts[2])), // Memory %
                            String.format("%.1f%%", Double.parseDouble(parts[3])), // CPU %
                            parts[4]  // State
                        });
                    }
                }
            }

            String[][] data = dataList.toArray(new String[0][]);
            OutputFormatter.printBeautifulTable(doc, style, headers, data);

        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при получении списка процессов: " + e.getMessage());
            }
        }
    }

    @Override
    public String getDescription() {
        return "список запущенных процессов";
    }

    @Override
    public String executeAndGetOutput(String... args) {
        try {
            String command = isWindows() ? "tasklist" : "ps aux";
            return executeSystemCommand(command);
        } catch (Exception e) {
            return "Ошибка: " + e.getMessage() + "\n";
        }
    }
} 