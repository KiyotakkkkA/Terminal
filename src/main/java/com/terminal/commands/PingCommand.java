package com.terminal.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.utils.OutputFormatter;

public class PingCommand extends AbstractCommand {

    public PingCommand(StyledDocument doc, Style style) {
        super(doc, style);
    }

    @Override
    public void execute(String... args) {
        try {
            if (args.length < 1) {
                showUsage();
                return;
            }

            String host = args[0];
            int count = 4;
            int timeout = 1000;

            for (int i = 1; i < args.length; i++) {
                if (args[i].equals("-n") && i + 1 < args.length) {
                    count = Integer.parseInt(args[++i]);
                } else if (args[i].equals("-w") && i + 1 < args.length) {
                    timeout = Integer.parseInt(args[++i]);
                }
            }

            OutputFormatter.printBoxedHeader(doc, style, "Пинг " + host);
            
            InetAddress address = InetAddress.getByName(host);
            OutputFormatter.printBoxedLine(doc, style, 
                String.format("IP адрес: %s", address.getHostAddress()));

            String command = System.getProperty("os.name").toLowerCase().contains("windows") 
                ? String.format("ping -n %d -w %d %s", count, timeout, host)
                : String.format("ping -c %d -W %d %s", count, timeout/1000, host);

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
                System.err.println("Ошибка при выполнении ping: " + e.getMessage());
            }
        }
    }

    private void showUsage() throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Использование: ping <хост> [-n число] [-w таймаут]");
        OutputFormatter.printBoxedLine(doc, style, "Проверяет доступность хоста");
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Параметры:");
        OutputFormatter.printBoxedLine(doc, style, "  -n число    количество запросов (по умолчанию 4)");
        OutputFormatter.printBoxedLine(doc, style, "  -w таймаут  таймаут в миллисекундах (по умолчанию 1000)");
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Примеры:");
        OutputFormatter.printBoxedLine(doc, style, "  ping google.com");
        OutputFormatter.printBoxedLine(doc, style, "  ping 8.8.8.8 -n 10");
        OutputFormatter.printBoxedLine(doc, style, "  ping localhost -w 500");
        OutputFormatter.printBoxedFooter(doc, style);
    }

    @Override
    public String getDescription() {
        return "проверка доступности хоста";
    }
} 