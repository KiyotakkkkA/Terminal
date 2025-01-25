package com.terminal.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class FindCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;

    public FindCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
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

            String pattern = null;
            String type = null;
            Long minSize = null;
            Long maxSize = null;
            boolean hasOptions = false;
            
            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.startsWith("-")) {
                    hasOptions = true;
                    if (i + 1 >= args.length) {
                        OutputFormatter.printError(doc, style, "Отсутствует значение для опции " + arg);
                        return;
                    }
                    String value = args[++i];
                    
                    switch (arg) {
                        case "-name":
                            pattern = convertWildcardsToRegex(value);
                            break;
                        case "-type":
                            if (!value.equals("f") && !value.equals("d")) {
                                OutputFormatter.printError(doc, style, "Тип должен быть 'f' (файл) или 'd' (директория)");
                                return;
                            }
                            type = value;
                            break;
                        case "-size":
                            try {
                                if (value.startsWith("+")) {
                                    minSize = Long.parseLong(value.substring(1));
                                } else if (value.startsWith("-")) {
                                    maxSize = Long.parseLong(value.substring(1));
                                } else {
                                    minSize = maxSize = Long.parseLong(value);
                                }
                            } catch (NumberFormatException e) {
                                OutputFormatter.printError(doc, style, "Неверный формат размера. Используйте число, +число или -число");
                                return;
                            }
                            break;
                        default:
                            OutputFormatter.printError(doc, style, "Неизвестная опция " + arg);
                            return;
                    }
                }
            }
            
            if (pattern == null) {
                for (int i = 0; i < args.length; i++) {
                    String arg = args[i];
                    if (!arg.startsWith("-") && !args[Math.max(0, i-1)].startsWith("-")) {
                        pattern = convertWildcardsToRegex(arg);
                        break;
                    }
                }
            }

            if (pattern == null && (type != null || minSize != null || maxSize != null)) {
                pattern = ".*";
            } else if (pattern == null) {
                pattern = convertWildcardsToRegex("*");
            }

            OutputFormatter.printBoxedHeader(doc, style, "Результаты поиска");
            if (!pattern.equals(".*")) {
                OutputFormatter.printBoxedLine(doc, style, "Шаблон: " + pattern.replace(".*", "*").replace("^", "").replace("$", ""));
            }
            if (type != null) {
                OutputFormatter.printBoxedLine(doc, style, "Тип: " + (type.equals("d") ? "директории" : "файлы"));
            }
            if (minSize != null && maxSize != null && minSize.equals(maxSize)) {
                OutputFormatter.printBoxedLine(doc, style, "Размер: " + minSize + " байт");
            } else {
                if (minSize != null) {
                    OutputFormatter.printBoxedLine(doc, style, "Мин. размер: " + minSize + " байт");
                }
                if (maxSize != null) {
                    OutputFormatter.printBoxedLine(doc, style, "Макс. размер: " + maxSize + " байт");
                }
            }

            File startDir = new File(pathHolder.getCurrentPath());
            List<File> results = new ArrayList<>();
            findFiles(startDir, pattern, type, minSize, maxSize, results);

            if (results.isEmpty()) {
                OutputFormatter.printBoxedLine(doc, style, "Ничего не найдено");
            } else {
                for (File file : results) {
                    String relativePath = startDir.toPath().relativize(file.toPath()).toString();
                    OutputFormatter.printBoxedLine(doc, style, 
                        String.format("%s %-50s %8d байт",
                            file.isDirectory() ? "DIR " : "FILE",
                            relativePath,
                            file.length()));
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

    private void showHelp() {
        try {
            OutputFormatter.printBoxedHeader(doc, style, "Использование: find [опции] [шаблон]");
            OutputFormatter.printBoxedLine(doc, style, "Опции:");
            OutputFormatter.printBoxedLine(doc, style, "  -name <шаблон>  поиск по имени файла");
            OutputFormatter.printBoxedLine(doc, style, "  -type <f|d>     поиск по типу (f - файл, d - директория)");
            OutputFormatter.printBoxedLine(doc, style, "  -size <N>       поиск по размеру (+N, -N, N байт)");
            OutputFormatter.printBoxedLine(doc, style, "");
            OutputFormatter.printBoxedLine(doc, style, "Примеры:");
            OutputFormatter.printBoxedLine(doc, style, "  find *.txt                  найти все .txt файлы");
            OutputFormatter.printBoxedLine(doc, style, "  find -name *.java           то же с явным указанием");
            OutputFormatter.printBoxedLine(doc, style, "  find -type d src            найти все папки src");
            OutputFormatter.printBoxedLine(doc, style, "  find -size +1000            файлы больше 1000 байт");
            OutputFormatter.printBoxedLine(doc, style, "  find -size -500             файлы меньше 500 байт");
            OutputFormatter.printBoxedLine(doc, style, "  find -name *.txt -size +1000 .txt больше 1000 байт");
            OutputFormatter.printBoxedLine(doc, style, "");
            OutputFormatter.printBoxedLine(doc, style, "Шаблоны:");
            OutputFormatter.printBoxedLine(doc, style, "  *        любая последовательность символов");
            OutputFormatter.printBoxedLine(doc, style, "  ?        один любой символ");
            OutputFormatter.printBoxedLine(doc, style, "  [abc]    один из указанных символов");
            OutputFormatter.printBoxedLine(doc, style, "  [a-z]    один символ из диапазона");
            OutputFormatter.printBoxedFooter(doc, style);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findFiles(File dir, String pattern, String type, Long minSize, Long maxSize, List<File> results) {
        try {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (matchesPattern(file, pattern, type, minSize, maxSize)) {
                        results.add(file);
                    }
                    if (file.isDirectory()) {
                        findFiles(file, pattern, type, minSize, maxSize, results);
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    private boolean matchesPattern(File file, String pattern, String type, Long minSize, Long maxSize) {
        if (type != null) {
            boolean isDirectory = file.isDirectory();
            if (type.equals("f") && isDirectory) return false;
            if (type.equals("d") && !isDirectory) return false;
        }

        if (!file.isDirectory()) {
            long size = file.length();
            if (minSize != null && size < minSize) return false;
            if (maxSize != null && size > maxSize) return false;
        }

        if (pattern != null && !pattern.equals(".*")) {
            return file.getName().matches(pattern);
        }
        
        return true;
    }

    private String convertWildcardsToRegex(String pattern) {
        if (pattern == null || pattern.equals("*")) {
            return ".*";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("^");
        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            switch (c) {
                case '*':
                    sb.append(".*");
                    break;
                case '?':
                    sb.append(".");
                    break;
                case '.':
                case '[':
                case ']':
                case '(':
                case ')':
                case '{':
                case '}':
                case '\\':
                case '+':
                case '^':
                case '$':
                case '|':
                    sb.append("\\").append(c);
                    break;
                default:
                    sb.append(c);
            }
        }
        sb.append("$");
        return sb.toString();
    }

    @Override
    public String getDescription() {
        return "поиск файлов по имени, типу и размеру";
    }
} 