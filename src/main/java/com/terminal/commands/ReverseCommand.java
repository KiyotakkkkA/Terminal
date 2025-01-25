package com.terminal.commands;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class ReverseCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;
    private static final int MIN_STRING_LENGTH = 4;
    private static final int BYTES_PER_LINE = 16;

    public ReverseCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style);
        this.pathHolder = pathHolder;
    }

    @Override
    protected void initializeSubCommands() {
        addSubCommand("strings", "извлечь строки из файла");
        addSubCommand("header", "анализ заголовка файла");
        addSubCommand("hex", "шестнадцатеричный дамп");
        addSubCommand("disasm", "дизассемблировать");
    }

    @Override
    public void execute(String... args) {
        try {
            if (args.length < 2) {
                OutputFormatter.printBoxedHeader(doc, style, "Использование: reverse <операция> <файл>");
                OutputFormatter.printBoxedLine(doc, style, "Операции:");
                OutputFormatter.printBoxedLine(doc, style, "  strings     извлечь строки из файла");
                OutputFormatter.printBoxedLine(doc, style, "  header      анализ заголовка файла");
                OutputFormatter.printBoxedLine(doc, style, "  hex         шестнадцатеричный дамп");
                OutputFormatter.printBoxedLine(doc, style, "  disasm      дизассемблировать");
                OutputFormatter.printBoxedFooter(doc, style);
                return;
            }

            String operation = args[0].toLowerCase();
            String fileName = args[1];
            File file = new File(pathHolder.getCurrentPath(), fileName);

            if (!file.exists()) {
                OutputFormatter.printError(doc, style, "Файл не найден");
                return;
            }

            switch (operation) {
                case "strings":
                    extractStrings(file);
                    break;
                case "hex":
                    hexDump(file);
                    break;
                case "analyze":
                    analyzeFile(file);
                    break;
                case "header":
                    analyzeHeader(file);
                    break;
                case "disasm":
                    disassemble(file);
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

    private void extractStrings(File file) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Извлечение строк");
        OutputFormatter.printBoxedLine(doc, style, "Файл: " + file.getName());
        OutputFormatter.printBoxedLine(doc, style, "");
        
        byte[] fileContent = Files.readAllBytes(file.toPath());
        List<String> strings = new ArrayList<>();
        StringBuilder currentString = new StringBuilder();
        
        for (byte b : fileContent) {
            if (isPrintableChar(b)) {
                currentString.append((char) b);
            } else if (currentString.length() >= MIN_STRING_LENGTH) {
                strings.add(currentString.toString());
                currentString = new StringBuilder();
            } else {
                currentString = new StringBuilder();
            }
        }
        
        if (currentString.length() >= MIN_STRING_LENGTH) {
            strings.add(currentString.toString());
        }

        for (String str : strings) {
            OutputFormatter.printBoxedLine(doc, style, str);
        }
        
        OutputFormatter.printBoxedFooter(doc, style);
    }

    private void hexDump(File file) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Шестнадцатеричный дамп");
        OutputFormatter.printBoxedLine(doc, style, "Файл: " + file.getName());
        OutputFormatter.printBoxedLine(doc, style, "");
        
        byte[] fileContent = Files.readAllBytes(file.toPath());
        StringBuilder hex = new StringBuilder();
        StringBuilder ascii = new StringBuilder();
        
        for (int i = 0; i < fileContent.length; i++) {
            if (i % BYTES_PER_LINE == 0) {
                if (i > 0) {
                    OutputFormatter.printBoxedLine(doc, style, String.format("%s  |%s|", hex.toString(), ascii.toString()));
                    hex.setLength(0);
                    ascii.setLength(0);
                }
                OutputFormatter.printBoxedLine(doc, style, String.format("%08X  ", i));
            }
            
            hex.append(String.format("%02X ", fileContent[i]));
            ascii.append(isPrintableChar(fileContent[i]) ? 
                (char) fileContent[i] : '.');
        }
        
        if (hex.length() > 0) {
            while (hex.length() < BYTES_PER_LINE * 3) {
                hex.append("   ");
            }
            OutputFormatter.printBoxedLine(doc, style, String.format("%s  |%s|", hex.toString(), ascii.toString()));
        }
        
        OutputFormatter.printBoxedFooter(doc, style);
    }

    private void analyzeFile(File file) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Анализ файла " + file.getName());
        OutputFormatter.printBoxedLine(doc, style, "");
        
        OutputFormatter.printBoxedLine(doc, style, String.format("Размер: %d байт", file.length()));
        
        byte[] header = new byte[4];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(header);
        }
        
        String fileType = detectFileType(header);
        OutputFormatter.printBoxedLine(doc, style, "Тип файла: " + fileType);
        
        byte[] content = Files.readAllBytes(file.toPath());
        Map<Byte, Integer> byteStats = new HashMap<>();
        for (byte b : content) {
            byteStats.merge(b, 1, Integer::sum);
        }
        
        OutputFormatter.printBoxedLine(doc, style, "\nСтатистика байтов:");
        byteStats.entrySet().stream()
            .sorted(Map.Entry.<Byte, Integer>comparingByValue().reversed())
            .limit(10)
            .forEach(entry -> {
                try {
                    OutputFormatter.printBoxedLine(doc, style, String.format("0x%02X: %d раз", 
                        entry.getKey(), entry.getValue()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }

    private void analyzeHeader(File file) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Анализ заголовка");
        OutputFormatter.printBoxedLine(doc, style, "Файл: " + file.getName());
        OutputFormatter.printBoxedLine(doc, style, "");
        
        byte[] header = new byte[64];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(header);
        }
        
        OutputFormatter.printBoxedLine(doc, style, "Заголовок в HEX:");
        for (int i = 0; i < header.length; i++) {
            if (i % 16 == 0) {
                OutputFormatter.printBoxedLine(doc, style, String.format("\n%04X: ", i));
            }
            OutputFormatter.printBoxedLine(doc, style, String.format("%02X ", header[i]));
        }
        
        OutputFormatter.printBoxedLine(doc, style, "\n\nСигнатура: ");
        String signature = detectFileType(Arrays.copyOf(header, 4));
        OutputFormatter.printBoxedLine(doc, style, signature + "\n\n");
        
        OutputFormatter.printBoxedFooter(doc, style);
    }

    private void disassemble(File file) throws Exception {
        OutputFormatter.printBoxedHeader(doc, style, "Дизассемблирование");
        OutputFormatter.printBoxedLine(doc, style, "Файл: " + file.getName());
        OutputFormatter.printBoxedLine(doc, style, "");

        byte[] fileContent = Files.readAllBytes(file.toPath());
        
        if (fileContent.length < 4) {
            OutputFormatter.printBoxedLine(doc, style, "│ Ошибка: Файл слишком мал");
            return;
        }

        boolean isPE = fileContent[0] == 0x4D && fileContent[1] == 0x5A; // MZ
        boolean isELF = fileContent[0] == 0x7F && fileContent[1] == 0x45 && 
                       fileContent[2] == 0x4C && fileContent[3] == 0x46;

        if (!isPE && !isELF) {
            OutputFormatter.printBoxedLine(doc, style, "│ Предупреждение: Файл не является исполняемым (PE/ELF)");
            OutputFormatter.printBoxedLine(doc, style, "│ Попытка дизассемблировать как бинарный код...");
        }

        int entryPoint = findEntryPoint(fileContent);
        if (entryPoint == -1) {
            entryPoint = 0;
        }

        OutputFormatter.printBoxedLine(doc, style, String.format("│ Точка входа: 0x%08X", entryPoint));
        OutputFormatter.printBoxedLine(doc, style, "├───────────────────────────────────────────────────────┤");

        for (int i = entryPoint; i < Math.min(fileContent.length, entryPoint + 1000); i++) {
            try {
                Instruction instr = decodeInstruction(fileContent, i);
                if (instr != null) {
                    OutputFormatter.printBoxedLine(doc, style, String.format("│ %08X: %-20s %s", 
                        i, formatBytes(fileContent, i, instr.length), instr.mnemonic));
                    i += instr.length - 1;
                } else {
                    OutputFormatter.printBoxedLine(doc, style, String.format("│ %08X: %02X", i, fileContent[i]));
                }
            } catch (Exception e) {
                continue;
            }
        }

        OutputFormatter.printBoxedFooter(doc, style);
    }

    private int findEntryPoint(byte[] fileContent) {
        if (fileContent[0] == 0x4D && fileContent[1] == 0x5A) {
            try {
                int peOffset = readDword(fileContent, 0x3C);
                if (peOffset > 0 && peOffset < fileContent.length - 4) {
                    if (fileContent[peOffset] == 0x50 && fileContent[peOffset + 1] == 0x45) {
                        return readDword(fileContent, peOffset + 0x28);
                    }
                }
            } catch (Exception e) {
                return -1;
            }
        }
        else if (fileContent[0] == 0x7F && fileContent[1] == 0x45 && 
                fileContent[2] == 0x4C && fileContent[3] == 0x46) {
            try {
                return readDword(fileContent, 0x18);
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }

    private int readDword(byte[] data, int offset) {
        return ((data[offset + 3] & 0xFF) << 24) |
               ((data[offset + 2] & 0xFF) << 16) |
               ((data[offset + 1] & 0xFF) << 8) |
               (data[offset] & 0xFF);
    }

    private String formatBytes(byte[] data, int offset, int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length && offset + i < data.length; i++) {
            sb.append(String.format("%02X ", data[offset + i]));
        }
        return sb.toString();
    }

    private static class Instruction {
        int length;
        String mnemonic;

        Instruction(int length, String mnemonic) {
            this.length = length;
            this.mnemonic = mnemonic;
        }
    }

    private Instruction decodeInstruction(byte[] data, int offset) {
        if (offset >= data.length) return null;

        byte opcode = data[offset];
        switch (opcode & 0xFF) {
            case 0x00: return new Instruction(2, "ADD BYTE PTR [" + getModRM(data, offset + 1) + "], AL");
            case 0x01: return new Instruction(2, "ADD DWORD PTR [" + getModRM(data, offset + 1) + "], EAX");
            case 0x03: return new Instruction(2, "ADD EAX, [" + getModRM(data, offset + 1) + "]");
            case 0x05: return new Instruction(5, "ADD EAX, " + readDword(data, offset + 1));
            case 0x50: return new Instruction(1, "PUSH EAX");
            case 0x51: return new Instruction(1, "PUSH ECX");
            case 0x52: return new Instruction(1, "PUSH EDX");
            case 0x53: return new Instruction(1, "PUSH EBX");
            case 0x54: return new Instruction(1, "PUSH ESP");
            case 0x55: return new Instruction(1, "PUSH EBP");
            case 0x56: return new Instruction(1, "PUSH ESI");
            case 0x57: return new Instruction(1, "PUSH EDI");
            case 0x58: return new Instruction(1, "POP EAX");
            case 0x59: return new Instruction(1, "POP ECX");
            case 0x5A: return new Instruction(1, "POP EDX");
            case 0x5B: return new Instruction(1, "POP EBX");
            case 0x5C: return new Instruction(1, "POP ESP");
            case 0x5D: return new Instruction(1, "POP EBP");
            case 0x5E: return new Instruction(1, "POP ESI");
            case 0x5F: return new Instruction(1, "POP EDI");
            case 0x68: return new Instruction(5, "PUSH " + readDword(data, offset + 1));
            case 0x6A: return new Instruction(2, "PUSH " + data[offset + 1]);
            case 0x74: return new Instruction(2, "JE " + (offset + 2 + (byte)data[offset + 1]));
            case 0x75: return new Instruction(2, "JNE " + (offset + 2 + (byte)data[offset + 1]));
            case 0x89: return new Instruction(2, "MOV [" + getModRM(data, offset + 1) + "], EAX");
            case 0x8B: return new Instruction(2, "MOV EAX, [" + getModRM(data, offset + 1) + "]");
            case 0x90: return new Instruction(1, "NOP");
            case 0xC3: return new Instruction(1, "RET");
            case 0xE8: return new Instruction(5, "CALL " + (offset + 5 + readDword(data, offset + 1)));
            case 0xE9: return new Instruction(5, "JMP " + (offset + 5 + readDword(data, offset + 1)));
            case 0xEB: return new Instruction(2, "JMP SHORT " + (offset + 2 + (byte)data[offset + 1]));
            case 0xFF: {
                if (offset + 1 < data.length) {
                    byte modrm = data[offset + 1];
                    switch ((modrm >> 3) & 7) {
                        case 2: return new Instruction(2, "CALL [" + getModRM(data, offset + 1) + "]");
                        case 4: return new Instruction(2, "JMP [" + getModRM(data, offset + 1) + "]");
                    }
                }
            }
        }
        return null;
    }

    private String getModRM(byte[] data, int offset) {
        if (offset >= data.length) return "???";
        
        byte modrm = data[offset];
        int mod = (modrm >> 6) & 3;
        int reg = (modrm >> 3) & 7;
        int rm = modrm & 7;
        
        String[] regs = {"EAX", "ECX", "EDX", "EBX", "ESP", "EBP", "ESI", "EDI"};
        
        switch (mod) {
            case 0:
                if (rm == 5) return String.format("[0x%X]", readDword(data, offset + 1));
                return "[" + regs[rm] + "]";
            case 1:
                return String.format("[%s + %d]", regs[rm], (byte)data[offset + 1]);
            case 2:
                return String.format("[%s + 0x%X]", regs[rm], readDword(data, offset + 1));
            case 3:
                return regs[rm];
        }
        return "???";
    }

    private boolean isPrintableChar(byte b) {
        return b >= 32 && b < 127;
    }

    private String detectFileType(byte[] header) {
        if (header.length < 4) return "Неизвестный";
        
        if (header[0] == 0x4D && header[1] == 0x5A) // MZ
            return "Исполняемый файл (EXE/DLL)";
        if (header[0] == 0x7F && header[1] == 0x45 && // ELF
            header[2] == 0x4C && header[3] == 0x46)
            return "Исполняемый файл Linux (ELF)";
        if (header[0] == (byte)0xFF && header[1] == (byte)0xD8)
            return "Изображение JPEG";
        if (header[0] == (byte)0x89 && header[1] == 0x50)
            return "Изображение PNG";
        if (header[0] == 0x50 && header[1] == 0x4B)
            return "Архив ZIP";
        if (header[0] == 0x7F && header[1] == 0x45)
            return "Файл ELF";
        
        return "Неизвестный формат";
    }

    @Override
    public String getDescription() {
        return "инструменты для реверс-инжиниринга";
    }
} 