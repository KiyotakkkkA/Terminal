package com.terminal.commands;

import java.net.InetAddress;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.utils.OutputFormatter;

public class TraceCommand extends AbstractCommand {

    public TraceCommand(StyledDocument doc, Style style) {
        super(doc, style);
    }

    @Override
    public void execute(String... args) {
        try {
            if (args.length < 1) {
                OutputFormatter.printBoxedHeader(doc, style, "Использование: trace <хост> [опции]");
                OutputFormatter.printBoxedLine(doc, style, "Опции:");
                OutputFormatter.printBoxedLine(doc, style, "  -m <число>    максимальное число прыжков");
                OutputFormatter.printBoxedLine(doc, style, "  -w <мс>       таймаут ожидания");
                OutputFormatter.printBoxedLine(doc, style, "  -d            показать DNS-имена");
                OutputFormatter.printBoxedFooter(doc, style);
                return;
            }

            String host = args[0];
            OutputFormatter.printBoxedHeader(doc, style, "Трассировка до " + host);
            OutputFormatter.printBoxedLine(doc, style, "Максимум 30 прыжков");

            InetAddress target = InetAddress.getByName(host);
            OutputFormatter.printBoxedLine(doc, style, "IP назначения: " + target.getHostAddress());
            OutputFormatter.printBoxedLine(doc, style, "");

            int maxHops = 30;
            int timeout = 1000;
            boolean showDns = false;

            for (int i = 1; i < args.length; i++) {
                if (args[i].equals("-m") && i + 1 < args.length) {
                    maxHops = Integer.parseInt(args[++i]);
                } else if (args[i].equals("-w") && i + 1 < args.length) {
                    timeout = Integer.parseInt(args[++i]);
                } else if (args[i].equals("-d")) {
                    showDns = true;
                }
            }

            for (int ttl = 1; ttl <= maxHops; ttl++) {
                StringBuilder line = new StringBuilder();
                line.append(String.format("%2d  ", ttl));
                
                String currentAddr = traceHop(host, ttl, timeout);
                if (currentAddr == null) {
                    line.append("* * *");
                    OutputFormatter.printBoxedLine(doc, style, line.toString());
                    continue;
                }

                if (showDns) {
                    try {
                        InetAddress addr = InetAddress.getByName(currentAddr);
                        line.append(String.format("%-15s (%s)", currentAddr, addr.getHostName()));
                    } catch (Exception e) {
                        line.append(currentAddr);
                    }
                } else {
                    line.append(currentAddr);
                }
                
                OutputFormatter.printBoxedLine(doc, style, line.toString());
                
                if (currentAddr.equals(target.getHostAddress())) {
                    OutputFormatter.printBoxedLine(doc, style, "");
                    OutputFormatter.printBoxedLine(doc, style, "Трассировка завершена");
                    OutputFormatter.printBoxedFooter(doc, style);
                    return;
                }
            }

            OutputFormatter.printBoxedLine(doc, style, "");
            OutputFormatter.printBoxedLine(doc, style, "Превышено максимальное количество прыжков");
            OutputFormatter.printBoxedFooter(doc, style);

        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private String traceHop(String host, int ttl, int timeout) {
        try {
            InetAddress addr = InetAddress.getByName(host);
            if (addr.isReachable(null, ttl, timeout)) {
                return addr.getHostAddress();
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public String getDescription() {
        return "трассировка маршрута до хоста";
    }
} 