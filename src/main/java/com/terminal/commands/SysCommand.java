package com.terminal.commands;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.text.DecimalFormat;
import java.time.Duration;
import java.util.Properties;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.utils.OutputFormatter;

public class SysCommand extends SystemCommandBase {
    private final DecimalFormat df = new DecimalFormat("#,##0.00");
    private final Style promptStyle;
    public SysCommand(StyledDocument doc, Style style, Style promptStyle) {
        super(doc, style);
        this.promptStyle = promptStyle;
    }

    @Override
    @SuppressWarnings("empty-statement")
    public void executeCommand(String... args) {
        try {
            OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
            Properties props = System.getProperties();
            
            // OS Info
            OutputFormatter.printBeautifulSection(doc, promptStyle, "Операционная система");
            String[] headers = {"Параметр", "Значение"};

            String[][] data = new String[][] {
                {"Имя", os.getName()},
                {"Версия", os.getVersion()},
                {"Архитектура", os.getArch()},
                {"Процессоры", String.valueOf(os.getAvailableProcessors())},
                {"Загрузка CPU", df.format(os.getSystemLoadAverage()) + "%"}
            };
            OutputFormatter.printBeautifulTable(doc, style, headers, data);
            
            // Memory Info
            OutputFormatter.printBeautifulSection(doc, promptStyle, "Оперативная память");
            Runtime rt = Runtime.getRuntime();
            data = new String[][] {
                {"Всего RAM", formatSize(rt.totalMemory())},
                {"Свободно RAM", formatSize(rt.freeMemory())},
                {"Макс. RAM", formatSize(rt.maxMemory())},
                {"Heap использовано", formatSize(memory.getHeapMemoryUsage().getUsed())},
                {"Non-Heap использовано", formatSize(memory.getNonHeapMemoryUsage().getUsed())},
                {"Heap свободно", formatSize(memory.getHeapMemoryUsage().getCommitted())},
                {"Non-Heap свободно", formatSize(memory.getNonHeapMemoryUsage().getCommitted())}
            };
            OutputFormatter.printBeautifulTable(doc, style, headers, data);
            
            // Disk Info
            OutputFormatter.printBeautifulSection(doc, promptStyle, "Диски");
            data = new String[File.listRoots().length][2];
            for (int i = 0; i < File.listRoots().length; i++) {
                File root = File.listRoots()[i];
                data[i][0] = root.getPath();
                data[i][1] = String.format("Всего: %s, Свободно: %s, Доступно: %s",
                    formatSize(root.getTotalSpace()),
                    formatSize(root.getFreeSpace()),
                    formatSize(root.getUsableSpace()));
            }
            OutputFormatter.printBeautifulTable(doc, style, headers, data);
            
            // Java Info
            OutputFormatter.printBeautifulSection(doc, promptStyle, "Java");
            data = new String[][] {
                {"Версия", props.getProperty("java.version")},
                {"Vendor", props.getProperty("java.vendor")},
                {"Home", props.getProperty("java.home")},
                {"Время работы", formatDuration(Duration.ofMillis(runtime.getUptime()))},
                {"Параметры запуска", String.join(" ", runtime.getInputArguments()) != null ? String.join(" ", runtime.getInputArguments()) : "Н/Д"}
            };
            OutputFormatter.printBeautifulTable(doc, style, headers, data);
            
            // Network Info
            OutputFormatter.printBeautifulSection(doc, promptStyle, "Сеть");
            data = new String[][] {
                {"Имя хоста", (System.getenv("COMPUTERNAME") != null ? 
                    System.getenv("COMPUTERNAME") : "Н/Д")},
                {"Домен", (System.getenv("USERDOMAIN") != null ? 
                    System.getenv("USERDOMAIN") : "Н/Д")},
                {"Сетевой профиль", (System.getenv("USERDNSDOMAIN") != null ? 
                    System.getenv("USERDNSDOMAIN") : "Н/Д")},
                {"Временная зона", props.getProperty("user.timezone")}
            };
            OutputFormatter.printBeautifulTable(doc, style, headers, data);

            // User Info
            OutputFormatter.printBeautifulSection(doc, promptStyle, "Пользователь");
            data = new String[][] {
                {"Имя", props.getProperty("user.name")},
                {"Домашняя директория", props.getProperty("user.home")},
                {"Рабочая директория", props.getProperty("user.dir")},
                {"Имя компьютера", System.getenv("COMPUTERNAME")},
                {"Временная директория", props.getProperty("java.io.tmpdir")}
            };
            OutputFormatter.printBeautifulTable(doc, style, headers, data);            
        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return df.format(bytes / Math.pow(1024, exp)) + " " + pre + "B";
    }

    private String formatDuration(Duration duration) {
        long days = duration.toDays();
        duration = duration.minusDays(days);
        long hours = duration.toHours();
        duration = duration.minusHours(hours);
        long minutes = duration.toMinutes();
        duration = duration.minusMinutes(minutes);
        long seconds = duration.getSeconds();
        
        return String.format("%d дн. %d ч. %d мин. %d сек.",
            days, hours, minutes, seconds);
    }

    @Override
    public String getDescription() {
        return "системная информация";
    }
} 