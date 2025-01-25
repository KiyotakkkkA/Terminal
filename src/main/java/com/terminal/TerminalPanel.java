package com.terminal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import com.terminal.commands.CatCommand;
import com.terminal.commands.CdCommand;
import com.terminal.commands.ClearCommand;
import com.terminal.commands.ConCommand;
import com.terminal.commands.CryptoCommand;
import com.terminal.commands.DnsCommand;
import com.terminal.commands.ExitCommand;
import com.terminal.commands.FindCommand;
import com.terminal.commands.FuzzCommand;
import com.terminal.commands.GrepCommand;
import com.terminal.commands.HashCommand;
import com.terminal.commands.HelpCommand;
import com.terminal.commands.LsCommand;
import com.terminal.commands.MkdirCommand;
import com.terminal.commands.NanoCommand;
import com.terminal.commands.NetstatCommand;
import com.terminal.commands.NmapCommand;
import com.terminal.commands.PingCommand;
import com.terminal.commands.PluginsCommand;
import com.terminal.commands.PortScanCommand;
import com.terminal.commands.ProcessAnalyzerCommand;
import com.terminal.commands.PsCommand;
import com.terminal.commands.PwdCommand;
import com.terminal.commands.ReverseCommand;
import com.terminal.commands.RmCommand;
import com.terminal.commands.RmdirCommand;
import com.terminal.commands.SplitCommand;
import com.terminal.commands.SysCommand;
import com.terminal.commands.ThemeCommand;
import com.terminal.commands.TouchCommand;
import com.terminal.commands.TraceCommand;
import com.terminal.commands.UnzipCommand;
import com.terminal.commands.WebCommand;
import com.terminal.commands.WifiScanCommand;
import com.terminal.commands.ZipCommand;
import com.terminal.sdk.Command;
import com.terminal.sdk.CommandCategory;
import com.terminal.sdk.CommandInfo;
import com.terminal.sdk.CurrentPathHolder;
import com.terminal.sdk.EventManager;
import com.terminal.sdk.EventType;
import com.terminal.sdk.TerminalEvent;
import com.terminal.utils.InputCallback;
import com.terminal.utils.PluginManager;
import com.terminal.utils.ThemeManager;

public class TerminalPanel extends JPanel implements CurrentPathHolder {
    private Color backgroundColor = new Color(13, 17, 23);
    private Color defaultTextColor = new Color(201, 209, 217);
    private Color usernameColor = new Color(88, 166, 255);
    private Color directoryColor = new Color(246, 185, 59);
    private Color errorColor = new Color(248, 81, 73);
    private Color successColor = new Color(63, 185, 80);
    private Color suggestionColor = new Color(139, 148, 158);
    private Color promptColor = new Color(126, 231, 135);
    private static final int LEFT_PADDING = 10;
    private final JTextPane textPane;
    private final Style defaultStyle;
    private final Style usernameStyle;
    private final Style directoryStyle;
    private final Style errorStyle;
    private final Style successStyle;
    private final Style suggestionStyle;
    private final Style promptStyle;
    private int userInputStart;
    private int commandStart;
    private String currentPath;
    private final List<String> commandHistory = new ArrayList<>();
    private int historyIndex;
    private final Map<String, CommandInfo> commands = new HashMap<>();
    private final Map<String, CommandInfo> subCommands = new HashMap<>();
    private boolean isInputMode = false;
    private StringBuilder inputBuffer;
    private InputCallback inputCallback;
    private String savedContent;
    private final TerminalFrame frame;
    private String currentSuggestion = "";
    private boolean isUpdatingSuggestion = false;
    private String lastInput = "";
    private static final String PROMPT_SYMBOL = "❯";
    private static final String BRANCH_SYMBOL = "";
    private static final int FONT_SIZE = 14;
    private static final int PADDING = 12;

    public TerminalPanel(TerminalFrame frame) {
        this.frame = frame;
        setLayout(new BorderLayout());
        
        addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                frame.setActivePanel(TerminalPanel.this);
                setBorder(javax.swing.BorderFactory.createLineBorder(promptColor, 1));
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                setBorder(null);
            }
        });
        
        addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                requestFocusInWindow();
            }
        });
        
        setFocusable(true);
        
        ThemeManager themeManager = ThemeManager.getInstance();
        backgroundColor = Color.decode(themeManager.getThemeColor("background"));
        defaultTextColor = Color.decode(themeManager.getThemeColor("foreground"));
        usernameColor = Color.decode(themeManager.getThemeColor("username"));
        directoryColor = Color.decode(themeManager.getThemeColor("directory"));
        errorColor = Color.decode(themeManager.getThemeColor("error"));
        successColor = Color.decode(themeManager.getThemeColor("success"));
        suggestionColor = Color.decode(themeManager.getThemeColor("suggestion"));
        promptColor = Color.decode(themeManager.getThemeColor("prompt"));
        
        textPane = new JTextPane();
        textPane.setBackground(backgroundColor);
        textPane.setCaretColor(defaultTextColor);
        
        textPane.putClientProperty("JTextPane.w3cLength", Boolean.FALSE);
        textPane.putClientProperty("JTextPane.honorDisplayProperties", Boolean.TRUE);
        
        textPane.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                frame.setActivePanel(TerminalPanel.this);
                TerminalPanel.this.setBorder(javax.swing.BorderFactory.createLineBorder(promptColor, 1));
            }
        });
        
        Font terminalFont;
        try {
            terminalFont = Font.createFont(Font.TRUETYPE_FONT, 
                getClass().getResourceAsStream("/fonts/JetBrainsMono-Regular.ttf"));
            terminalFont = terminalFont.deriveFont(14f);
        } catch (Exception e) {
            terminalFont = new Font("Consolas", Font.PLAIN, 14);
        }
        
        textPane.setFont(terminalFont);
        textPane.setMargin(new Insets(10, LEFT_PADDING, 10, 10));
        
        StyleContext sc = new StyleContext();

        promptStyle = sc.addStyle("promptStyle", null);
        StyleConstants.setForeground(promptStyle, promptColor);
        StyleConstants.setBold(promptStyle, true);
        
        defaultStyle = sc.addStyle("DefaultStyle", null);
        StyleConstants.setForeground(defaultStyle, defaultTextColor);
        StyleConstants.setBold(defaultStyle, true);
        
        usernameStyle = sc.addStyle("UsernameStyle", null);
        StyleConstants.setForeground(usernameStyle, usernameColor);
        StyleConstants.setBold(usernameStyle, true);

        directoryStyle = sc.addStyle("DirectoryStyle", null);
        StyleConstants.setForeground(directoryStyle, directoryColor);
        StyleConstants.setBold(directoryStyle, true);

        errorStyle = sc.addStyle("ErrorStyle", null);
        StyleConstants.setForeground(errorStyle, errorColor);
        StyleConstants.setBold(errorStyle, true);

        successStyle = sc.addStyle("SuccessStyle", null);
        StyleConstants.setForeground(successStyle, successColor);
        StyleConstants.setBold(successStyle, true);
        
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(null);
        scrollPane.setBackground(backgroundColor);
        scrollPane.getViewport().setBackground(backgroundColor);
        add(scrollPane, BorderLayout.CENTER);
        
        historyIndex = -1;
        currentPath = System.getProperty("user.dir");
        
        printWelcomeMessage();
        initializeCommands();
        setupKeyListeners();
        displayPrompt();
        
        suggestionStyle = textPane.addStyle("suggestion", defaultStyle);
        StyleConstants.setForeground(suggestionStyle, suggestionColor);
        
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (!isInputMode) {
                    updateSuggestion();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!isInputMode) {
                    updateSuggestion();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                if (!isInputMode) {
                    updateSuggestion();
                }
            }
        });

        PluginManager.getInstance().loadPlugins();
        
        EventManager.getInstance().subscribe(EventType.STATE_CHANGED, this::handleStateChanged);
        EventManager.getInstance().subscribe(EventType.COMMAND_COMPLETED, this::handleCommandCompleted);
        EventManager.getInstance().subscribe(EventType.COMMAND_FAILED, this::handleCommandFailed);
        EventManager.getInstance().subscribe(EventType.OUTPUT_UPDATED, this::handleOutputUpdated);
        EventManager.getInstance().subscribe(EventType.THEME_CHANGED, this::handleThemeChanged);
        
        commands.putAll(PluginManager.getInstance().getPluginCommands());
    }

    private void initializeCommands() {
        StyledDocument doc = textPane.getStyledDocument();
        
        // Файловые операции
        registerCommand("pwd", new PwdCommand(doc, defaultStyle, this), CommandCategory.FILE_OPERATIONS);
        registerCommand("ls", new LsCommand(doc, defaultStyle, directoryStyle, this), CommandCategory.FILE_OPERATIONS);
        registerCommand("cd", new CdCommand(doc, defaultStyle, this), CommandCategory.FILE_OPERATIONS);
        registerCommand("mkdir", new MkdirCommand(doc, defaultStyle, this), CommandCategory.FILE_OPERATIONS);
        registerCommand("rmdir", new RmdirCommand(doc, defaultStyle, this), CommandCategory.FILE_OPERATIONS);
        registerCommand("cat", new CatCommand(doc, defaultStyle, this, commands), CommandCategory.FILE_OPERATIONS);
        registerCommand("touch", new TouchCommand(doc, defaultStyle, this), CommandCategory.FILE_OPERATIONS);
        registerCommand("nano", new NanoCommand(doc, defaultStyle, this), CommandCategory.FILE_OPERATIONS);
        registerCommand("rm", new RmCommand(doc, defaultStyle, this), CommandCategory.FILE_OPERATIONS);

        // Сетевые команды
        registerCommand("ping", new PingCommand(doc, defaultStyle), CommandCategory.NETWORK);
        registerCommand("netstat", new NetstatCommand(doc, defaultStyle), CommandCategory.NETWORK);
        registerCommand("trace", new TraceCommand(doc, defaultStyle), CommandCategory.NETWORK);
        registerCommand("dns", new DnsCommand(doc, defaultStyle, this), CommandCategory.NETWORK);
        registerCommand("nmap", new NmapCommand(doc, defaultStyle), CommandCategory.NETWORK);
        registerCommand("portscan", new PortScanCommand(doc, defaultStyle), CommandCategory.NETWORK);
        registerCommand("wifi", new WifiScanCommand(doc, defaultStyle), CommandCategory.NETWORK);
        registerCommand("web", new WebCommand(doc, defaultStyle), CommandCategory.NETWORK);
        registerCommand("con", new ConCommand(doc, defaultStyle), CommandCategory.NETWORK);

        // Системные команды
        registerCommand("ps", new PsCommand(doc, defaultStyle), CommandCategory.SYSTEM);
        registerCommand("sys", new SysCommand(doc, defaultStyle), CommandCategory.SYSTEM);
        registerCommand("procanalyze", new ProcessAnalyzerCommand(doc, defaultStyle), CommandCategory.SYSTEM);
        registerCommand("cls", new ClearCommand(textPane), CommandCategory.SYSTEM);
        registerCommand("plugins", new PluginsCommand(doc, defaultStyle), CommandCategory.SYSTEM);
        registerCommand("theme", new ThemeCommand(doc, defaultStyle), CommandCategory.SYSTEM);
        registerCommand("split", new SplitCommand(doc, defaultStyle, frame), CommandCategory.SYSTEM);

        // Поиск и обработка
        registerCommand("find", new FindCommand(doc, defaultStyle, this), CommandCategory.SEARCH_AND_PROCESS);
        registerCommand("grep", new GrepCommand(doc, defaultStyle, this), CommandCategory.SEARCH_AND_PROCESS);
        registerCommand("zip", new ZipCommand(doc, defaultStyle, this), CommandCategory.SEARCH_AND_PROCESS);
        registerCommand("unzip", new UnzipCommand(doc, defaultStyle, this), CommandCategory.SEARCH_AND_PROCESS);
        registerCommand("hash", new HashCommand(doc, defaultStyle, this), CommandCategory.SEARCH_AND_PROCESS);
        registerCommand("crypto", new CryptoCommand(doc, defaultStyle, this), CommandCategory.SEARCH_AND_PROCESS);
        registerCommand("fuzz", new FuzzCommand(doc, defaultStyle), CommandCategory.SEARCH_AND_PROCESS);
        registerCommand("reverse", new ReverseCommand(doc, defaultStyle, this), CommandCategory.SEARCH_AND_PROCESS);

        // Служебные команды
        registerCommand("help", new HelpCommand(doc, defaultStyle, commands), CommandCategory.SYSTEM);
        registerCommand("exit", new ExitCommand(doc, defaultStyle, () -> System.exit(0)), CommandCategory.SYSTEM);
    }

    private void registerCommand(String name, Command command, CommandCategory category) {
        commands.put(name, new CommandInfo(name, command, category));
    }

    private void printWelcomeMessage() {
        String version = "2.1";
        String banner = String.format(
            "╔══════════════════════════════════════════════════════════════╗\n" +
            "║                     Terminal v%-3s                            ║\n" +
            "║                                                              ║\n" +
            "║  %s Type 'help' to see available commands                     ║\n" +
            "║  %s Press Tab for autocompletion                              ║\n" +
            "║  %s Use arrow keys to navigate history                        ║\n" +
            "╚══════════════════════════════════════════════════════════════╝\n",
            version, "•", "•", "•"
        );
        
        Style welcomeStyle = textPane.addStyle("welcomeStyle", defaultStyle);
        StyleConstants.setForeground(welcomeStyle, usernameColor);
        StyleConstants.setBold(welcomeStyle, true);
        
        appendString(banner, welcomeStyle);
    }

    private void setupKeyListeners() {
        textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_D && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
                    if (isInputMode) {
                        e.consume();
                        finishInput();
                        return;
                    }
                }

                if (!textPane.isEditable() && !isInputMode) {
                    if (e.getKeyCode() != KeyEvent.VK_TAB) {
                        textPane.setEditable(true);
                        userInputStart = textPane.getDocument().getLength();
                        textPane.setCaretPosition(userInputStart);
                    }
                }

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    if (isInputMode) {
                        try {
                            StyledDocument doc = textPane.getStyledDocument();
                            doc.insertString(textPane.getCaretPosition(), "\n", defaultStyle);
                        } catch (BadLocationException ex) {
                            ex.printStackTrace();
                        }
                    } else {
                        if (!currentSuggestion.isEmpty()) {
                            completeSuggestion();
                        }
                        String input = getCurrentInputLine();
                        if (!input.trim().isEmpty()) {
                            executeCommand();
                        } else {
                            displayPrompt();
                        }
                    }
                    return;
                }

                if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    e.consume();
                    if (!currentSuggestion.isEmpty()) {
                        completeSuggestion();
                    }
                    return;
                }

                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    if (!isInputMode) {
                        if (!currentSuggestion.isEmpty()) {
                            e.consume();
                            removeSuggestion();
                            return;
                        }
                        if (textPane.getCaretPosition() <= userInputStart) {
                            e.consume();
                        }
                    }
                }

                if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    if (!isInputMode) {
                        int maxPosition = textPane.getDocument().getLength() - 
                            (currentSuggestion.isEmpty() ? 0 : currentSuggestion.length());
                        if (textPane.getCaretPosition() > maxPosition) {
                            e.consume();
                            textPane.setCaretPosition(maxPosition);
                        }
                    }
                }

                if (e.getKeyCode() == KeyEvent.VK_UP) {
                    e.consume();
                    showPreviousCommand();
                } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                    e.consume();
                    showNextCommand();
                }
            }
        });
    }

    private void displayPrompt() {
        try {
            StyledDocument doc = textPane.getStyledDocument();
            String currentText = doc.getText(0, doc.getLength());
            if (!currentText.endsWith("\n")) {
                doc.insertString(doc.getLength(), "\n", defaultStyle);
            }
            
            if (isInputMode) {
                return;
            }
            
            String username = System.getProperty("user.name");
            String hostname = "terminal";
            
            appendString(username + "@" + hostname, usernameStyle);
            appendString(" ~ ", defaultStyle);
            appendString(getCurrentPath(), directoryStyle);
            appendString("\n" + PROMPT_SYMBOL + " ", promptStyle);
            
            userInputStart = textPane.getDocument().getLength();
            textPane.setCaretPosition(userInputStart);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    private void appendString(String str, Style style) {
        StyledDocument doc = textPane.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), str, style);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void executeCommand() {
        if (!isActive()) {
            return;
        }
        
        try {
            String commandLine = getCurrentInputLine();
            appendString("\n", defaultStyle);
            
            if (!commandLine.isEmpty()) {
                commandHistory.add(commandLine);
                historyIndex = commandHistory.size();
                
                String[] parts = commandLine.trim().split("\\s+");
                if (parts.length > 0) {
                    String commandName = parts[0].toLowerCase();
                    CommandInfo commandInfo = commands.get(commandName);

                    if (commandInfo == null) {
                        appendString("Ошибка: Команда не найдена: " + commandName + "\n", errorStyle);
                        displayPrompt();
                    } else {
                        String[] args = new String[parts.length - 1];
                        System.arraycopy(parts, 1, args, 0, args.length);
                        
                        try {
                            commandInfo.getCommand().execute(args);
                            displayPrompt();
                        } catch (Exception e) {
                            appendString("Ошибка: " + e.getMessage() + "\n", errorStyle);
                            displayPrompt();
                        }
                    }
                }
            } else {
                displayPrompt();
            }
        } catch (Exception e) {
            appendString("Ошибка: " + e.getMessage() + "\n", errorStyle);
            displayPrompt();
        }
    }

    private void showPreviousCommand() {
        if (!commandHistory.isEmpty() && historyIndex > 0) {
            historyIndex--;
            String command = commandHistory.get(historyIndex);
            StyledDocument doc = textPane.getStyledDocument();
            try {
                doc.remove(userInputStart, doc.getLength() - userInputStart);
                doc.insertString(userInputStart, command, promptStyle);
                textPane.setCaretPosition(doc.getLength());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void showNextCommand() {
        if (!commandHistory.isEmpty() && historyIndex < commandHistory.size() - 1) {
            historyIndex++;
            String command = commandHistory.get(historyIndex);
            StyledDocument doc = textPane.getStyledDocument();
            try {
                doc.remove(userInputStart, doc.getLength() - userInputStart);
                doc.insertString(userInputStart, command, promptStyle);
                textPane.setCaretPosition(doc.getLength());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getCurrentPath() {
        return currentPath;
    }

    @Override
    public void setCurrentPath(String path) {
        this.currentPath = path;
    }

    public void startInputMode(InputCallback callback, String initialText) {
        textPane.setText("");
        try {
            textPane.getDocument().insertString(0, initialText, defaultStyle);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        
        textPane.setEditable(true);
        
        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown()) {
                    if (e.getKeyCode() == KeyEvent.VK_S) {
                        callback.onInputComplete(textPane.getText());
                        textPane.setEditable(false);
                        textPane.removeKeyListener(this);
                    } else if (e.getKeyCode() == KeyEvent.VK_X) {
                        textPane.setEditable(false);
                        textPane.removeKeyListener(this);
                    }
                }
            }
        };
        textPane.addKeyListener(keyAdapter);
    }

    private void finishInput() {
        if (isInputMode && inputCallback != null) {
            isInputMode = false;
            
            String input = textPane.getText();
            
            textPane.setText(savedContent);
            savedContent = null;
            
            frame.setTitle("Terminal");
            frame.setEditMode(false);
            
            inputCallback.onInputComplete(input);
            inputBuffer = null;
            inputCallback = null;
            
            displayPrompt();
        }
    }

    private String getCurrentInputLine() {
        try {
            StyledDocument doc = textPane.getStyledDocument();
            int length = doc.getLength() - userInputStart;
            if (length > 0) {
                String text = doc.getText(userInputStart, length);
                if (!currentSuggestion.isEmpty() && text.endsWith(currentSuggestion)) {
                    text = text.substring(0, text.length() - currentSuggestion.length());
                }
                return text.trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private void updateSuggestion() {
        if (isUpdatingSuggestion) {
            return;
        }
        
        SwingUtilities.invokeLater(() -> {
            try {
                isUpdatingSuggestion = true;
                
                if (!textPane.isEditable()) {
                    return;
                }

                String currentInput = getCurrentInputLine();
                if (currentInput.equals(lastInput)) {
                    return;
                }
                lastInput = currentInput;

                if (currentInput.isEmpty()) {
                    removeSuggestion();
                    return;
                }

                String[] parts = currentInput.split("\\s+");
                String commandPart = parts[0].toLowerCase();
                
                // Если это существующая команда
                CommandInfo commandInfo = commands.get(commandPart);
                if (commandInfo != null) {
                    List<String> suggestions = commandInfo.getCommand().getSuggestions(
                        Arrays.copyOfRange(parts, 1, parts.length)
                    );
                    
                    if (!suggestions.isEmpty()) {
                        String lastPart = parts[parts.length - 1];
                        String suggestion = suggestions.get(0);
                        
                        if (suggestion.toLowerCase().startsWith(lastPart.toLowerCase()) && 
                            !suggestion.equalsIgnoreCase(lastPart)) {
                            String newSuggestion = suggestion.substring(Math.min(lastPart.length(), suggestion.length()));
                            if (!newSuggestion.isEmpty() && !newSuggestion.equals(currentSuggestion)) {
                                int caretPosition = textPane.getCaretPosition();
                                removeSuggestion();
                                currentSuggestion = newSuggestion;
                                StyledDocument doc = textPane.getStyledDocument();
                                doc.insertString(doc.getLength(), newSuggestion, suggestionStyle);
                                textPane.setCaretPosition(caretPosition);
                            }
                        } else {
                            removeSuggestion();
                        }
                    } else {
                        removeSuggestion();
                    }
                    return;
                }

                if (parts.length == 1) {
                    String bestMatch = findBestMatch(commandPart);
                    if (!bestMatch.isEmpty() && !bestMatch.equals(commandPart) && 
                        !bestMatch.equalsIgnoreCase(commandPart)) {
                        String newSuggestion = bestMatch.substring(Math.min(commandPart.length(), bestMatch.length()));
                        if (!newSuggestion.isEmpty() && !newSuggestion.equals(currentSuggestion)) {
                            int caretPosition = textPane.getCaretPosition();
                            removeSuggestion();
                            currentSuggestion = newSuggestion;
                            StyledDocument doc = textPane.getStyledDocument();
                            doc.insertString(doc.getLength(), newSuggestion, suggestionStyle);
                            textPane.setCaretPosition(caretPosition);
                        }
                    } else {
                        removeSuggestion();
                    }
                } else {
                    removeSuggestion();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                isUpdatingSuggestion = false;
            }
        });
    }

    private String findBestMatch(String prefix) {
        return commands.keySet().stream()
            .filter(cmd -> cmd.startsWith(prefix.toLowerCase()))
            .findFirst()
            .orElse("");
    }

    private void completeSuggestion() {
        try {
            if (!currentSuggestion.isEmpty()) {
                StyledDocument doc = textPane.getStyledDocument();
                int currentLength = doc.getLength();
                doc.remove(currentLength - currentSuggestion.length(), currentSuggestion.length());
                doc.insertString(doc.getLength(), currentSuggestion, promptStyle);
                currentSuggestion = "";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void removeSuggestion() {
        try {
            if (!currentSuggestion.isEmpty()) {
                StyledDocument doc = textPane.getStyledDocument();
                int currentLength = doc.getLength();
                doc.remove(currentLength - currentSuggestion.length(), currentSuggestion.length());
                currentSuggestion = "";
                lastInput = getCurrentInputLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CommandInfo getCommand(String name) {
        return commands.get(name);
    }

    private void handleStateChanged(TerminalEvent event) {
        if (!isActive()) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            appendString((String) event.getData() + "\n", successStyle);
        });
    }

    private void handleCommandCompleted(TerminalEvent event) {
        if (!isActive()) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (event.getData() != null) {
                appendString(event.getData().toString(), defaultStyle);
            }
            displayPrompt();
        });
    }

    private void handleCommandFailed(TerminalEvent event) {
        if (!isActive()) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (event.getData() != null) {
                appendString("Ошибка: " + event.getData().toString() + "\n", errorStyle);
            }
            displayPrompt();
        });
    }

    private void handleOutputUpdated(TerminalEvent event) {
        if (!isActive()) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            if (event.getData() != null) {
                appendString(event.getData().toString() + "\n", defaultStyle);
            }
        });
    }

    private void handleThemeChanged(TerminalEvent event) {
        SwingUtilities.invokeLater(() -> {
            String themeName = event.getData().toString();
            applyTheme(themeName);
            textPane.repaint();
        });
    }

    private void applyTheme(String themeName) {
        try {
            com.google.gson.JsonObject theme = ThemeManager.getInstance().getCurrentTheme();
            
            backgroundColor = Color.decode(theme.get("background").getAsString());
            defaultTextColor = Color.decode(theme.get("foreground").getAsString());
            usernameColor = Color.decode(theme.get("username").getAsString());
            directoryColor = Color.decode(theme.get("directory").getAsString());
            errorColor = Color.decode(theme.get("error").getAsString());
            successColor = Color.decode(theme.get("success").getAsString());
            suggestionColor = Color.decode(theme.get("suggestion").getAsString());
            promptColor = Color.decode(theme.get("prompt").getAsString());

            setBackground(backgroundColor);
            textPane.setBackground(backgroundColor);
            textPane.setCaretColor(defaultTextColor);
            textPane.setForeground(defaultTextColor);
            
            Component[] components = getComponents();
            for (Component comp : components) {
                if (comp instanceof JScrollPane) {
                    JScrollPane scrollPane = (JScrollPane) comp;
                    scrollPane.setBackground(backgroundColor);
                    scrollPane.getViewport().setBackground(backgroundColor);
                    scrollPane.setBorder(null);
                }
            }
            
            StyleConstants.setForeground(defaultStyle, defaultTextColor);
            StyleConstants.setForeground(usernameStyle, usernameColor);
            StyleConstants.setForeground(directoryStyle, directoryColor);
            StyleConstants.setForeground(errorStyle, errorColor);
            StyleConstants.setForeground(successStyle, successColor);
            StyleConstants.setForeground(suggestionStyle, suggestionColor);
            StyleConstants.setForeground(promptStyle, promptColor);

            revalidate();
            repaint();
        } catch (Exception e) {
            System.err.println("Ошибка при применении темы: " + e.getMessage());
        }
    }

    public void dispose() {
        EventManager eventManager = EventManager.getInstance();
        eventManager.unsubscribe(EventType.STATE_CHANGED, this::handleStateChanged);
        eventManager.unsubscribe(EventType.COMMAND_COMPLETED, this::handleCommandCompleted);
        eventManager.unsubscribe(EventType.COMMAND_FAILED, this::handleCommandFailed);
        eventManager.unsubscribe(EventType.OUTPUT_UPDATED, this::handleOutputUpdated);
        eventManager.unsubscribe(EventType.THEME_CHANGED, this::handleThemeChanged);
        
        PluginManager.getInstance().shutdown();
        
        eventManager.shutdown();
    }

    public boolean isActive() {
        return frame.getActivePanel() == this;
    }
    
    @Override
    public boolean requestFocusInWindow() {
        if (textPane != null) {
            return textPane.requestFocusInWindow();
        }
        return super.requestFocusInWindow();
    }
} 