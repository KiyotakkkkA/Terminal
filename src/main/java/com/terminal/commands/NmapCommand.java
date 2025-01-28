package com.terminal.commands;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.utils.OutputFormatter;

public class NmapCommand extends AbstractCommand {
    private static final int THREAD_POOL_SIZE = 50;
    private static final int TIMEOUT = 1000;

    public NmapCommand(StyledDocument doc, Style style) {
        super(doc, style);
    }

    @Override
    public void executeCommand(String... args) {
        try {
            if (args.length < 1) {
                showUsage();
                return;
            }

            String target = args[0];
            boolean scanPorts = args.length > 1 && args[1].equals("-p");
            
            OutputFormatter.printBoxedHeader(doc, style, "Сканирование сети");
            OutputFormatter.printBoxedLine(doc, style, "Цель: " + target);

            if (target.contains("/")) {
                String[] parts = target.split("/");
                String baseIP = parts[0].substring(0, parts[0].lastIndexOf(".") + 1);
                int mask = Integer.parseInt(parts[1]);
                int hosts = (int) Math.pow(2, 32 - mask);

                OutputFormatter.printBoxedLine(doc, style, String.format("Подсеть: %s0/%d", baseIP, mask));
                OutputFormatter.printBoxedLine(doc, style, String.format("Количество хостов: %d", hosts));
                OutputFormatter.printBoxedLine(doc, style, "");

                ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
                List<Future<HostScanResult>> futures = new ArrayList<>();

                for (int i = 1; i < hosts && i < 255; i++) {
                    String ip = baseIP + i;
                    futures.add(executor.submit(() -> scanHost(ip, scanPorts)));
                }

                executor.shutdown();
                executor.awaitTermination(30, TimeUnit.SECONDS);

                boolean foundHosts = false;
                for (Future<HostScanResult> future : futures) {
                    HostScanResult result = future.get();
                    if (result.isUp) {
                        foundHosts = true;
                        OutputFormatter.printBoxedLine(doc, style, String.format("Хост %s доступен", result.ip));
                        if (result.hostname != null) {
                            OutputFormatter.printBoxedLine(doc, style, String.format("  Имя: %s", result.hostname));
                        }
                        if (result.openPorts != null && !result.openPorts.isEmpty()) {
                            OutputFormatter.printBoxedLine(doc, style, "  Открытые порты:");
                            for (Integer port : result.openPorts) {
                                OutputFormatter.printBoxedLine(doc, style, String.format("    %d: %s", 
                                    port, getServiceName(port)));
                            }
                        }
                    }
                }

                if (!foundHosts) {
                    OutputFormatter.printBoxedLine(doc, style, "Активных хостов не найдено");
                }

            } else {
                HostScanResult result = scanHost(target, scanPorts);
                if (result.isUp) {
                    OutputFormatter.printBoxedLine(doc, style, "Хост доступен");
                    if (result.hostname != null) {
                        OutputFormatter.printBoxedLine(doc, style, "Имя: " + result.hostname);
                    }
                    if (result.openPorts != null && !result.openPorts.isEmpty()) {
                        OutputFormatter.printBoxedLine(doc, style, "Открытые порты:");
                        for (Integer port : result.openPorts) {
                            OutputFormatter.printBoxedLine(doc, style, String.format("  %d: %s", 
                                port, getServiceName(port)));
                        }
                    }
                } else {
                    OutputFormatter.printBoxedLine(doc, style, "Хост недоступен");
                }
            }

            OutputFormatter.printBoxedFooter(doc, style);

        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при сканировании: " + e.getMessage());
            }
        }
    }

    private HostScanResult scanHost(String ip, boolean scanPorts) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            if (addr.isReachable(TIMEOUT)) {
                HostScanResult result = new HostScanResult(ip);
                result.isUp = true;
                try {
                    result.hostname = addr.getHostName();
                } catch (Exception e) {
                }
                
                if (scanPorts) {
                    result.openPorts = new ArrayList<>();
                    for (int port : getCommonPorts()) {
                        try {
                            java.net.Socket socket = new java.net.Socket();
                            socket.connect(new java.net.InetSocketAddress(addr, port), TIMEOUT);
                            socket.close();
                            result.openPorts.add(port);
                        } catch (Exception e) {
                        }
                    }
                    Collections.sort(result.openPorts);
                }
                return result;
            }
        } catch (Exception e) {
        }
        return new HostScanResult(ip);
    }

    private int[] getCommonPorts() {
        return new int[] {
            21, 22, 23, 25, 53, 80, 110, 143, 443, 445, 
            3306, 3389, 5432, 8080, 27017
        };
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
            case 445: return "SMB";
            case 3306: return "MySQL";
            case 3389: return "RDP";
            case 5432: return "PostgreSQL";
            case 8080: return "HTTP-Alt";
            case 27017: return "MongoDB";
            default: return "unknown";
        }
    }

    private void showUsage() throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Использование: nmap <цель> [-p]");
        OutputFormatter.printBoxedLine(doc, style, "Сканирует сеть или отдельный хост");
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Параметры:");
        OutputFormatter.printBoxedLine(doc, style, "  цель     IP адрес, имя хоста или подсеть (например: 192.168.1.0/24)");
        OutputFormatter.printBoxedLine(doc, style, "  -p       включить сканирование портов");
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Примеры:");
        OutputFormatter.printBoxedLine(doc, style, "  nmap localhost           - сканировать локальный хост");
        OutputFormatter.printBoxedLine(doc, style, "  nmap 192.168.1.1 -p     - сканировать хост с проверкой портов");
        OutputFormatter.printBoxedLine(doc, style, "  nmap 192.168.1.0/24     - сканировать всю подсеть");
        OutputFormatter.printBoxedFooter(doc, style);
    }

    private static class HostScanResult {
        final String ip;
        boolean isUp;
        String hostname;
        List<Integer> openPorts;

        HostScanResult(String ip) {
            this.ip = ip;
            this.isUp = false;
        }
    }

    @Override
    public String getDescription() {
        return "сканирование сети и хостов";
    }
} 