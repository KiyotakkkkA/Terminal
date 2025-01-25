package com.terminal.commands;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.utils.OutputFormatter;

public class ConCommand extends SystemCommandBase {
    private static final int SCAN_TIMEOUT = 1000;
    private static final int THREAD_POOL_SIZE = 20;

    public ConCommand(StyledDocument doc, Style style) {
        super(doc, style);
    }

    @Override
    public void execute(String... args) {
        try {
            OutputFormatter.printBoxedHeader(doc, style, "Информация о сетевых подключениях");
            
            List<NetworkInterface> activeInterfaces = new ArrayList<>();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isUp() && !ni.isLoopback()) {
                    activeInterfaces.add(ni);
                    printInterfaceInfo(ni);
                }
            }

            OutputFormatter.printBoxedLine(doc, style, "");
            OutputFormatter.printBoxedLine(doc, style, "Локальный хост:");
            InetAddress localhost = InetAddress.getLocalHost();
            OutputFormatter.printBoxedLine(doc, style, "  Имя хоста: " + localhost.getHostName());
            OutputFormatter.printBoxedLine(doc, style, "  IP адрес: " + localhost.getHostAddress());
            OutputFormatter.printBoxedLine(doc, style, "  Каноническое имя: " + localhost.getCanonicalHostName());
            
            OutputFormatter.printBoxedLine(doc, style, "");
            OutputFormatter.printBoxedLine(doc, style, "Сканирование сети:");
            for (NetworkInterface ni : activeInterfaces) {
                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    InetAddress address = ia.getAddress();
                    if (address.isSiteLocalAddress()) {
                        scanNetwork(address, ia.getNetworkPrefixLength());
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

    private void printInterfaceInfo(NetworkInterface ni) throws Exception {
        OutputFormatter.printBoxedLine(doc, style, "Интерфейс: " + ni.getDisplayName());
        OutputFormatter.printBoxedLine(doc, style, "  Имя: " + ni.getName());
        
        byte[] mac = ni.getHardwareAddress();
        if (mac != null) {
            StringBuilder macAddress = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                macAddress.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            OutputFormatter.printBoxedLine(doc, style, "  MAC адрес: " + macAddress.toString());
        }
        
        for (InterfaceAddress addr : ni.getInterfaceAddresses()) {
            OutputFormatter.printBoxedLine(doc, style, "  IP: " + addr.getAddress().getHostAddress() + 
                "/" + addr.getNetworkPrefixLength());
            if (addr.getBroadcast() != null) {
                OutputFormatter.printBoxedLine(doc, style, "  Broadcast: " + addr.getBroadcast().getHostAddress());
            }
        }
        
        OutputFormatter.printBoxedLine(doc, style, "  MTU: " + ni.getMTU());
        OutputFormatter.printBoxedLine(doc, style, "  Multicast: " + ni.supportsMulticast());
        OutputFormatter.printBoxedLine(doc, style, "  Point-to-point: " + ni.isPointToPoint());
        OutputFormatter.printBoxedLine(doc, style, "  Virtual: " + ni.isVirtual());
        OutputFormatter.printBoxedLine(doc, style, "  Loopback: " + ni.isLoopback());
        OutputFormatter.printBoxedLine(doc, style, "");
    }

    private void scanNetwork(InetAddress networkAddress, short prefixLength) throws Exception {
        String baseIP = networkAddress.getHostAddress();
        String subnet = baseIP.substring(0, baseIP.lastIndexOf(".") + 1);
        
        OutputFormatter.printBoxedLine(doc, style, "  Сканирование сети " + subnet + "0/" + prefixLength);
        
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Future<String>> futures = new ArrayList<>();

        for (int i = 1; i < 255; i++) {
            final String host = subnet + i;
            futures.add(executor.submit(() -> {
                try {
                    InetAddress addr = InetAddress.getByName(host);
                    if (addr.isReachable(SCAN_TIMEOUT)) {
                        String hostname = addr.getCanonicalHostName();
                        return String.format("    Найдено устройство: %s (%s)",
                            addr.getHostAddress(),
                            hostname.equals(addr.getHostAddress()) ? "неизвестно" : hostname);
                    }
                } catch (Exception ignored) {}
                return null;
            }));
        }
        
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        boolean devicesFound = false;
        for (Future<String> future : futures) {
            try {
                String result = future.get();
                if (result != null) {
                    OutputFormatter.printBoxedLine(doc, style, result);
                    devicesFound = true;
                }
            } catch (Exception ignored) {}
        }
        
        if (!devicesFound) {
            OutputFormatter.printBoxedLine(doc, style, "    Устройства не найдены");
        }
        OutputFormatter.printBoxedLine(doc, style, "    Сканирование завершено");
        OutputFormatter.printBoxedLine(doc, style, "");
    }

    @Override
    public String getDescription() {
        return "информация о сетевых подключениях и устройствах в сети";
    }

    @Override
    public String executeAndGetOutput(String... args) {
        StringBuilder output = new StringBuilder();
        try {
            List<NetworkInterface> activeInterfaces = new ArrayList<>();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isUp() && !ni.isLoopback()) {
                    activeInterfaces.add(ni);
                    appendInterfaceInfo(output, ni);
                }
            }

            InetAddress localhost = InetAddress.getLocalHost();
            output.append("\nЛокальный хост:\n")
                  .append("Имя хоста: ").append(localhost.getHostName()).append("\n")
                  .append("IP адрес: ").append(localhost.getHostAddress()).append("\n")
                  .append("Каноническое имя: ").append(localhost.getCanonicalHostName()).append("\n\n");

            output.append("Сканирование сети:\n");
            for (NetworkInterface ni : activeInterfaces) {
                for (InterfaceAddress ia : ni.getInterfaceAddresses()) {
                    InetAddress address = ia.getAddress();
                    if (address.isSiteLocalAddress()) {
                        appendNetworkScan(output, address, ia.getNetworkPrefixLength());
                    }
                }
            }
        } catch (Exception e) {
            output.append("Ошибка: ").append(e.getMessage()).append("\n");
        }
        return output.toString();
    }

    private void appendInterfaceInfo(StringBuilder output, NetworkInterface ni) throws Exception {
        output.append("\nИнтерфейс: ").append(ni.getDisplayName()).append("\n");
        output.append("Имя: ").append(ni.getName()).append("\n");
        
        byte[] mac = ni.getHardwareAddress();
        if (mac != null) {
            StringBuilder macAddress = new StringBuilder("MAC адрес: ");
            for (int i = 0; i < mac.length; i++) {
                macAddress.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            output.append(macAddress.toString()).append("\n");
        }
        
        for (InterfaceAddress addr : ni.getInterfaceAddresses()) {
            output.append("IP: ").append(addr.getAddress().getHostAddress())
                  .append("/").append(addr.getNetworkPrefixLength()).append("\n");
            if (addr.getBroadcast() != null) {
                output.append("Broadcast: ").append(addr.getBroadcast().getHostAddress()).append("\n");
            }
        }
        
        output.append("MTU: ").append(ni.getMTU()).append("\n")
              .append("Multicast: ").append(ni.supportsMulticast()).append("\n")
              .append("Point-to-point: ").append(ni.isPointToPoint()).append("\n")
              .append("Virtual: ").append(ni.isVirtual()).append("\n")
              .append("Loopback: ").append(ni.isLoopback()).append("\n");
    }

    private void appendNetworkScan(StringBuilder output, InetAddress networkAddress, short prefixLength) {
        try {
            String baseIP = networkAddress.getHostAddress();
            String subnet = baseIP.substring(0, baseIP.lastIndexOf(".") + 1);
            
            output.append("\nСканирование сети ").append(subnet).append("0/").append(prefixLength).append("\n");
            
            ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
            List<Future<String>> futures = new ArrayList<>();

            for (int i = 1; i < 255; i++) {
                final String host = subnet + i;
                futures.add(executor.submit(() -> {
                    try {
                        InetAddress addr = InetAddress.getByName(host);
                        if (addr.isReachable(SCAN_TIMEOUT)) {
                            String hostname = addr.getCanonicalHostName();
                            return String.format("  Найдено устройство: %s (%s)\n",
                                addr.getHostAddress(),
                                hostname.equals(addr.getHostAddress()) ? "неизвестно" : hostname);
                        }
                    } catch (Exception ignored) {}
                    return null;
                }));
            }
            
            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            boolean devicesFound = false;
            for (Future<String> future : futures) {
                String result = future.get();
                if (result != null) {
                    output.append(result);
                    devicesFound = true;
                }
            }
            
            if (!devicesFound) {
                output.append("  Устройства не найдены\n");
            }
            output.append("  Сканирование завершено\n");
            
        } catch (Exception e) {
            output.append("Ошибка сканирования: ").append(e.getMessage()).append("\n");
        }
    }
} 