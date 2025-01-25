package com.terminal.commands;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.utils.OutputFormatter;

public class WebCommand extends AbstractCommand {

    public WebCommand(StyledDocument doc, Style style) {
        super(doc, style);
    }

    @Override
    public void execute(String... args) {
        try {
            if (args.length < 2) {
                showUsage();
                return;
            }

            String operation = args[0];
            String urlStr = args[1];

            switch (operation) {
                case "get":
                    doGet(urlStr);
                    break;
                case "post":
                    if (args.length < 3) {
                        OutputFormatter.printError(doc, style, "Требуются данные для POST-запроса");
                        return;
                    }
                    doPost(urlStr, args[2]);
                    break;
                case "headers":
                    getHeaders(urlStr);
                    break;
                case "ssl":
                    checkSSL(urlStr);
                    break;
                case "scan":
                    scanVulnerabilities(urlStr);
                    break;
                default:
                    OutputFormatter.printError(doc, style, "Неизвестная операция");
            }
        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при выполнении web-запроса: " + e.getMessage());
            }
        }
    }

    private void doGet(String urlStr) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "GET-запрос");
        OutputFormatter.printBoxedLine(doc, style, "URL: " + urlStr);
        OutputFormatter.printBoxedLine(doc, style, "");

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        OutputFormatter.printBoxedLine(doc, style, "Код ответа: " + responseCode);
        OutputFormatter.printBoxedLine(doc, style, "");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine).append("\n");
        }
        in.close();

        OutputFormatter.printBoxedLine(doc, style, "Ответ:");
        OutputFormatter.printBoxedLine(doc, style, response.toString());
        OutputFormatter.printBoxedFooter(doc, style);
    }

    private void doPost(String urlStr, String data) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "POST-запрос");
        OutputFormatter.printBoxedLine(doc, style, "URL: " + urlStr);
        OutputFormatter.printBoxedLine(doc, style, "Данные: " + data);
        OutputFormatter.printBoxedLine(doc, style, "");

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
            out.writeBytes(data);
            out.flush();
        }

        int responseCode = conn.getResponseCode();
        OutputFormatter.printBoxedLine(doc, style, "Код ответа: " + responseCode);
        OutputFormatter.printBoxedLine(doc, style, "");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine).append("\n");
        }
        in.close();

        OutputFormatter.printBoxedLine(doc, style, "Ответ:");
        OutputFormatter.printBoxedLine(doc, style, response.toString());
        OutputFormatter.printBoxedFooter(doc, style);
    }

    private void getHeaders(String urlStr) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Заголовки");
        OutputFormatter.printBoxedLine(doc, style, "URL: " + urlStr);
        OutputFormatter.printBoxedLine(doc, style, "");

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("HEAD");

        Map<String, List<String>> headers = conn.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String key = entry.getKey() == null ? "" : entry.getKey() + ": ";
            OutputFormatter.printBoxedLine(doc, style, key + String.join(", ", entry.getValue()));
        }

        OutputFormatter.printBoxedFooter(doc, style);
    }

    private void checkSSL(String urlStr) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Проверка SSL");
        OutputFormatter.printBoxedLine(doc, style, "URL: " + urlStr);
        OutputFormatter.printBoxedLine(doc, style, "");

        URL url = new URL(urlStr);
        if (!url.getProtocol().equals("https")) {
            OutputFormatter.printError(doc, style, "URL должен использовать HTTPS");
            return;
        }

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (SSLSocket socket = (SSLSocket) factory.createSocket(url.getHost(), 443)) {
            OutputFormatter.printBoxedLine(doc, style, "SSL соединение установлено");
            OutputFormatter.printBoxedLine(doc, style, "");

            OutputFormatter.printBoxedLine(doc, style, "Поддерживаемые протоколы:");
            for (String protocol : socket.getEnabledProtocols()) {
                OutputFormatter.printBoxedLine(doc, style, "  " + protocol);
            }

            OutputFormatter.printBoxedLine(doc, style, "");
            OutputFormatter.printBoxedLine(doc, style, "Поддерживаемые шифры:");
            for (String cipher : socket.getEnabledCipherSuites()) {
                OutputFormatter.printBoxedLine(doc, style, "  " + cipher);
            }
        }

        OutputFormatter.printBoxedFooter(doc, style);
    }

    private void scanVulnerabilities(String urlStr) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Сканирование уязвимостей");
        OutputFormatter.printBoxedLine(doc, style, "URL: " + urlStr);
        OutputFormatter.printBoxedLine(doc, style, "");

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        OutputFormatter.printBoxedLine(doc, style, "Проверка HTTP-методов:");
        String[] methods = {"GET", "POST", "PUT", "DELETE", "OPTIONS", "TRACE"};
        for (String method : methods) {
            try {
                HttpURLConnection testConn = (HttpURLConnection) url.openConnection();
                testConn.setRequestMethod(method);
                int responseCode = testConn.getResponseCode();
                if (responseCode != 405) { // 405 = Method Not Allowed
                    OutputFormatter.printBoxedLine(doc, style, 
                        String.format("  %s: разрешен (код %d)", method, responseCode));
                }
            } catch (Exception e) {
            }
        }

        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Проверка заголовков безопасности:");
        
        Map<String, List<String>> headers = conn.getHeaderFields();
        checkSecurityHeader(headers, "Strict-Transport-Security");
        checkSecurityHeader(headers, "X-Frame-Options");
        checkSecurityHeader(headers, "X-Content-Type-Options");
        checkSecurityHeader(headers, "X-XSS-Protection");
        checkSecurityHeader(headers, "Content-Security-Policy");

        OutputFormatter.printBoxedFooter(doc, style);
    }

    private void checkSecurityHeader(Map<String, List<String>> headers, String headerName) throws Exception {
        List<String> values = headers.get(headerName);
        if (values == null || values.isEmpty()) {
            OutputFormatter.printBoxedLine(doc, style, 
                String.format("  %s: отсутствует", headerName));
        } else {
            OutputFormatter.printBoxedLine(doc, style, 
                String.format("  %s: %s", headerName, String.join(", ", values)));
        }
    }

    private void showUsage() throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Использование: web <операция> <url> [данные]");
        OutputFormatter.printBoxedLine(doc, style, "Выполняет HTTP-запросы");
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Операции:");
        OutputFormatter.printBoxedLine(doc, style, "  get <url>           - выполнить GET-запрос");
        OutputFormatter.printBoxedLine(doc, style, "  post <url> <data>   - выполнить POST-запрос");
        OutputFormatter.printBoxedLine(doc, style, "  headers <url>       - получить заголовки");
        OutputFormatter.printBoxedLine(doc, style, "  ssl <url>           - проверить SSL");
        OutputFormatter.printBoxedLine(doc, style, "  scan <url>          - сканировать уязвимости");
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Примеры:");
        OutputFormatter.printBoxedLine(doc, style, "  web get https://example.com");
        OutputFormatter.printBoxedLine(doc, style, "  web post https://example.com/api \"data=test\"");
        OutputFormatter.printBoxedLine(doc, style, "  web ssl https://example.com");
        OutputFormatter.printBoxedFooter(doc, style);
    }

    @Override
    public String getDescription() {
        return "выполнение HTTP-запросов";
    }
} 