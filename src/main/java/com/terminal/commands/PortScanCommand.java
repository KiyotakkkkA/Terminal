package com.terminal.commands;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.utils.OutputFormatter;

public class PortScanCommand extends AbstractCommand {
    private static final int TIMEOUT = 200;
    private static final int THREAD_POOL_SIZE = 50;
    private final Style promptStyle;

    public PortScanCommand(StyledDocument doc, Style style, Style promptStyle) {
        super(doc, style, null, "scan", "Сканирование портов", "NETWORK");
        this.promptStyle = promptStyle;
    }

    @Override
    public void executeCommand(String... args) {
        try {
            if (args.length < 1) {
                showUsage();
                return;
            }

            String host = args[0];
            int startPort = 1;
            int endPort = 1024;

            if (args.length > 1) {
                String[] ports = args[1].split("-");
                startPort = Integer.parseInt(ports[0]);
                endPort = ports.length > 1 ? Integer.parseInt(ports[1]) : startPort;
            }

            OutputFormatter.printBeautifulSection(doc, promptStyle, "СКАНИРОВАНИЕ ПОРТОВ");
            OutputFormatter.printBeautifulSection(doc, style, "Хост: " + host);
            
            String[] headers = {"Порт", "Статус", "Сервис"};
            List<String[]> dataList = new ArrayList<>();

            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            List<Future<PortScanResult>> futures = new ArrayList<>();

            for (int port = startPort; port <= endPort; port++) {
                final int currentPort = port;
                futures.add(executor.submit(() -> scanPort(host, currentPort)));
            }

            executor.shutdown();
            executor.awaitTermination(30, TimeUnit.SECONDS);

            for (Future<PortScanResult> future : futures) {
                PortScanResult result = future.get();
                if (result.isOpen) {
                    dataList.add(new String[]{
                        String.valueOf(result.port),
                        "открыт",
                        result.service != null ? result.service : "неизвестный сервис"
                    });
                }
            }

            if (dataList.isEmpty()) {
                OutputFormatter.printBeautifulSection(doc, style, "Открытых портов не найдено");
            } else {
                String[][] data = dataList.toArray(new String[0][]);
                OutputFormatter.printBeautifulTable(doc, style, headers, data);
            }

        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при сканировании портов: " + e.getMessage());
            }
        }
    }

    private PortScanResult scanPort(String host, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), TIMEOUT);
            return new PortScanResult(port, true, getServiceName(port));
        } catch (Exception e) {
            return new PortScanResult(port, false, null);
        }
    }

    private String getServiceName(int port) {
        switch (port) {
            case 21: return "FTP";
            case 22: return "SSH";
            case 23: return "Telnet";
            case 25: return "SMTP";
            case 53: return "DNS";
            case 80: return "HTTP";
            case 110: return "POP3";
            case 143: return "IMAP";
            case 443: return "HTTPS";
            case 3306: return "MySQL";
            case 3389: return "RDP";
            case 5432: return "PostgreSQL";
            case 27017: return "MongoDB";
            default: return null;
        }
    }

    private void showUsage() throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Использование: portscan <хост> [диапазон]");
        OutputFormatter.printBoxedLine(doc, style, "Сканирует порты указанного хоста");
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Параметры:");
        OutputFormatter.printBoxedLine(doc, style, "  хост      IP адрес или доменное имя");
        OutputFormatter.printBoxedLine(doc, style, "  диапазон  диапазон портов (например: 80 или 1-1024)");
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Примеры:");
        OutputFormatter.printBoxedLine(doc, style, "  portscan localhost");
        OutputFormatter.printBoxedLine(doc, style, "  portscan example.com 80");
        OutputFormatter.printBoxedLine(doc, style, "  portscan 192.168.1.1 20-25");
        OutputFormatter.printBoxedFooter(doc, style);
    }

    private static class PortScanResult {
        final int port;
        final boolean isOpen;
        final String service;

        PortScanResult(int port, boolean isOpen, String service) {
            this.port = port;
            this.isOpen = isOpen;
            this.service = service;
        }
    }

    @Override
    public String getDescription() {
        return "сканирование портов хоста";
    }
} 