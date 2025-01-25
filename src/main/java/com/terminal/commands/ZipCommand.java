package com.terminal.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class ZipCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;

    public ZipCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style);
        this.pathHolder = pathHolder;
    }

    @Override
    public void execute(String... args) {
        try {
            if (args.length < 2) {
                OutputFormatter.printBoxedHeader(doc, style, "Использование: zip <архив.zip> <файлы...>");
                OutputFormatter.printBoxedLine(doc, style, "Создает ZIP архив из указанных файлов");
                OutputFormatter.printBoxedLine(doc, style, "Примеры:");
                OutputFormatter.printBoxedLine(doc, style, "  zip archive.zip file1.txt file2.txt");
                OutputFormatter.printBoxedLine(doc, style, "  zip docs.zip *.txt");
                OutputFormatter.printBoxedFooter(doc, style);
                return;
            }

            String zipFileName = args[0];
            File zipFile = new File(pathHolder.getCurrentPath(), zipFileName);

            OutputFormatter.printBoxedHeader(doc, style, "Создание архива");
            OutputFormatter.printBoxedLine(doc, style, "Архив: " + zipFile.getName());
            OutputFormatter.printBoxedLine(doc, style, "");

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                for (int i = 1; i < args.length; i++) {
                    File fileToZip = new File(pathHolder.getCurrentPath(), args[i]);
                    addToZip(fileToZip, fileToZip.getName(), zos);
                }
            }

            OutputFormatter.printBoxedFooter(doc, style);

        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void addToZip(File file, String fileName, ZipOutputStream zos) throws Exception {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File child : files) {
                    addToZip(child, fileName + "/" + child.getName(), zos);
                }
            }
            return;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                zos.write(buffer, 0, length);
            }
            zos.closeEntry();

            OutputFormatter.printBoxedLine(doc, style, "Добавлен файл: " + fileName);
        }
    }

    @Override
    public String getDescription() {
        return "создать zip архив";
    }
} 