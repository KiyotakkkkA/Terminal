package com.terminal.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.core.CommandContext;
import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class ZipCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;

    public ZipCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style, pathHolder, "zip", "Создание ZIP архивов", "ARCHIVE_OPERATIONS");
        this.pathHolder = pathHolder;
    }

    @Override
    public void execute(CommandContext context) {
        try {
            String[] args = context.getArgs();
            if (args.length < 2) {
                showUsage(context);
                return;
            }

            String zipFileName = args[0];
            if (!zipFileName.toLowerCase().endsWith(".zip")) {
                zipFileName += ".zip";
            }

            File zipFile = new File(pathHolder.getCurrentPath(), zipFileName);
            OutputFormatter.printBeautifulSection(context.getDoc(), context.getStyle(), "Создание ZIP архива");
            OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(), "Архив: " + zipFile.getCanonicalPath());

            try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
                for (int i = 1; i < args.length; i++) {
                    File fileToZip = new File(pathHolder.getCurrentPath(), args[i]);
                    if (!fileToZip.exists()) {
                        OutputFormatter.printError(context.getDoc(), context.getStyle(), "Файл не существует: " + args[i]);
                        continue;
                    }

                    addToZip(context, zos, fileToZip, "");
                }
            }

            OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(), "Архив успешно создан");
            
            // Выводим статистику
            String[][] stats = {
                {"Размер архива", String.format("%,d байт", zipFile.length())},
                {"Количество файлов", String.valueOf(args.length - 1)},
                {"Расположение", zipFile.getCanonicalPath()}
            };
            
            OutputFormatter.printBeautifulTable(context.getDoc(), context.getStyle(),
                new String[]{"Параметр", "Значение"}, stats);

        } catch (Exception e) {
            try {
                OutputFormatter.printError(context.getDoc(), context.getStyle(), "Ошибка при создании архива: " + e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при создании архива: " + e.getMessage());
            }
        }
    }

    private void addToZip(CommandContext context, ZipOutputStream zos, File fileToZip, String parentPath) throws Exception {
        String entryPath = parentPath + fileToZip.getName();
        
        if (fileToZip.isDirectory()) {
            File[] children = fileToZip.listFiles();
            if (children != null) {
                for (File child : children) {
                    addToZip(context, zos, child, entryPath + "/");
                }
            }
            return;
        }

        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(entryPath);
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }

            zos.closeEntry();
            OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(),
                String.format("Добавлен файл: %s (%,d байт)", entryPath, fileToZip.length()));
        }
    }

    private void showUsage(CommandContext context) throws Exception {
        OutputFormatter.printBeautifulSection(context.getDoc(), context.getStyle(), "Использование команды zip");
        OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(),
            "zip <архив.zip> <файлы...>\n\n" +
            "Создает ZIP архив из указанных файлов\n\n" +
            "Примеры:\n" +
            "  zip archive.zip file1.txt file2.txt\n" +
            "  zip docs.zip *.doc *.pdf\n" +
            "  zip backup.zip folder/");
    }

    @Override
    public void executeCommand(String... args) throws Exception {
        CommandContext context = new CommandContext("", args, doc, style, pathHolder);
        execute(context);
    }

    @Override
    public String getDescription() {
        return "создать zip архив";
    }
} 