package com.terminal.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.utils.OutputFormatter;

public class ProcAnalyzeCommand extends AbstractCommand {
    
    public ProcAnalyzeCommand(StyledDocument doc, Style style) {
        super(doc, style, null, "procanalyze", "Анализ процесса", "SYSTEM");
    }

    @Override
    public void executeCommand(String... args) {
        try {
            if (args.length < 1) {
                showUsage();
                return;
            }

            String pid = args[0];
            OutputFormatter.printBeautifulSection(doc, style, "АНАЛИЗ ПРОЦЕССА");
            OutputFormatter.printBeautifulSection(doc, style, "PID: " + pid);

            String[] basicHeaders = {"Параметр", "Значение"};
            List<String[]> basicInfo = new ArrayList<>();
            
            Process process = Runtime.getRuntime().exec(
                System.getProperty("os.name").toLowerCase().contains("windows")
                    ? "wmic process where ProcessId=" + pid + " get CommandLine,CreationDate,Priority,ThreadCount,WorkingSetSize /format:csv"
                    : "ps -p " + pid + " -o pid,ppid,cmd,%cpu,%mem,etime,state,nlwp"
            );

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean isFirst = true;
            
            while ((line = reader.readLine()) != null) {
                if (isFirst) {
                    isFirst = false;
                    continue;
                }
                if (!line.trim().isEmpty()) {
                    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                        String[] parts = line.split(",");
                        if (parts.length >= 5) {
                            basicInfo.add(new String[]{"Командная строка", parts[1]});
                            basicInfo.add(new String[]{"Время создания", formatWindowsDate(parts[2])});
                            basicInfo.add(new String[]{"Приоритет", parts[3]});
                            basicInfo.add(new String[]{"Количество потоков", parts[4]});
                            basicInfo.add(new String[]{"Использование памяти", formatBytes(Long.parseLong(parts[5].trim()))});
                        }
                    } else {
                        String[] parts = line.trim().split("\\s+");
                        if (parts.length >= 8) {
                            basicInfo.add(new String[]{"PID", parts[0]});
                            basicInfo.add(new String[]{"PPID", parts[1]});
                            basicInfo.add(new String[]{"Команда", parts[2]});
                            basicInfo.add(new String[]{"CPU %", parts[3]});
                            basicInfo.add(new String[]{"Память %", parts[4]});
                            basicInfo.add(new String[]{"Время работы", parts[5]});
                            basicInfo.add(new String[]{"Статус", parts[6]});
                            basicInfo.add(new String[]{"Потоки", parts[7]});
                        }
                    }
                    break;
                }
            }

            String[][] basicData = basicInfo.toArray(new String[0][]);
            OutputFormatter.printBeautifulTable(doc, style, basicHeaders, basicData);

            OutputFormatter.printBeautifulSection(doc, style, "ОТКРЫТЫЕ ФАЙЛЫ");
            String[] fileHeaders = {"Путь", "Тип", "Доступ"};
            List<String[]> fileInfo = new ArrayList<>();
            
            Process fileProcess = Runtime.getRuntime().exec(
                System.getProperty("os.name").toLowerCase().contains("windows")
                    ? "handle -p " + pid
                    : "lsof -p " + pid
            );

            reader = new BufferedReader(new InputStreamReader(fileProcess.getInputStream()));
            int fileCount = 0;
            
            while ((line = reader.readLine()) != null && fileCount < 10) {
                if (!line.trim().isEmpty() && !line.startsWith("Handle")) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length >= 3) {
                        fileInfo.add(new String[]{
                            parts[parts.length - 1],
                            parts[1],
                            parts[2]
                        });
                        fileCount++;
                    }
                }
            }

            if (fileInfo.isEmpty()) {
                OutputFormatter.printBeautifulSection(doc, style, "Нет доступных данных");
            } else {
                String[][] fileData = fileInfo.toArray(new String[0][]);
                OutputFormatter.printBeautifulTable(doc, style, fileHeaders, fileData);
            }

        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при анализе процесса: " + e.getMessage());
            }
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }

    private String formatWindowsDate(String wmiDate) {
        try {
            String year = wmiDate.substring(0, 4);
            String month = wmiDate.substring(4, 6);
            String day = wmiDate.substring(6, 8);
            String hour = wmiDate.substring(8, 10);
            String minute = wmiDate.substring(10, 12);
            String second = wmiDate.substring(12, 14);
            return String.format("%s-%s-%s %s:%s:%s", year, month, day, hour, minute, second);
        } catch (Exception e) {
            return wmiDate;
        }
    }

    private void showUsage() throws Exception {
        OutputFormatter.printBeautifulSection(doc, style, "ИСПОЛЬЗОВАНИЕ");
        OutputFormatter.printBeautifulSection(doc, style, "procanalyze <pid>");
        
        String[] headers = {"Параметр", "Описание"};
        String[][] data = {
            {"pid", "идентификатор процесса для анализа"}
        };
        
        OutputFormatter.printBeautifulTable(doc, style, headers, data);
    }
    
    @Override
    public String getDescription() {
        return "анализ процесса";
    }
} 