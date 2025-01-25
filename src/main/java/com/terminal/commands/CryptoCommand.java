package com.terminal.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import com.terminal.sdk.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class CryptoCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int KEY_SIZE = 256;
    private static final int FALLBACK_KEY_SIZE = 128;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public CryptoCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style);
        this.pathHolder = pathHolder;

        try {
            int maxKeySize = Cipher.getMaxAllowedKeyLength("AES");
            if (maxKeySize < KEY_SIZE) {
                OutputFormatter.printBoxedHeader(doc, style, "Предупреждение");
                OutputFormatter.printBoxedLine(doc, style, "Ограничение размера ключа в вашей системе");
                OutputFormatter.printBoxedLine(doc, style, "Для использования AES-256 установите JCE Unlimited Strength");
                OutputFormatter.printBoxedLine(doc, style, "Будет использован AES-128");
                OutputFormatter.printBoxedFooter(doc, style);
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected void initializeSubCommands() {
        addSubCommand("genkey", "сгенерировать ключ");
        addSubCommand("encrypt", "зашифровать файл");
        addSubCommand("decrypt", "расшифровать файл");
    }

    @Override
    public void execute(String... args) {
        try {
            if (args.length < 1) {
                OutputFormatter.printBoxedHeader(doc, style, "Использование: crypto <операция> [параметры]");
                OutputFormatter.printBoxedLine(doc, style, "Операции:");
                OutputFormatter.printBoxedLine(doc, style, "  genkey           сгенерировать ключ");
                OutputFormatter.printBoxedLine(doc, style, "  encrypt <файл>   зашифровать файл (требуется ключ)");
                OutputFormatter.printBoxedLine(doc, style, "  decrypt <файл>   расшифровать файл (требуется ключ)");
                OutputFormatter.printBoxedLine(doc, style, "");
                OutputFormatter.printBoxedLine(doc, style, "Примеры:");
                OutputFormatter.printBoxedLine(doc, style, "  1. Сгенерировать ключ:");
                OutputFormatter.printBoxedLine(doc, style, "     crypto genkey");
                OutputFormatter.printBoxedLine(doc, style, "");
                OutputFormatter.printBoxedLine(doc, style, "  2. Зашифровать файл:");
                OutputFormatter.printBoxedLine(doc, style, "     crypto encrypt secret.txt <ключ>");
                OutputFormatter.printBoxedLine(doc, style, "     (создаст файл secret.txt.encrypted)");
                OutputFormatter.printBoxedLine(doc, style, "");
                OutputFormatter.printBoxedLine(doc, style, "  3. Расшифровать файл:");
                OutputFormatter.printBoxedLine(doc, style, "     crypto decrypt secret.txt.encrypted <ключ>");
                OutputFormatter.printBoxedLine(doc, style, "     (создаст файл secret.txt.decrypted)");
                OutputFormatter.printBoxedFooter(doc, style);
                return;
            }

            String operation = args[0].toLowerCase();
            switch (operation) {
                case "encrypt":
                    if (args.length < 2) {
                        OutputFormatter.printError(doc, style, "Укажите файл для шифрования");
                        return;
                    }
                    if (args.length < 3) {
                        OutputFormatter.printError(doc, style, "Требуется ключ для шифрования");
                        return;
                    }
                    File inputFileEncrypt = new File(pathHolder.getCurrentPath(), args[1]);
                    encryptFile(inputFileEncrypt, args[2]);
                    break;
                    
                case "decrypt":
                    if (args.length < 2) {
                        OutputFormatter.printError(doc, style, "Укажите файл для расшифровки");
                        return;
                    }
                    if (args.length < 3) {
                        OutputFormatter.printError(doc, style, "Требуется ключ для расшифровки");
                        return;
                    }
                    File inputFileDecrypt = new File(pathHolder.getCurrentPath(), args[1]);
                    decryptFile(inputFileDecrypt, args[2]);
                    break;
                    
                case "genkey":
                    generateKey();
                    break;
                    
                default:
                    OutputFormatter.printError(doc, style, "Неизвестная операция");
            }
        } catch (Exception e) {
            try {
                OutputFormatter.printError(doc, style, e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void generateKey() throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Генерация ключа");
        
        int actualKeySize;
        try {
            int maxKeySize = Cipher.getMaxAllowedKeyLength("AES");
            actualKeySize = maxKeySize < KEY_SIZE ? FALLBACK_KEY_SIZE : KEY_SIZE;
        } catch (Exception e) {
            actualKeySize = FALLBACK_KEY_SIZE;
        }

        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(actualKeySize);
        SecretKey key = keyGen.generateKey();
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        
        OutputFormatter.printBoxedLine(doc, style, "Алгоритм: AES-" + actualKeySize);
        OutputFormatter.printBoxedLine(doc, style, "Ключ: " + encodedKey);
        OutputFormatter.printBoxedLine(doc, style, "");
        OutputFormatter.printBoxedLine(doc, style, "Важно: Сохраните этот ключ! Он потребуется для расшифровки");
        OutputFormatter.printBoxedFooter(doc, style);
    }

    private void encryptFile(File inputFile, String key) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Шифрование файла");
        OutputFormatter.printBoxedLine(doc, style, "Файл: " + inputFile.getName());
        
        if (!inputFile.exists()) {
            OutputFormatter.printError(doc, style, "Файл не найден");
            return;
        }

        if (inputFile.length() == 0) {
            OutputFormatter.printError(doc, style, "Файл пуст");
            return;
        }

        File outputFile = new File(inputFile.getParent(), inputFile.getName() + ".encrypted");
        
        try {
            byte[] decodedKey = Base64.getDecoder().decode(key);
            if (decodedKey.length * 8 > Cipher.getMaxAllowedKeyLength("AES")) {
                OutputFormatter.printError(doc, style, "Размер ключа не поддерживается вашей системой");
                OutputFormatter.printBoxedLine(doc, style, "Для использования AES-256 установите JCE Unlimited Strength");
                OutputFormatter.printBoxedLine(doc, style, "Сгенерируйте новый ключ командой 'crypto genkey'");
                return;
            }
            
            SecretKey secretKey = new SecretKeySpec(decodedKey, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            byte[] iv = generateIV();
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

            try (FileInputStream fis = new FileInputStream(inputFile);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {
                
                OutputFormatter.printBoxedLine(doc, style, "Статус: Начало шифрования...");
                
                fos.write(iv);
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytesRead = 0;
                long fileSize = inputFile.length();
                
                while ((bytesRead = fis.read(buffer)) != -1) {
                    byte[] output = cipher.update(buffer, 0, bytesRead);
                    if (output != null) {
                        fos.write(output);
                    }
                    totalBytesRead += bytesRead;
                    int progress = (int)((totalBytesRead * 100) / fileSize);
                    OutputFormatter.printBoxedLine(doc, style, String.format("Прогресс: %d%%", progress));
                }
                
                byte[] outputBytes = cipher.doFinal();
                if (outputBytes != null) {
                    fos.write(outputBytes);
                }
                
                OutputFormatter.printBoxedLine(doc, style, "");
                OutputFormatter.printBoxedLine(doc, style, "Статус: Шифрование завершено");
                OutputFormatter.printBoxedLine(doc, style, "Зашифрованный файл: " + outputFile.getName());
                OutputFormatter.printBoxedFooter(doc, style);
            }
        } catch (IllegalArgumentException e) {
            OutputFormatter.printError(doc, style, "Неверный формат ключа");
            OutputFormatter.printBoxedLine(doc, style, "Убедитесь, что ключ в формате Base64");
            OutputFormatter.printBoxedLine(doc, style, "Используйте команду 'crypto genkey' для генерации ключа");
        } catch (Exception e) {
            OutputFormatter.printError(doc, style, "Ошибка при шифровании: " + e.getMessage());
            if (outputFile.exists()) {
                outputFile.delete();
            }
        }
    }

    private void decryptFile(File inputFile, String key) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Расшифровка файла");
        OutputFormatter.printBoxedLine(doc, style, "Файл: " + inputFile.getName());
        
        if (!inputFile.exists()) {
            OutputFormatter.printError(doc, style, "Файл не найден");
            return;
        }

        if (inputFile.length() == 0) {
            OutputFormatter.printError(doc, style, "Файл пуст");
            return;
        }

        String outputFileName = inputFile.getName().endsWith(".encrypted") ?
            inputFile.getName().substring(0, inputFile.getName().length() - ".encrypted".length()) :
            inputFile.getName() + ".decrypted";
            
        File outputFile = new File(inputFile.getParent(), outputFileName);
        
        try {
            byte[] decodedKey = Base64.getDecoder().decode(key);
            SecretKey secretKey = new SecretKeySpec(decodedKey, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);

            try (FileInputStream fis = new FileInputStream(inputFile);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {
                
                OutputFormatter.printBoxedLine(doc, style, "Статус: Начало расшифровки...");
                
                byte[] iv = new byte[16];
                fis.read(iv);
                cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytesRead = 0;
                long fileSize = inputFile.length() - 16; // Subtract IV size
                
                while ((bytesRead = fis.read(buffer)) != -1) {
                    byte[] output = cipher.update(buffer, 0, bytesRead);
                    if (output != null) {
                        fos.write(output);
                    }
                    totalBytesRead += bytesRead;
                    int progress = (int)((totalBytesRead * 100) / fileSize);
                    OutputFormatter.printBoxedLine(doc, style, String.format("Прогресс: %d%%", progress));
                }
                
                byte[] outputBytes = cipher.doFinal();
                if (outputBytes != null) {
                    fos.write(outputBytes);
                }
                
                OutputFormatter.printBoxedLine(doc, style, "");
                OutputFormatter.printBoxedLine(doc, style, "Статус: Расшифровка завершена");
                OutputFormatter.printBoxedLine(doc, style, "Расшифрованный файл: " + outputFile.getName());
                OutputFormatter.printBoxedFooter(doc, style);
            }
        } catch (IllegalArgumentException e) {
            OutputFormatter.printError(doc, style, "Неверный формат ключа");
            OutputFormatter.printBoxedLine(doc, style, "Убедитесь, что ключ в формате Base64");
            OutputFormatter.printBoxedLine(doc, style, "Используйте команду 'crypto genkey' для генерации ключа");
            if (outputFile.exists()) {
                outputFile.delete();
            }
        } catch (Exception e) {
            OutputFormatter.printError(doc, style, "Ошибка при расшифровке: " + e.getMessage());
            if (outputFile.exists()) {
                outputFile.delete();
            }
        }
    }

    private byte[] generateIV() {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16];
        random.nextBytes(iv);
        return iv;
    }

    @Override
    public String getDescription() {
        return "инструменты для работы с криптографией";
    }
} 