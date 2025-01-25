package com.terminal.commands;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class HashCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;

    public HashCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style);
        this.pathHolder = pathHolder;
    }

    @Override
    protected void initializeSubCommands() {
        addSubCommand("md5", "MD5 хеш");
        addSubCommand("sha1", "SHA-1 хеш");
        addSubCommand("sha256", "SHA-256 хеш");
        addSubCommand("sha512", "SHA-512 хеш");
    }

    @Override
    public void execute(String... args) {
        try {
            if (args.length < 2) {
                OutputFormatter.printBoxedHeader(doc, style, "Использование: hash <алгоритм> <файл>");
                OutputFormatter.printBoxedLine(doc, style, "Алгоритмы:");
                OutputFormatter.printBoxedLine(doc, style, "  md5         MD5 хеш");
                OutputFormatter.printBoxedLine(doc, style, "  sha1        SHA-1 хеш");
                OutputFormatter.printBoxedLine(doc, style, "  sha256      SHA-256 хеш");
                OutputFormatter.printBoxedLine(doc, style, "  sha512      SHA-512 хеш");
                OutputFormatter.printBoxedFooter(doc, style);
                return;
            }

            String algorithm = args[0].toLowerCase();
            String input = args[1];

            OutputFormatter.printBoxedHeader(doc, style, "Вычисление хеша");
            OutputFormatter.printBoxedLine(doc, style, "Алгоритм: " + algorithm.toUpperCase());
            OutputFormatter.printBoxedLine(doc, style, "Входные данные: " + input);
            OutputFormatter.printBoxedLine(doc, style, "");
            OutputFormatter.printBoxedLine(doc, style, "Результат: " + calculateHash(algorithm, input));
            OutputFormatter.printBoxedFooter(doc, style);

        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private String calculateHash(String algorithm, String input) throws IOException {
        String mdAlgorithm;
        switch (algorithm) {
            case "md5":
                mdAlgorithm = "MD5";
                break;
            case "sha1":
                mdAlgorithm = "SHA-1";
                break;
            case "sha256":
                mdAlgorithm = "SHA-256";
                break;
            case "sha512":
                mdAlgorithm = "SHA-512";
                break;
            default:
                throw new IllegalArgumentException("Неподдерживаемый алгоритм: " + algorithm);
        }

        try (FileInputStream fis = new FileInputStream(input)) {
            MessageDigest digest = MessageDigest.getInstance(mdAlgorithm);
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
            byte[] hashBytes = digest.digest();
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("Ошибка при инициализации алгоритма: " + e.getMessage());
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    @Override
    public String getDescription() {
        return "вычисление хешей файлов";
    }
} 