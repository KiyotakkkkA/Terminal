package com.terminal.commands;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import com.terminal.sdk.core.CommandContext;
import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.OutputFormatter;

public class ReverseCommand extends AbstractCommand {
    private final CurrentPathHolder pathHolder;
    private static final int MIN_STRING_LENGTH = 4;
    private static final int BYTES_PER_LINE = 16;

    public ReverseCommand(StyledDocument doc, Style style, CurrentPathHolder pathHolder) {
        super(doc, style, pathHolder, "reverse", "Реверс-инжиниринг файлов", "SEARCH_AND_PROCESS");
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

            String subCommand = args[0];
            String fileName = args[1];
            File file = new File(pathHolder.getCurrentPath(), fileName);

            if (!file.exists()) {
                OutputFormatter.printError(context.getDoc(), context.getStyle(), "Файл не существует: " + fileName);
                return;
            }

            switch (subCommand) {
                case "strings":
                    extractStrings(context, file);
                    break;
                case "header":
                    analyzeHeader(context, file);
                    break;
                case "hex":
                    hexDump(context, file);
                    break;
                case "disasm":
                    disassemble(context, file);
                    break;
                default:
                    OutputFormatter.printError(context.getDoc(), context.getStyle(), "Неизвестная подкоманда: " + subCommand);
                    showUsage(context);
            }
        } catch (Exception e) {
            try {
                OutputFormatter.printError(context.getDoc(), context.getStyle(), "Ошибка: " + e.getMessage());
            } catch (Exception ex) {
                System.err.println("Ошибка при выполнении команды: " + e.getMessage());
            }
        }
    }

    private void extractStrings(CommandContext context, File file) throws Exception {
        OutputFormatter.printBeautifulSection(context.getDoc(), context.getStyle(), "Извлечение строк из " + file.getName());
        
        byte[] bytes = Files.readAllBytes(file.toPath());
        List<String> strings = new ArrayList<>();
        StringBuilder currentString = new StringBuilder();
        
        for (byte b : bytes) {
            if (isPrintable((char) b)) {
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

        if (!strings.isEmpty()) {
            String[][] data = new String[strings.size()][2];
            for (int i = 0; i < strings.size(); i++) {
                data[i][0] = String.valueOf(i + 1);
                data[i][1] = strings.get(i);
            }
            
            OutputFormatter.printBeautifulTable(context.getDoc(), context.getStyle(),
                new String[]{"#", "Строка"}, data);
        } else {
            OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(), "Строки не найдены");
        }
    }

    private void analyzeHeader(CommandContext context, File file) throws Exception {
        OutputFormatter.printBeautifulSection(context.getDoc(), context.getStyle(), "Анализ заголовка " + file.getName());
        
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] header = new byte[32];
            int read = fis.read(header);
            
            String[][] data = new String[3][2];
            data[0] = new String[]{"Размер файла", String.format("%,d байт", file.length())};
            data[1] = new String[]{"Тип файла", detectFileType(header)};
            data[2] = new String[]{"Сигнатура", bytesToHex(Arrays.copyOf(header, Math.min(read, 8)))};
            
            OutputFormatter.printBeautifulTable(context.getDoc(), context.getStyle(),
                new String[]{"Параметр", "Значение"}, data);
        }
    }

    private void hexDump(CommandContext context, File file) throws Exception {
        OutputFormatter.printBeautifulSection(context.getDoc(), context.getStyle(), "Шестнадцатеричный дамп " + file.getName());
        
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[BYTES_PER_LINE];
            int offset = 0;
            int read;
            
            while ((read = fis.read(buffer)) != -1) {
                StringBuilder hex = new StringBuilder();
                StringBuilder ascii = new StringBuilder();
                
                for (int i = 0; i < BYTES_PER_LINE; i++) {
                    if (i < read) {
                        hex.append(String.format("%02X ", buffer[i]));
                        ascii.append(isPrintable((char) buffer[i]) ? (char) buffer[i] : '.');
                    } else {
                        hex.append("   ");
                        ascii.append(" ");
                    }
                }
                
                OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(),
                    String.format("%08X  %-48s  |%s|", offset, hex, ascii));
                
                offset += read;
                if (offset >= 256) break; // Ограничиваем вывод
            }
        }
    }

    private void disassemble(CommandContext context, File file) throws Exception {
        OutputFormatter.printBeautifulSection(context.getDoc(), context.getStyle(), "Дизассемблирование " + file.getName());

        byte[] fileContent = Files.readAllBytes(file.toPath());
        
        if (fileContent.length < 4) {
            OutputFormatter.printError(context.getDoc(), context.getStyle(), "Файл слишком мал для анализа");
            return;
        }

        boolean isPE = fileContent[0] == 0x4D && fileContent[1] == 0x5A; // MZ
        boolean isELF = fileContent[0] == 0x7F && fileContent[1] == 0x45 && 
                       fileContent[2] == 0x4C && fileContent[3] == 0x46; // ELF

        if (!isPE && !isELF) {
            OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(),
                "Предупреждение: Файл не является исполняемым (PE/ELF)\n" +
                "Попытка дизассемблировать как бинарный код...");
        }

        int entryPoint = findEntryPoint(fileContent);
        if (entryPoint == -1) {
            entryPoint = 0;
        }

        String[][] info = {
            {"Тип файла", isPE ? "PE (Windows Executable)" : isELF ? "ELF (Linux Executable)" : "Бинарный файл"},
            {"Точка входа", String.format("0x%08X", entryPoint)},
            {"Размер", String.format("%,d байт", fileContent.length)}
        };
        
        OutputFormatter.printBeautifulTable(context.getDoc(), context.getStyle(),
            new String[]{"Параметр", "Значение"}, info);

        OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(), "\nДизассемблированный код:");

        List<String[]> instructions = new ArrayList<>();
        for (int i = entryPoint; i < Math.min(fileContent.length, entryPoint + 1000); i++) {
            try {
                Instruction instr = decodeInstruction(fileContent, i);
                if (instr != null) {
                    instructions.add(new String[]{
                        String.format("%08X", i),
                        formatBytes(fileContent, i, instr.length),
                        instr.mnemonic
                    });
                    i += instr.length - 1;
                } else {
                    instructions.add(new String[]{
                        String.format("%08X", i),
                        String.format("%02X", fileContent[i]),
                        "DB " + String.format("%02X", fileContent[i])
                    });
                }
            } catch (Exception e) {
                continue;
            }
        }

        if (!instructions.isEmpty()) {
            OutputFormatter.printBeautifulTable(context.getDoc(), context.getStyle(),
                new String[]{"Смещение", "Байты", "Инструкция"},
                instructions.toArray(new String[0][0]));
        }
    }

    private static class Instruction {
        int length;
        String mnemonic;

        Instruction(int length, String mnemonic) {
            this.length = length;
            this.mnemonic = mnemonic;
        }
    }

    private int findEntryPoint(byte[] fileContent) {
        if (fileContent[0] == 0x4D && fileContent[1] == 0x5A) { // PE
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
        else if (fileContent[0] == 0x7F && fileContent[1] == 0x45 && // ELF
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
        return sb.toString().trim();
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

    private boolean isPrintable(char c) {
        return c >= 32 && c < 127;
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02X ", b));
        }
        return result.toString().trim();
    }

    private String detectFileType(byte[] header) {
        if (header[0] == 'M' && header[1] == 'Z') return "Исполняемый файл (EXE)";
        if (header[0] == 'P' && header[1] == 'K') return "Архив ZIP";
        if (header[0] == (byte)0xFF && header[1] == (byte)0xD8) return "Изображение JPEG";
        if (header[0] == (byte)0x89 && header[1] == 'P') return "Изображение PNG";
        if (header[0] == '%' && header[1] == 'P') return "Документ PDF";
        return "Неизвестный формат";
    }

    private void showUsage(CommandContext context) throws Exception {
        OutputFormatter.printBeautifulSection(context.getDoc(), context.getStyle(), "Использование команды reverse");
        OutputFormatter.printBeautifulMessage(context.getDoc(), context.getStyle(),
            "reverse <подкоманда> <файл>\n\n" +
            "Подкоманды:\n" +
            "  strings  - извлечь текстовые строки из файла\n" +
            "  header   - анализ заголовка файла\n" +
            "  hex      - шестнадцатеричный дамп файла\n" +
            "  disasm   - дизассемблировать файл");
    }

    @Override
    public void executeCommand(String... args) throws Exception {
        CommandContext context = new CommandContext("", args, doc, style, pathHolder);
        execute(context);
    }
} 