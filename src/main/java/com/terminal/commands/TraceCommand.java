package com.terminal.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.utils.OutputFormatter;

public class TraceCommand extends AbstractCommand {
    private final Style promptStyle;
    private volatile boolean isRunning = true;

    public TraceCommand(StyledDocument doc, Style style, Style promptStyle) {
        super(doc, style, null, "trace", "Трассировка маршрута", "NETWORK");
        this.promptStyle = promptStyle;
    }

    @Override
    public void executeCommand(String... args) {
        // Сбрасываем флаг в начале выполнения
        isRunning = true;
        
        if (args.length < 1) {
            showUsage();
            return;
        }

        BufferedReader reader = null;
        AtomicReference<Process> processRef = new AtomicReference<>();

        try {
            String target = args[0];
            OutputFormatter.printBeautifulSection(doc, promptStyle, "ТРАССИРОВКА МАРШРУТА");
            OutputFormatter.printBeautifulSection(doc, style, "К узлу: " + target);

            String[] headers = {"Хоп", "IP адрес", "Время", "Имя хоста"};
            List<String[]> dataList = new ArrayList<>();

            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
            String command = isWindows ? "tracert -h 30 " + target : "traceroute -m 30 " + target;

            ProcessBuilder pb = new ProcessBuilder();
            if (isWindows) {
                pb.command("cmd.exe", "/c", command);
            } else {
                pb.command("sh", "-c", command);
            }

            Process process = pb.start();
            processRef.set(process);
            reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), 
                    isWindows ? "CP866" : "UTF-8")
            );

            // Устанавливаем таймаут на весь процесс
            Thread timeoutThread = new Thread(() -> {
                try {
                    Thread.sleep(30000); // 30 секунд таймаут
                    isRunning = false;
                    Process p = processRef.get();
                    if (p != null) {
                        p.destroy();
                    }
                } catch (InterruptedException e) {
                    // Игнорируем прерывание
                }
            });
            timeoutThread.start();

            String line;
            boolean dataStarted = false;
            int hopCount = 1;

            while ((line = reader.readLine()) != null && isRunning) {
                line = line.trim();
                
                if (line.isEmpty()) continue;
                
                // Пропускаем заголовки
                if (!dataStarted) {
                    if (isWindows && (line.contains("над максимальным") || line.contains("Трассировка"))) {
                        dataStarted = true;
                    } else if (!isWindows && line.contains("traceroute to")) {
                        dataStarted = true;
                    }
                    continue;
                }

                // Обработка строки с данными
                if (isWindows) {
                    if (line.matches("\\s*\\d+.*")) {
                        String[] parts = line.trim().split("\\s+");
                        String hop = String.valueOf(hopCount++);
                        String ip = "*";
                        String time = "*";
                        String hostname = "*";

                        // Ищем IP адрес или имя хоста
                        for (int i = parts.length - 1; i >= 0; i--) {
                            if (parts[i].contains(".") || parts[i].equals("*")) {
                                ip = parts[i];
                                hostname = parts[i];
                                break;
                            }
                        }

                        // Ищем время
                        for (int i = 0; i < parts.length; i++) {
                            if (parts[i].matches("\\d+") || parts[i].equals("<1")) {
                                time = parts[i] + " мс";
                                break;
                            }
                        }

                        dataList.add(new String[]{hop, ip, time, hostname});
                    }
                } else {
                    if (line.matches("\\s*\\d+.*")) {
                        String[] parts = line.trim().split("\\s+");
                        String hop = String.valueOf(hopCount++);
                        String ip = "*";
                        String time = "*";
                        String hostname = "*";

                        if (parts.length >= 2) {
                            if (!parts[1].equals("*")) {
                                ip = parts[1];
                                hostname = parts[1];
                                if (parts.length >= 4) {
                                    time = parts[2].replace("ms", "").trim() + " мс";
                                }
                            }
                        }

                        dataList.add(new String[]{hop, ip, time, hostname});
                    }
                }
            }

            // Отображаем финальную таблицу
            if (dataList.isEmpty()) {
                OutputFormatter.printBeautifulSection(doc, style, "Не удалось получить информацию о маршруте");
            } else {
                String[][] data = dataList.toArray(new String[0][]);
                OutputFormatter.printBeautifulTable(doc, style, headers, data);
            }

        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, "Ошибка при выполнении трассировки: " + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            isRunning = false;
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    // Игнорируем ошибки закрытия
                }
            }
            Process p = processRef.get();
            if (p != null) {
                p.destroy();
            }
        }
    }

    private void showUsage() {
        try {
            OutputFormatter.printBeautifulSection(doc, promptStyle, "ИСПОЛЬЗОВАНИЕ КОМАНДЫ TRACE");
            String[] headers = {"Опция", "Описание"};
            String[][] data = {
                {"trace <хост>", "Трассировка маршрута до указанного хоста"},
                {"trace -m <число>", "Максимальное количество прыжков (TTL)"},
                {"trace -w <мс>", "Таймаут ожидания для каждого ответа"},
                {"trace -d", "Не преобразовывать адреса в имена DNS"}
            };
            OutputFormatter.printBeautifulTable(doc, style, headers, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDescription() {
        return "трассировка маршрута до указанного узла";
    }
} 