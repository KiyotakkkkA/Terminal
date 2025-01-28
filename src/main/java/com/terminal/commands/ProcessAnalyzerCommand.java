package com.terminal.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.utils.OutputFormatter;

public class ProcessAnalyzerCommand extends AbstractCommand {
    private final Style promptStyle;

    public ProcessAnalyzerCommand(StyledDocument doc, Style style, Style promptStyle) {
        super(doc, style, null, "procanalyze", "Анализ процессов", "SYSTEM");
        this.promptStyle = promptStyle;
    }

    @Override
    public void executeCommand(String... args) {
        try {
            OutputFormatter.printBoxedHeader(doc, promptStyle, "Анализ процессов");
            OutputFormatter.printBoxedLine(doc, promptStyle, "Системная информация:");
            OutputFormatter.printBoxedLine(doc, style, "");

            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long maxMemory = runtime.maxMemory();

            OutputFormatter.printBoxedLine(doc, style, String.format(
                "Память: использовано %dMB из %dMB (макс: %dMB)",
                (totalMemory - freeMemory) / 1024 / 1024,
                totalMemory / 1024 / 1024,
                maxMemory / 1024 / 1024));

            OutputFormatter.printBoxedLine(doc, style, "");
            OutputFormatter.printBoxedLine(doc, style, "Активные процессы:");
            OutputFormatter.printBoxedLine(doc, style, "PID    CPU%   МБ      Имя процесса");
            OutputFormatter.printBoxedLine(doc, style, "");

            String command = System.getProperty("os.name").toLowerCase().contains("windows")
                ? "tasklist /FO CSV /NH"
                : "ps -eo pid,pcpu,pmem,comm --sort=-pcpu --no-headers";

            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), 
                    System.getProperty("os.name").toLowerCase().contains("windows") ? "CP866" : "UTF-8")
            );

            String line;
            while ((line = reader.readLine()) != null) {
                if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        String processName = parts[0].replaceAll("\"", "");
                        String pid = parts[1].replaceAll("\"", "");
                        String mem = parts[4].replaceAll("\"", "").replaceAll("[^0-9]", "");
                        long memKb = Long.parseLong(mem);
                        double memMb = memKb / 1024.0;
                        
                        OutputFormatter.printBoxedLine(doc, style, String.format(
                            "%-6s %-6s %-7.1f %-37s",
                            pid, "N/A", memMb, 
                            processName.length() > 37 ? processName.substring(0, 34) + "..." : processName
                        ));
                    }
                } else {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 4) {
                        String pid = parts[0];
                        String cpu = parts[1];
                        String mem = parts[2];
                        String name = parts[3];
                        
                        OutputFormatter.printBoxedLine(doc, style, String.format(
                            "%-6s %-6s %-7s %-37s",
                            pid, cpu, mem,
                            name.length() > 37 ? name.substring(0, 34) + "..." : name
                        ));
                    }
                }
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

    @Override
    public String getDescription() {
        return "анализ процессов системы";
    }

    @Override
    public String executeAndGetOutput(String... args) {
        StringBuilder output = new StringBuilder();
        try {
            if (args.length > 0) {
                output.append(getProcessInfo(args[0]));
            } else {
                output.append(getAllProcessesInfo());
            }
        } catch (Exception e) {
            output.append("Ошибка: ").append(e.getMessage()).append("\n");
        }
        return output.toString();
    }

    private String getAllProcessesInfo() throws Exception {
        String command = System.getProperty("os.name").toLowerCase().contains("windows")
            ? "tasklist /V /FO LIST"
            : "ps aux --forest";

        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream(), 
                System.getProperty("os.name").toLowerCase().contains("windows") ? "CP866" : "UTF-8")
        );

        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        return output.toString();
    }

    private String getProcessInfo(String pid) throws Exception {
        String command = System.getProperty("os.name").toLowerCase().contains("windows")
            ? String.format("wmic process where ProcessId=%s get CommandLine,CreationDate,KernelModeTime,UserModeTime,ThreadCount,Priority,WorkingSetSize /format:list", pid)
            : String.format("ps -p %s -o pid,ppid,cmd,etime,time,nice,pcpu,pmem,rss,vsz --forest", pid);

        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(process.getInputStream(), 
                System.getProperty("os.name").toLowerCase().contains("windows") ? "CP866" : "UTF-8")
        );

        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        return output.toString();
    }
} 