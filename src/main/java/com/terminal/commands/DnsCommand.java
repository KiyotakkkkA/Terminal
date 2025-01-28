package com.terminal.commands;

import java.net.InetAddress;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import org.xbill.DNS.AAAARecord;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.MXRecord;
import org.xbill.DNS.NSRecord;
import org.xbill.DNS.Name;
import org.xbill.DNS.PTRRecord;
import org.xbill.DNS.ReverseMap;
import org.xbill.DNS.TXTRecord;
import org.xbill.DNS.Type;

import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class DnsCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;

    public DnsCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style, null, "dns", "DNS-запросы к доменным именам", "NETWORK");
        this.pathHolder = pathHolder;
    }

    @Override
    protected void initializeSubCommands() {
        addSubCommand("lookup", "прямой DNS-запрос");
        addSubCommand("reverse", "обратный DNS-запрос");
        addSubCommand("mx", "получить MX-записи");
        addSubCommand("ns", "получить NS-записи");
        addSubCommand("all", "получить все записи");
    }

    @Override
    public void executeCommand(String... args) {
        try {
            if (args.length < 2) {
                OutputFormatter.printBoxedHeader(doc, style, "Использование: dns <операция> <домен>");
                OutputFormatter.printBoxedLine(doc, style, "Операции:");
                OutputFormatter.printBoxedLine(doc, style, "  lookup      прямой DNS-запрос");
                OutputFormatter.printBoxedLine(doc, style, "  reverse     обратный DNS-запрос");
                OutputFormatter.printBoxedLine(doc, style, "  mx          получить MX-записи");
                OutputFormatter.printBoxedLine(doc, style, "  ns          получить NS-записи");
                OutputFormatter.printBoxedLine(doc, style, "  all         получить все записи");
                OutputFormatter.printBoxedFooter(doc, style);
                return;
            }

            String operation = args[0].toLowerCase();
            String domain = args[1];

            switch (operation) {
                case "lookup":
                    performLookup(domain);
                    break;
                case "reverse":
                    performReverseLookup(domain);
                    break;
                case "mx":
                    lookupMXRecords(domain);
                    break;
                case "ns":
                    lookupNSRecords(domain);
                    break;
                case "all":
                    performAllLookups(domain);
                    break;
                default:
                    OutputFormatter.printError(doc, style, "Неизвестная операция");
            }
        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void performLookup(String domain) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "DNS записи для " + domain);
        
        org.xbill.DNS.Record[] aRecords = new Lookup(domain, Type.A).run();
        if (aRecords != null && aRecords.length > 0) {
            for (org.xbill.DNS.Record record : aRecords) {
                ARecord a = (ARecord) record;
                OutputFormatter.printBoxedLine(doc, style, String.format("IPv4: %s", a.getAddress().getHostAddress()));
            }
        } else {
            OutputFormatter.printBoxedLine(doc, style, "IPv4: записи не найдены");
        }

        org.xbill.DNS.Record[] aaaaRecords = new Lookup(domain, Type.AAAA).run();
        if (aaaaRecords != null && aaaaRecords.length > 0) {
            for (org.xbill.DNS.Record record : aaaaRecords) {
                AAAARecord aaaa = (AAAARecord) record;
                String ipv6 = aaaa.getAddress().getHostAddress();
                if (ipv6.length() > 41) {
                    ipv6 = ipv6.substring(0, 38) + "...";
                }
                OutputFormatter.printBoxedLine(doc, style, String.format("IPv6: %s", ipv6));
            }
        } else {
            OutputFormatter.printBoxedLine(doc, style, "IPv6: записи не найдены");
        }
        
        OutputFormatter.printBoxedFooter(doc, style);
    }

    private void performReverseLookup(String ip) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Обратный DNS-запрос для " + ip);
        
        try {
            InetAddress addr = InetAddress.getByName(ip);
            Name name = ReverseMap.fromAddress(addr);
            org.xbill.DNS.Record[] records = new Lookup(name, Type.PTR).run();
            
            if (records != null && records.length > 0) {
                for (org.xbill.DNS.Record record : records) {
                    PTRRecord ptr = (PTRRecord) record;
                    String hostname = ptr.getTarget().toString();
                    if (hostname.length() > 41) {
                        hostname = hostname.substring(0, 38) + "...";
                    }
                    OutputFormatter.printBoxedLine(doc, style, String.format("Имя: %s", hostname));
                }
            } else {
                OutputFormatter.printBoxedLine(doc, style, "Записи не найдены");
            }
        } catch (Exception e) {
            OutputFormatter.printError(doc, style, e.getMessage());
        }
        
        OutputFormatter.printBoxedFooter(doc, style);
    }

    private void lookupMXRecords(String domain) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "MX записи для " + domain);
        OutputFormatter.printBoxedLine(doc, style, "Приоритет  Почтовый сервер");
        OutputFormatter.printBoxedLine(doc, style, "");
        
        try {
            org.xbill.DNS.Record[] records = new Lookup(domain, Type.MX).run();
            
            if (records != null && records.length > 0) {
                for (org.xbill.DNS.Record record : records) {
                    MXRecord mx = (MXRecord) record;
                    String server = mx.getTarget().toString();
                    if (server.length() > 41) {
                        server = server.substring(0, 38) + "...";
                    }
                    OutputFormatter.printBoxedLine(doc, style, String.format("%-10d %s", mx.getPriority(), server));
                }
            } else {
                OutputFormatter.printBoxedLine(doc, style, "MX записи не найдены");
            }
        } catch (Exception e) {
            OutputFormatter.printError(doc, style, e.getMessage());
        }
        
        OutputFormatter.printBoxedFooter(doc, style);
    }

    private void lookupNSRecords(String domain) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "NS записи для " + domain);
        
        try {
            org.xbill.DNS.Record[] records = new Lookup(domain, Type.NS).run();
            
            if (records != null && records.length > 0) {
                for (org.xbill.DNS.Record record : records) {
                    NSRecord ns = (NSRecord) record;
                    String server = ns.getTarget().toString();
                    if (server.length() > 41) {
                        server = server.substring(0, 38) + "...";
                    }
                    OutputFormatter.printBoxedLine(doc, style, String.format("NS: %s", server));
                }
            } else {
                OutputFormatter.printBoxedLine(doc, style, "NS записи не найдены");
            }
        } catch (Exception e) {
            OutputFormatter.printError(doc, style, e.getMessage());
        }
        
        OutputFormatter.printBoxedFooter(doc, style);
    }

    private void performAllLookups(String domain) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Все DNS записи для " + domain);
        OutputFormatter.printBoxedFooter(doc, style);
        OutputFormatter.printBoxedLine(doc, style, "");
        
        performLookup(domain);
        OutputFormatter.printBoxedLine(doc, style, "");
        
        lookupMXRecords(domain);
        OutputFormatter.printBoxedLine(doc, style, "");
        
        lookupNSRecords(domain);
        OutputFormatter.printBoxedLine(doc, style, "");
        
        OutputFormatter.printBoxedHeader(doc, style, "TXT записи");
        
        try {
            org.xbill.DNS.Record[] txtRecords = new Lookup(domain, Type.TXT).run();
            if (txtRecords != null && txtRecords.length > 0) {
                for (org.xbill.DNS.Record record : txtRecords) {
                    TXTRecord txt = (TXTRecord) record;
                    String txtRecord = txt.rdataToString();
                    while (txtRecord.length() > 54) {
                        OutputFormatter.printBoxedLine(doc, style, txtRecord.substring(0, 54));
                        txtRecord = txtRecord.substring(54);
                    }
                    if (txtRecord.length() > 0) {
                        OutputFormatter.printBoxedLine(doc, style, txtRecord);
                    }
                }
            } else {
                OutputFormatter.printBoxedLine(doc, style, "TXT записи не найдены");
            }
        } catch (Exception e) {
            OutputFormatter.printError(doc, style, e.getMessage());
        }
        
        OutputFormatter.printBoxedFooter(doc, style);
    }

    @Override
    public String getDescription() {
        return "DNS-запросы";
    }
} 