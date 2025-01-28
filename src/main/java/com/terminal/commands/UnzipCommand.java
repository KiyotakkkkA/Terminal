package com.terminal.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.core.CommandContext;
import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class UnzipCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;

    public UnzipCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style, pathHolder, "unzip", "Распаковка ZIP архивов", "ARCHIVE_OPERATIONS");
        this.pathHolder = pathHolder;
    }

    @Override
    public void execute(CommandContext context) {
        try {
            String[] args = context.getArgs();
            if (args.length < 1) {
                showUsage(context);
                return;
            }

            File zipFile = new File(pathHolder.getCurrentPath(), args[0]);
            if (!zipFile.exists()) {
                OutputFormatter.printError(context.getDoc(), context.getStyle(), "Файл не существует: " + args[0]);
                return;
            }

            String targetDir = args.length > 1 ? args[1] : zipFile.getName().replaceFirst("[.][^.]+$", "");
            File outputDir = new File(pathHolder.getCurrentPath(), targetDir);
            
            if (!outputDir.exists() && !outputDir.mkdirs()) {
                OutputFormatter.printError(context.getDoc(), context.getStyle(), "Не удалось создать директорию: " + targetDir);
                return;
            }

            OutputFormatter.printBeautifulSection(context.getDoc(), context.getStyle(), "Распаковка архива");
            OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(), 
                String.format("Архив: %s\nПапка: %s", zipFile.getCanonicalPath(), outputDir.getCanonicalPath()));

            List<ExtractedFile> extractedFiles = new ArrayList<>();
            long totalSize = 0;

            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
                ZipEntry entry;
                byte[] buffer = new byte[1024];

                while ((entry = zis.getNextEntry()) != null) {
                    File newFile = new File(outputDir, entry.getName());
                    String canonicalDestinationPath = newFile.getCanonicalPath();
                    
                    if (!canonicalDestinationPath.startsWith(outputDir.getCanonicalPath())) {
                        OutputFormatter.printError(context.getDoc(), context.getStyle(),
                            "Попытка записи за пределы целевой директории: " + entry.getName());
                        continue;
                    }

                    if (entry.isDirectory()) {
                        if (!newFile.mkdirs() && !newFile.isDirectory()) {
                            OutputFormatter.printError(context.getDoc(), context.getStyle(),
                                "Не удалось создать директорию: " + entry.getName());
                        }
                        continue;
                    }

                    File parent = newFile.getParentFile();
                    if (!parent.exists() && !parent.mkdirs()) {
                        OutputFormatter.printError(context.getDoc(), context.getStyle(),
                            "Не удалось создать директорию: " + parent.getPath());
                        continue;
                    }

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        long size = 0;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                            size += len;
                        }
                        extractedFiles.add(new ExtractedFile(entry.getName(), size));
                        totalSize += size;
                    }
                }
            }

            // Выводим статистику
            OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(), "\nСтатистика распаковки:");
            
            String[][] stats = {
                {"Всего файлов", String.valueOf(extractedFiles.size())},
                {"Общий размер", String.format("%,d байт", totalSize)},
                {"Папка назначения", outputDir.getCanonicalPath()}
            };
            
            OutputFormatter.printBeautifulTable(context.getDoc(), context.getStyle(),
                new String[]{"Параметр", "Значение"}, stats);

            // Выводим список распакованных файлов
            if (!extractedFiles.isEmpty()) {
                OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(), "\nРаспакованные файлы:");
                
                String[][] files = new String[extractedFiles.size()][2];
                for (int i = 0; i < extractedFiles.size(); i++) {
                    ExtractedFile ef = extractedFiles.get(i);
                    files[i][0] = ef.name;
                    files[i][1] = String.format("%,d байт", ef.size);
                }
                
                OutputFormatter.printBeautifulTable(context.getDoc(), context.getStyle(),
                    new String[]{"Имя файла", "Размер"}, files);
            }

        } catch (Exception e) {
            try {
                OutputFormatter.printError(context.getDoc(), context.getStyle(), "Ошибка при распаковке: " + e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при распаковке: " + e.getMessage());
            }
        }
    }

    private static class ExtractedFile {
        final String name;
        final long size;

        ExtractedFile(String name, long size) {
            this.name = name;
            this.size = size;
        }
    }

    private void showUsage(CommandContext context) throws Exception {
        OutputFormatter.printBeautifulSection(context.getDoc(), context.getStyle(), "Использование команды unzip");
        OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(),
            "unzip <архив.zip> [папка]\n\n" +
            "Распаковывает ZIP архив в указанную папку\n" +
            "Если папка не указана, создается папка с именем архива\n\n" +
            "Примеры:\n" +
            "  unzip archive.zip\n" +
            "  unzip docs.zip extracted/");
    }

    @Override
    public void executeCommand(String... args) throws Exception {
        CommandContext context = new CommandContext("", args, doc, style, pathHolder);
        execute(context);
    }

    @Override
    public String getDescription() {
        return "распаковка ZIP-архива";
    }
} 