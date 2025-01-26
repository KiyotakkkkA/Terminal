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

import com.terminal.sdk.AsyncCommand;
import com.terminal.utils.OutputFormatter;

public class ConCommand extends AsyncCommand {
    private static final int SCAN_TIMEOUT = 200;
    private static final int THREAD_POOL_SIZE = 50;
    private static final int EXECUTOR_TIMEOUT = 5;
    private final Style promptStyle;

    public ConCommand(StyledDocument doc, Style style, Style promptStyle) {
        super(doc, style);
        this.promptStyle = promptStyle;
    }

    @Override
    public void execute(String... args) throws Exception {
        List<NetworkInterface> activeInterfaces = new ArrayList<>();
        startOutputBlock();
        
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            int interfaceNumber = 1;
            
            OutputFormatter.printBeautifulSection(doc, promptStyle, "ИНФОРМАЦИЯ О СЕТЕВЫХ ПОДКЛЮЧЕНИЯХ");
            
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isUp() && !ni.isLoopback()) {
                    activeInterfaces.add(ni);
                    collectAndDisplayInterfaceInfo(ni, interfaceNumber++);
                }
            }

            InetAddress localhost = InetAddress.getLocalHost();
            String[][] localhostData = {
                {"Имя хоста", localhost.getHostName()},
                {"IP адрес", localhost.getHostAddress()},
                {"Каноническое имя", localhost.getCanonicalHostName()}
            };
            String[] headers = {"Параметр", "Значение"};
            OutputFormatter.printBeautifulSection(doc, promptStyle, "ЛОКАЛЬНЫЙ ХОСТ");
            OutputFormatter.printBeautifulTable(doc, style, headers, localhostData);

            for (NetworkInterface ni : activeInterfaces) {
                for (InterfaceAddress addr : ni.getInterfaceAddresses()) {
                    if (addr.getAddress().isSiteLocalAddress()) {
                        String subnet = addr.getAddress().getHostAddress();
                        subnet = subnet.substring(0, subnet.lastIndexOf(".") + 1);
                        
                        String[][] networkData = {
                            {"Сеть", subnet + "0/" + addr.getNetworkPrefixLength()},
                            {"Маска подсети", calculateNetmask(addr.getNetworkPrefixLength())},
                            {"Broadcast", addr.getBroadcast() != null ? addr.getBroadcast().getHostAddress() : "Недоступен"}
                        };
                        OutputFormatter.printBeautifulSection(doc, promptStyle, "ИНФОРМАЦИЯ О СЕТИ");
                        OutputFormatter.printBeautifulTable(doc, style, headers, networkData);

                        OutputFormatter.printBeautifulSection(doc, promptStyle, "СКАНИРОВАНИЕ СЕТИ");
                        scanAndDisplayDevices(subnet);
                    }
                }
            }
        } finally {
            endOutputBlock();
            OutputFormatter.printBeautifulSectionEnd(doc, style);
            appendWithStyle("\n", style);
        }
    }

    private void collectAndDisplayInterfaceInfo(NetworkInterface ni, int number) throws Exception {
        String[] headers = {"Параметр", "Значение"};
        OutputFormatter.printBeautifulSection(doc, promptStyle, "СЕТЕВОЙ ИНТЕРФЕЙС №" + number);
        
        String[][] data = {
            {"Имя", ni.getName()},
            {"Отображаемое имя", ni.getDisplayName()},
            {"MAC адрес", formatMacAddress(ni.getHardwareAddress())},
            {"MTU", String.valueOf(ni.getMTU())},
            {"Multicast", ni.supportsMulticast() ? "да" : "нет"},
            {"Point-to-point", ni.isPointToPoint() ? "да" : "нет"},
            {"Virtual", ni.isVirtual() ? "да" : "нет"},
            {"Loopback", ni.isLoopback() ? "да" : "нет"}
        };
        
        List<String[]> ipAddresses = new ArrayList<>();
        for (InterfaceAddress addr : ni.getInterfaceAddresses()) {
            ipAddresses.add(new String[]{
                "IP адрес",
                addr.getAddress().getHostAddress() + "/" + addr.getNetworkPrefixLength()
            });
            if (addr.getBroadcast() != null) {
                ipAddresses.add(new String[]{
                    "Broadcast",
                    addr.getBroadcast().getHostAddress()
                });
            }
        }
        
        String[][] fullData = new String[data.length + ipAddresses.size()][2];
        System.arraycopy(data, 0, fullData, 0, data.length);
        for (int i = 0; i < ipAddresses.size(); i++) {
            fullData[data.length + i] = ipAddresses.get(i);
        }
        
        OutputFormatter.printBeautifulTable(doc, style, headers, fullData);
    }

    private void scanAndDisplayDevices(String subnet) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<Future<String[]>> futures = new ArrayList<>();
        List<String[]> foundDevices = new ArrayList<>();

        try {
            InetAddress broadcast = InetAddress.getByName(subnet + "255");
            futures.add(executor.submit(() -> checkHost(broadcast)));

            for (int i = 1; i <= 5; i++) {
                final String host = subnet + i;
                futures.add(executor.submit(() -> checkHost(InetAddress.getByName(host))));
            }

            for (int i = 6; i < 255; i++) {
                final String host = subnet + i;
                futures.add(executor.submit(() -> checkHost(InetAddress.getByName(host))));
            }

            executor.shutdown();
            if (!executor.awaitTermination(EXECUTOR_TIMEOUT, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }

            for (Future<String[]> future : futures) {
                try {
                    String[] result = future.get();
                    if (result != null) {
                        foundDevices.add(result);
                    }
                } catch (Exception ignored) {}
            }

            if (!foundDevices.isEmpty()) {
                String[][] tableData = new String[foundDevices.size()][7];
                String[] headers = {"IP адрес", "Имя", "Hostname", "Доступен", "Loopback", "Site Local", "Link Local"};
                for (int i = 0; i < foundDevices.size(); i++) {
                    tableData[i] = foundDevices.get(i);
                }
                OutputFormatter.printBeautifulTable(doc, style, headers, tableData);
            } else {
                OutputFormatter.printBeautifulMessage(doc, style, "Устройства не найдены");
            }
        } finally {
            executor.shutdownNow();
            appendWithStyle("\n", style);
        }
    }

    private String[] checkHost(InetAddress address) {
        try {
            if (address.isReachable(SCAN_TIMEOUT)) {
                String hostname = address.getCanonicalHostName();
                return new String[]{
                    address.getHostAddress(),
                    hostname.equals(address.getHostAddress()) ? "неизвестно" : hostname,
                    address.getHostName(),
                    address.isReachable(SCAN_TIMEOUT) ? "да" : "нет",
                    address.isLoopbackAddress() ? "да" : "нет",
                    address.isSiteLocalAddress() ? "да" : "нет",
                    address.isLinkLocalAddress() ? "да" : "нет"
                };
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String calculateNetmask(short prefixLength) {
        int mask = 0xffffffff << (32 - prefixLength);
        return String.format("%d.%d.%d.%d",
            (mask >> 24) & 0xFF,
            (mask >> 16) & 0xFF,
            (mask >> 8) & 0xFF,
            mask & 0xFF);
    }

    private String formatMacAddress(byte[] mac) {
        if (mac == null) {
            return "Недоступен";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
        }
        return sb.toString();
    }

    @Override
    public String getDescription() {
        return "информация о сетевых подключениях";
    }

    @Override
    public List<String> getSuggestions(String[] args) {
        return new ArrayList<>();
    }
} 