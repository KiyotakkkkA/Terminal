package com.terminal.commands;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.AbstractAsyncCommand;
import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class ConCommand extends AbstractAsyncCommand {
    private static final int SCAN_TIMEOUT = 200;
    private static final int THREAD_POOL_SIZE = 50;
    private static final int EXECUTOR_TIMEOUT = 5;
    private final Style promptStyle;
    private volatile boolean isRunning = true;

    public ConCommand(StyledDocument doc, Style style, Style promptStyle, CurrentPathHolder pathHolder) {
        super(doc, style, pathHolder);
        this.promptStyle = promptStyle;
    }

    @Override
    public CompletableFuture<Void> executeAsync(String[] args) {
        isRunning = true;
        output.startAnimation(FRAMES, FRAME_DELAY);
        
        return CompletableFuture.runAsync(() -> {
            try {
                scanNetworkAsync();
            } catch (Exception e) {
                if (!isInterrupted()) {
                    output.completeWithError(e.getMessage());
                }
            } finally {
                isRunning = false;
                if (!isInterrupted()) {
                    output.complete();
                }
            }
        });
    }

    @Override
    protected void executeCommand(String... args) throws Exception {
        CompletableFuture<Void> future = executeAsync(args);
        Thread.sleep(100);
    }

    private void scanNetworkAsync() throws Exception {
        if (!isRunning) return;
        
        ExecutorService networkExecutor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        try {
            List<NetworkInterface> activeInterfaces = new ArrayList<>();
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            
            while (interfaces.hasMoreElements() && isRunning) {
                NetworkInterface ni = interfaces.nextElement();
                if (ni.isUp() && !ni.isLoopback()) {
                    activeInterfaces.add(ni);
                    collectAndDisplayInterfaceInfo(ni, activeInterfaces.size());
                }
            }

            if (!isRunning) return;
            
            InetAddress localhost = InetAddress.getLocalHost();
            String[][] localhostData = {
                {"Имя хоста", localhost.getHostName()},
                {"IP адрес", localhost.getHostAddress()},
                {"Каноническое имя", localhost.getCanonicalHostName()}
            };
            String[] headers = {"Параметр", "Значение"};
            OutputFormatter.printBeautifulSection(doc, promptStyle, "ЛОКАЛЬНЫЙ ХОСТ");
            OutputFormatter.printBeautifulTable(doc, style, headers, localhostData);

            if (!isRunning) return;

            for (NetworkInterface ni : activeInterfaces) {
                if (!isRunning) break;
                
                for (InterfaceAddress addr : ni.getInterfaceAddresses()) {
                    if (!isRunning) break;
                    
                    if (addr.getAddress().isSiteLocalAddress()) {
                        String subnet = addr.getAddress().getHostAddress();
                        subnet = subnet.substring(0, subnet.lastIndexOf(".") + 1);
                        
                        String[][] networkData = {
                            {"Сеть", subnet + "0/" + addr.getNetworkPrefixLength()},
                            {"Маска подсети", calculateNetmask(addr.getNetworkPrefixLength())},
                            {"Broadcast", addr.getBroadcast() != null ? addr.getBroadcast().getHostAddress() : "Недоступен"}
                        };
                        OutputFormatter.printBeautifulSection(doc, promptStyle, "ИНФОРМАЦИЯ О СЕТИ - " + subnet + "0/" + addr.getNetworkPrefixLength());
                        OutputFormatter.printBeautifulTable(doc, style, headers, networkData);

                        if (!isRunning) break;
                        
                        OutputFormatter.printBeautifulSection(doc, promptStyle, "СКАНИРОВАНИЕ СЕТИ");
                        scanAndDisplayDevices(subnet);
                    }
                }
            }
        } finally {
            networkExecutor.shutdownNow();
        }
    }

    private void collectAndDisplayInterfaceInfo(NetworkInterface ni, int number) throws Exception {
        if (!isRunning) return;
        
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
        if (!isRunning) return;
        
        List<Future<String[]>> futures = new ArrayList<>();
        List<String[]> foundDevices = new ArrayList<>();

        try {
            InetAddress broadcast = InetAddress.getByName(subnet + "255");
            futures.add(CompletableFuture.supplyAsync(() -> checkHost(broadcast)));

            for (int i = 1; i <= 5 && isRunning; i++) {
                final String host = subnet + i;
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        return checkHost(InetAddress.getByName(host));
                    } catch (Exception e) {
                        return null;
                    }
                }));
            }

            for (int i = 6; i < 255 && isRunning; i++) {
                final String host = subnet + i;
                futures.add(CompletableFuture.supplyAsync(() -> {
                    try {
                        return checkHost(InetAddress.getByName(host));
                    } catch (Exception e) {
                        return null;
                    }
                }));
            }

            for (Future<String[]> future : futures) {
                if (!isRunning) break;
                try {
                    String[] result = future.get(SCAN_TIMEOUT * 2, TimeUnit.MILLISECONDS);
                    if (result != null) {
                        foundDevices.add(result);
                    }
                } catch (Exception ignored) {}
            }

            if (!isRunning) return;

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
    public void interrupt() {
        isRunning = false;
        super.interrupt();
    }

    @Override
    public String[] getSuggestions(String[] args) {
        return new String[0];
    }
} 