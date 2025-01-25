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

    public SysCommand(StyledDocument doc, Style style) {
        super(doc, style);
    }

    @Override
    public void execute(String... args) {
        try {
            OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            MemoryMXBean memory = ManagementFactory.getMemoryMXBean();
            Properties props = System.getProperties();

            OutputFormatter.printBoxedHeader(doc, style, "Системная информация");
            
            // OS Info
            OutputFormatter.printBoxedLine(doc, style, "Операционная система:");
            OutputFormatter.printBoxedLine(doc, style, "  Имя: " + os.getName());
            OutputFormatter.printBoxedLine(doc, style, "  Версия: " + os.getVersion());
            OutputFormatter.printBoxedLine(doc, style, "  Архитектура: " + os.getArch());
            OutputFormatter.printBoxedLine(doc, style, "  Процессоры: " + os.getAvailableProcessors());
            OutputFormatter.printBoxedLine(doc, style, "  Загрузка CPU: " + df.format(os.getSystemLoadAverage()) + "%");
            
            // Memory Info
            OutputFormatter.printBoxedLine(doc, style, "");
            OutputFormatter.printBoxedLine(doc, style, "Память:");
            Runtime rt = Runtime.getRuntime();
            OutputFormatter.printBoxedLine(doc, style, "  Всего RAM: " + formatSize(rt.totalMemory()));
            OutputFormatter.printBoxedLine(doc, style, "  Свободно RAM: " + formatSize(rt.freeMemory()));
            OutputFormatter.printBoxedLine(doc, style, "  Макс. RAM: " + formatSize(rt.maxMemory()));
            OutputFormatter.printBoxedLine(doc, style, "  Heap использовано: " + formatSize(memory.getHeapMemoryUsage().getUsed()));
            OutputFormatter.printBoxedLine(doc, style, "  Non-Heap использовано: " + formatSize(memory.getNonHeapMemoryUsage().getUsed()));
            
            // Disk Info
            OutputFormatter.printBoxedLine(doc, style, "");
            OutputFormatter.printBoxedLine(doc, style, "Диски:");
            for (File root : File.listRoots()) {
                String diskInfo = String.format("  %s Всего: %s, Свободно: %s, Доступно: %s",
                    root.getPath(),
                    formatSize(root.getTotalSpace()),
                    formatSize(root.getFreeSpace()),
                    formatSize(root.getUsableSpace()));
                OutputFormatter.printBoxedLine(doc, style, diskInfo);
            }
            
            // Java Info
            OutputFormatter.printBoxedLine(doc, style, "");
            OutputFormatter.printBoxedLine(doc, style, "Java:");
            OutputFormatter.printBoxedLine(doc, style, "  Версия: " + props.getProperty("java.version"));
            OutputFormatter.printBoxedLine(doc, style, "  Vendor: " + props.getProperty("java.vendor"));
            OutputFormatter.printBoxedLine(doc, style, "  Home: " + props.getProperty("java.home"));
            OutputFormatter.printBoxedLine(doc, style, "  Время работы: " + formatDuration(Duration.ofMillis(runtime.getUptime())));
            OutputFormatter.printBoxedLine(doc, style, "  Параметры запуска: " + runtime.getInputArguments());

            // Network Info
            OutputFormatter.printBoxedLine(doc, style, "");
            OutputFormatter.printBoxedLine(doc, style, "Сеть:");
            OutputFormatter.printBoxedLine(doc, style, "  Имя хоста: " + (System.getenv("COMPUTERNAME") != null ? 
                System.getenv("COMPUTERNAME") : "Н/Д"));
            OutputFormatter.printBoxedLine(doc, style, "  Домен: " + (System.getenv("USERDOMAIN") != null ? 
                System.getenv("USERDOMAIN") : "Н/Д"));
            OutputFormatter.printBoxedLine(doc, style, "  Сетевой профиль: " + (System.getenv("USERDNSDOMAIN") != null ? 
                System.getenv("USERDNSDOMAIN") : "Н/Д"));
            OutputFormatter.printBoxedLine(doc, style, "  Временная зона: " + props.getProperty("user.timezone"));

            // User Info
            OutputFormatter.printBoxedLine(doc, style, "");
            OutputFormatter.printBoxedLine(doc, style, "Пользователь:");
            OutputFormatter.printBoxedLine(doc, style, "  Имя: " + props.getProperty("user.name"));
            OutputFormatter.printBoxedLine(doc, style, "  Домашняя директория: " + props.getProperty("user.home"));
            OutputFormatter.printBoxedLine(doc, style, "  Рабочая директория: " + props.getProperty("user.dir"));
            OutputFormatter.printBoxedLine(doc, style, "  Временная директория: " + props.getProperty("java.io.tmpdir"));
            
            OutputFormatter.printBoxedFooter(doc, style);
            
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