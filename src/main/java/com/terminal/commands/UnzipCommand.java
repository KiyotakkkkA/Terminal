package com.terminal.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class UnzipCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;

    public UnzipCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style);
        this.pathHolder = pathHolder;
    }

    @Override
    public void execute(String... args) {
        try {
            if (args.length < 1) {
                showUsage();
                return;
            }

            File zipFile = new File(pathHolder.getCurrentPath(), args[0]);
            if (!zipFile.exists()) {
                OutputFormatter.printError(doc, style, "Файл не существует: " + args[0]);
                return;
            }

            String outputDir = args.length > 1 ? args[1] : zipFile.getName().replace(".zip", "");
            File outputPath = new File(pathHolder.getCurrentPath(), outputDir);
            if (!outputPath.exists()) {
                outputPath.mkdirs();
            }

            OutputFormatter.printBoxedHeader(doc, style, "Распаковка архива");
            OutputFormatter.printBoxedLine(doc, style, "Архив: " + zipFile.getName());
            OutputFormatter.printBoxedLine(doc, style, "Путь: " + outputPath.getCanonicalPath());

            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    File newFile = new File(outputPath, entry.getName());
                    if (entry.isDirectory()) {
                        newFile.mkdirs();
                        continue;
                    }
                    
                    newFile.getParentFile().mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    OutputFormatter.printBoxedLine(doc, style, "Распаковано: " + entry.getName());
                }
            }

            OutputFormatter.printBoxedFooter(doc, style);

        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при распаковке: " + e.getMessage());
            }
        }
    }

    private void showUsage() throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Использование: unzip <архив.zip> [папка]");
        OutputFormatter.printBoxedLine(doc, style, "Распаковывает ZIP-архив в указанную папку");
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Примеры:");
        OutputFormatter.printBoxedLine(doc, style, "  unzip archive.zip           - распаковать в папку archive");
        OutputFormatter.printBoxedLine(doc, style, "  unzip data.zip output      - распаковать в папку output");
        OutputFormatter.printBoxedFooter(doc, style);
    }

    @Override
    public String getDescription() {
        return "распаковка ZIP-архива";
    }
} 