package com.terminal.commands;

import java.net.InetAddress;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.core.CommandContext;
import com.terminal.utils.OutputFormatter;

public class PingCommand extends AbstractCommand {
    private volatile boolean isRunning = true;

    public PingCommand(StyledDocument doc, Style style) {
        super(doc, style, null, "ping", "Проверка доступности хоста", "NETWORK");
    }

    @Override
    public void execute(CommandContext context) {
        try {
            String[] args = context.getArgs();
            if (args.length < 1) {
                showUsage(context);
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

            OutputFormatter.printBeautifulSection(context.getDoc(), context.getStyle(), "Пинг " + host);
            
            InetAddress address = InetAddress.getByName(host);
            String hostname = address.getHostName();
            String ip = address.getHostAddress();
            
            OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(), 
                String.format("Пинг %s [%s] с %d байтами данных:", hostname, ip, 32));

            int successful = 0;
            long totalTime = 0;
            long minTime = Long.MAX_VALUE;
            long maxTime = 0;

            for (int i = 0; i < count && isRunning; i++) {
                long startTime = System.currentTimeMillis();
                boolean reachable = address.isReachable(timeout);
                long endTime = System.currentTimeMillis();
                long time = endTime - startTime;

                if (reachable) {
                    successful++;
                    totalTime += time;
                    minTime = Math.min(minTime, time);
                    maxTime = Math.max(maxTime, time);
                    
                    OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(),
                        String.format("Ответ от %s: время=%dмс", ip, time));
                } else {
                    OutputFormatter.printError(context.getDoc(), context.getStyle(),
                        String.format("Превышен интервал ожидания для %s", ip));
                }

                if (i < count - 1) Thread.sleep(1000);
            }

            if (successful > 0) {
                double avgTime = totalTime / (double) successful;
                double lossPercent = ((count - successful) / (double) count) * 100;
                
                String[][] stats = {
                    {"Отправлено", String.valueOf(count)},
                    {"Получено", String.valueOf(successful)},
                    {"Потеряно", String.format("%.0f%% (%d)", lossPercent, count - successful)},
                    {"Минимальное время", minTime + " мс"},
                    {"Максимальное время", maxTime + " мс"},
                    {"Среднее время", String.format("%.1f мс", avgTime)}
                };
                
                OutputFormatter.printBeautifulTable(context.getDoc(), context.getStyle(),
                    new String[]{"Параметр", "Значение"}, stats);
            }
            
        } catch (Exception e) {
            try {
                OutputFormatter.printError(context.getDoc(), context.getStyle(), "Ошибка: " + e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при выполнении команды ping: " + e.getMessage());
            }
        }
    }

    private void showUsage(CommandContext context) throws Exception {
        OutputFormatter.printBeautifulSection(context.getDoc(), context.getStyle(), "Использование команды ping");
        OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(),
            "ping <хост> [-n число] [-w таймаут]\n" +
            "  -n число    количество запросов\n" +
            "  -w таймаут  таймаут в миллисекундах");
    }

    @Override
    public void executeCommand(String... args) throws Exception {
        CommandContext context = new CommandContext("", args, doc, style, pathHolder);
        execute(context);
    }

    public void interrupt() {
        isRunning = false;
    }
} 