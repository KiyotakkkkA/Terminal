package com.terminal.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class GrepCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;
    private static final Charset[] CHARSETS = {
        StandardCharsets.UTF_8,
        StandardCharsets.UTF_16,
        StandardCharsets.UTF_16BE,
        StandardCharsets.UTF_16LE,
        Charset.forName("windows-1251"),
        StandardCharsets.ISO_8859_1
    };

    public GrepCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style);
        this.pathHolder = pathHolder;
    }

    @Override
    public void execute(String... args) {
        try {
            if (args.length < 1) {
                showHelp();
                return;
            }

            boolean ignoreCase = false;
            boolean showLineNumbers = false;
            boolean invertMatch = false;
            String pattern = null;
            List<String> files = new ArrayList<>();

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.startsWith("-")) {
                    for (char flag : arg.substring(1).toCharArray()) {
                        switch (flag) {
                            case 'i':
                                ignoreCase = true;
                                break;
                            case 'n':
                                showLineNumbers = true;
                                break;
                            case 'v':
                                invertMatch = true;
                                break;
                            default:
                                OutputFormatter.printBoxedLine(doc, style, 
                                    "Предупреждение: Неизвестный флаг '" + flag + "' игнорируется");
                        }
                    }
                } else {
                    if (pattern == null) {
                        pattern = arg;
                    } else {
                        files.add(arg);
                    }
                }
            }

            if (pattern == null || files.isEmpty()) {
                showHelp();
                return;
            }

            Pattern regex;
            try {
                regex = Pattern.compile(pattern, ignoreCase ? Pattern.CASE_INSENSITIVE : 0);
            } catch (Exception e) {
                OutputFormatter.printBoxedHeader(doc, style, "Ошибка");
                OutputFormatter.printBoxedLine(doc, style, "Некорректное регулярное выражение: " + e.getMessage());
                OutputFormatter.printBoxedFooter(doc, style);
                return;
            }

            OutputFormatter.printBoxedHeader(doc, style, "Поиск в файлах");
            OutputFormatter.printBoxedLine(doc, style, "Шаблон: " + pattern);
            if (ignoreCase) {
                OutputFormatter.printBoxedLine(doc, style, "Игнорировать регистр: да");
            }
            if (showLineNumbers) {
                OutputFormatter.printBoxedLine(doc, style, "Показывать номера строк: да");
            }
            if (invertMatch) {
                OutputFormatter.printBoxedLine(doc, style, "Инвертировать поиск: да");
            }

            for (String fileName : files) {
                File file = new File(pathHolder.getCurrentPath(), fileName);
                if (!file.exists()) {
                    OutputFormatter.printError(doc, style, "Файл не существует: " + fileName);
                    continue;
                }

                if (files.size() > 1) {
                    OutputFormatter.printBoxedLine(doc, style, "Файл: " + fileName);
                }

                boolean fileProcessed = false;
                for (Charset charset : CHARSETS) {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(new FileInputStream(file), charset))) {
                        String line;
                        int lineNum = 0;
                        boolean hasValidContent = false;

                        for (int i = 0; i < 5 && (line = reader.readLine()) != null; i++) {
                            if (isValidString(line)) {
                                hasValidContent = true;
                                break;
                            }
                        }

                        if (!hasValidContent) {
                            continue;
                        }

                        reader.close();
                        try (BufferedReader validReader = new BufferedReader(
                                new InputStreamReader(new FileInputStream(file), charset))) {
                            while ((line = validReader.readLine()) != null) {
                                lineNum++;
                                Matcher matcher = regex.matcher(line);
                                boolean matches = matcher.find();
                                
                                if (matches != invertMatch) {
                                    if (showLineNumbers) {
                                        OutputFormatter.printBoxedLine(doc, style, 
                                            String.format("%4d: %s", lineNum, line));
                                    } else {
                                        OutputFormatter.printBoxedLine(doc, style, line);
                                    }
                                }
                            }
                            fileProcessed = true;
                            break;
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }

                if (!fileProcessed) {
                    OutputFormatter.printError(doc, style, 
                        "Не удалось определить кодировку файла: " + fileName);
                }
            }

            OutputFormatter.printBoxedFooter(doc, style);

        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, "Ошибка при выполнении команды: " + e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при выполнении команды: " + e.getMessage());
            }
        }
    }

    private boolean isValidString(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (c < 32 && c != '\t' && c != '\n' && c != '\r') {
                return false;
            }
        }
        return true;
    }

    private void showHelp() {
        try {
            OutputFormatter.printBoxedHeader(doc, style, "Использование: grep [опции] <шаблон> <файл...>");
            OutputFormatter.printBoxedLine(doc, style, "Опции:");
            OutputFormatter.printBoxedLine(doc, style, "  -i    игнорировать регистр");
            OutputFormatter.printBoxedLine(doc, style, "  -n    показывать номера строк");
            OutputFormatter.printBoxedLine(doc, style, "  -v    показывать строки НЕ содержащие шаблон");
            OutputFormatter.printBoxedLine(doc, style, "");
            OutputFormatter.printBoxedLine(doc, style, "Примеры:");
            OutputFormatter.printBoxedLine(doc, style, "  grep error log.txt");
            OutputFormatter.printBoxedLine(doc, style, "  grep -i -n error *.log");
            OutputFormatter.printBoxedLine(doc, style, "  grep -v test file1.txt file2.txt");
            OutputFormatter.printBoxedFooter(doc, style);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDescription() {
        return "поиск текста в файлах";
    }
} 