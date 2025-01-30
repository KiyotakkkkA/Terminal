package com.terminal;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import com.terminal.sdk.AbstractAsyncCommand;
import com.terminal.sdk.core.AsyncCommand;
import com.terminal.sdk.core.AsyncTaskManager;
import com.terminal.sdk.core.Command;
import com.terminal.sdk.core.CommandCategory;
import com.terminal.sdk.core.CommandContext;
import com.terminal.sdk.core.CommandFacade;
import com.terminal.sdk.core.CommandInfo;
import com.terminal.sdk.core.handlers.LoggingCommandHandler;
import com.terminal.sdk.events.EventType;
import com.terminal.sdk.events.TerminalEvent;
import com.terminal.sdk.services.IEventManager;
import com.terminal.sdk.services.IPluginManager;
import com.terminal.sdk.services.IThemeManager;
import com.terminal.sdk.services.ServiceLocator;
import com.terminal.sdk.services.TerminalService;
import com.terminal.sdk.system.CurrentPathHolder;
import com.terminal.utils.BannerGenerator;
import com.terminal.utils.InputCallback;
import com.terminal.utils.PluginManager;
import com.terminal.utils.SidebarManager;

public class TerminalPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private Color backgroundColor = new Color(13, 17, 23);
    private Color defaultTextColor = new Color(201, 209, 217);
    private Color usernameColor = new Color(88, 166, 255);
    private Color directoryColor = new Color(246, 185, 59);
    private Color errorColor = new Color(248, 81, 73);
    private Color successColor = new Color(63, 185, 80);
    private Color suggestionColor = new Color(139, 148, 158);
    private Color promptColor = new Color(126, 231, 135);
    private static final int LEFT_PADDING = 10;
    private final JTextPane textPane = new JTextPane();
    private Style defaultStyle;
    private Style usernameStyle;
    private Style directoryStyle;
    private Style errorStyle;
    private Style successStyle;
    private Style suggestionStyle;
    private Style promptStyle;
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
    private CurrentPathHolder pathHolder;
    private String savedContent;
    private final TerminalFrame frame;
    private String currentSuggestion = "";
    private boolean isUpdatingSuggestion = false;
    private String lastInput = "";
    private static final String PROMPT_SYMBOL = "❯";
    private static final String BRANCH_SYMBOL = "";
    private static final int FONT_SIZE = 14;
    private static final int PADDING = 12;
    private SidebarManager sidebarManager;
    private final IEventManager eventManager;
    private final IThemeManager themeManager;
    private final IPluginManager pluginManager;
    private boolean isLocked = false;
    private Command currentCommand;
    private static int nextId = 0;
    private final int terminalId;
    private CommandFacade commandFacade;

    public TerminalPanel(TerminalFrame frame, String version) {
        this.frame = frame;
        this.terminalId = nextId++;
        
        ServiceLocator locator = ServiceLocator.getInstance();
        this.eventManager = locator.resolve(IEventManager.class);
        this.themeManager = locator.resolve(IThemeManager.class);
        this.pluginManager = locator.resolve(IPluginManager.class);
        this.commandFacade = CommandFacade.getInstance();
        
        // Добавляем обработчик логирования в начало цепочки
        this.commandFacade.addCommandHandler(new LoggingCommandHandler());
        
        // Регистрируем текущий терминал
        TerminalService.getInstance().setTerminalPanel(this);
        
        this.pathHolder = CurrentPathHolder.getInstance();
        
        initializeUI();
        initializeStyles();
        initializeKeyBindings();
        printWelcomeMessage(version);
        initializeCommands();
        
        // Инициализируем начальное состояние
        userInputStart = 0;
        displayPrompt();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        
        sidebarManager = new SidebarManager(this);
        add(sidebarManager.getSidebarPanel(), BorderLayout.WEST);
        
        pathHolder = CurrentPathHolder.getInstance();
        
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
        
        backgroundColor = Color.decode(themeManager.getThemeColor("background"));
        defaultTextColor = Color.decode(themeManager.getThemeColor("foreground"));
        usernameColor = Color.decode(themeManager.getThemeColor("username"));
        directoryColor = Color.decode(themeManager.getThemeColor("directory"));
        errorColor = Color.decode(themeManager.getThemeColor("error"));
        successColor = Color.decode(themeManager.getThemeColor("success"));
        suggestionColor = Color.decode(themeManager.getThemeColor("suggestion"));
        promptColor = Color.decode(themeManager.getThemeColor("prompt"));
        
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
        
        setupKeyListeners();
        
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
        
        eventManager.subscribe(EventType.STATE_CHANGED, this::handleStateChanged);
        eventManager.subscribe(EventType.COMMAND_COMPLETED, this::handleCommandCompleted);
        eventManager.subscribe(EventType.COMMAND_FAILED, this::handleCommandFailed);
        eventManager.subscribe(EventType.OUTPUT_UPDATED, this::handleOutputUpdated);
        eventManager.subscribe(EventType.THEME_CHANGED, this::handleThemeChanged);
        
        commands.putAll(pluginManager.getPluginCommands());

        textPane.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                requestFocusInWindow();
            }

            @Override
            public void mousePressed(MouseEvent e) {}

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mouseEntered(MouseEvent e) {}

            @Override
            public void mouseExited(MouseEvent e) {}
        });
    }

    private void initializeStyles() {
        // Implementation of initializeStyles method
    }

    private void initializeKeyBindings() {
        // Implementation of initializeKeyBindings method
    }

    private void initializeCommands() {
        StyledDocument doc = textPane.getStyledDocument();
        
        // Файловые операции
        registerCommand("pwd", new PwdCommand(doc, defaultStyle, pathHolder), CommandCategory.FILE_OPERATIONS);
        registerCommand("ls", new LsCommand(doc, defaultStyle, directoryStyle, pathHolder), CommandCategory.FILE_OPERATIONS);
        registerCommand("cd", new CdCommand(doc, defaultStyle, pathHolder), CommandCategory.FILE_OPERATIONS);
        registerCommand("mkdir", new MkdirCommand(doc, defaultStyle, pathHolder), CommandCategory.FILE_OPERATIONS);
        registerCommand("rmdir", new RmdirCommand(doc, defaultStyle, pathHolder), CommandCategory.FILE_OPERATIONS);
        registerCommand("cat", new CatCommand(doc, defaultStyle, pathHolder, commands), CommandCategory.FILE_OPERATIONS);
        registerCommand("touch", new TouchCommand(doc, defaultStyle, pathHolder), CommandCategory.FILE_OPERATIONS);
        registerCommand("nano", new NanoCommand(doc, defaultStyle, pathHolder), CommandCategory.FILE_OPERATIONS);
        registerCommand("rm", new RmCommand(doc, defaultStyle, pathHolder), CommandCategory.FILE_OPERATIONS);
        
        // Сетевые команды
        registerCommand("ping", new PingCommand(doc, defaultStyle), CommandCategory.NETWORK);
        registerCommand("netstat", new NetstatCommand(doc, defaultStyle, promptStyle), CommandCategory.NETWORK);
        registerCommand("trace", new TraceCommand(doc, defaultStyle, promptStyle), CommandCategory.NETWORK);
        registerCommand("dns", new DnsCommand(doc, defaultStyle, pathHolder), CommandCategory.NETWORK);
        registerCommand("nmap", new NmapCommand(doc, defaultStyle), CommandCategory.NETWORK);
        registerCommand("portscan", new PortScanCommand(doc, defaultStyle, promptStyle), CommandCategory.NETWORK);
        registerCommand("wifi", new WifiScanCommand(doc, defaultStyle, promptStyle, pathHolder), CommandCategory.NETWORK);
        registerCommand("web", new WebCommand(doc, defaultStyle, pathHolder), CommandCategory.NETWORK);
        registerCommand("con", new ConCommand(doc, defaultStyle, promptStyle, pathHolder), CommandCategory.NETWORK);
        
        // Утилиты
        registerCommand("grep", new GrepCommand(doc, defaultStyle, pathHolder), CommandCategory.SEARCH_AND_PROCESS);
        registerCommand("find", new FindCommand(doc, defaultStyle, pathHolder), CommandCategory.SEARCH_AND_PROCESS);
        registerCommand("zip", new ZipCommand(doc, defaultStyle, pathHolder), CommandCategory.SEARCH_AND_PROCESS);
        registerCommand("unzip", new UnzipCommand(doc, defaultStyle, pathHolder), CommandCategory.SEARCH_AND_PROCESS);
        registerCommand("reverse", new ReverseCommand(doc, defaultStyle, pathHolder), CommandCategory.SEARCH_AND_PROCESS);
        registerCommand("hash", new HashCommand(doc, defaultStyle, pathHolder), CommandCategory.SEARCH_AND_PROCESS);
        registerCommand("crypto", new CryptoCommand(doc, defaultStyle, pathHolder), CommandCategory.SEARCH_AND_PROCESS);
        
        // Системные команды
        registerCommand("ps", new PsCommand(doc, defaultStyle, promptStyle), CommandCategory.SYSTEM);
        registerCommand("sys", new SysCommand(doc, defaultStyle, promptStyle), CommandCategory.SYSTEM);
        registerCommand("cls", new ClearCommand(doc, defaultStyle, pathHolder), CommandCategory.SYSTEM);
        registerCommand("plugins", new PluginsCommand(doc, defaultStyle, pathHolder), CommandCategory.SYSTEM);
        registerCommand("theme", new ThemeCommand(doc, defaultStyle, pathHolder), CommandCategory.SYSTEM);
        registerCommand("split", new SplitCommand(doc, defaultStyle, frame), CommandCategory.SYSTEM);
        
        // Служебные команды
        registerCommand("help", new HelpCommand(doc, defaultStyle, promptStyle, pathHolder, commands), CommandCategory.SYSTEM);
        registerCommand("exit", new ExitCommand(doc, defaultStyle, () -> System.exit(0)), CommandCategory.SYSTEM);
    }

    private void registerCommand(String name, Command command, CommandCategory category) {
        CommandInfo info = new CommandInfo(name, command.getDescription(), category.name(), command);
        commands.put(name, info);
        // Больше не регистрируем команду в CommandFacade, так как будем использовать прямое выполнение
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

    private void executeCommand(String input) {
        try {
            String[] parts = input.trim().split("\\s+");
            if (parts.length == 0 || input.trim().isEmpty()) {
                displayPrompt();
                return;
            }

            String commandName = parts[0].toLowerCase();
            CommandInfo commandInfo = commands.get(commandName);
            appendString("\n", defaultStyle);

            if (commandInfo == null) {
                appendString("Ошибка: Команда не найдена: " + commandName + "\n", errorStyle);
                displayPrompt();
            } else {
                String[] args = new String[parts.length - 1];
                System.arraycopy(parts, 1, args, 0, args.length);
                
                currentCommand = commandInfo.getCommand();
                
                if (!commandHistory.contains(input.trim())) {
                    commandHistory.add(input.trim());
                    historyIndex = commandHistory.size();
                    // Добавляем команду в боковую панель истории
                    sidebarManager.addToHistory(input.trim());
                }
                
                // Выполняем команду напрямую
                Command command = commandInfo.getCommand();
                command.execute(new CommandContext(commandName, args, textPane.getStyledDocument(), defaultStyle, pathHolder));
                
                // Добавляем перенос строки после выполнения команды
                appendString("\n", defaultStyle);
                displayPrompt();
            }
        } catch (Exception e) {
            e.printStackTrace();
            appendString("\n", defaultStyle);
            appendString("Ошибка: " + e.getMessage() + "\n", errorStyle);
            displayPrompt();
        }
    }

    private void handleCtrlC() {
        if (currentCommand instanceof AbstractAsyncCommand) {
            AbstractAsyncCommand asyncCommand = (AbstractAsyncCommand) currentCommand;
            
            lock();
            
            AsyncTaskManager.getInstance().cancelAllTasks(terminalId);
            
            asyncCommand.interrupt();
            
            SwingUtilities.invokeLater(() -> {
                currentCommand = null;
                appendString("\n^C\n", errorStyle);
                unlock();
                userInputStart = textPane.getDocument().getLength();
                displayPrompt();
            });
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

    public String getCurrentPath() {
        return pathHolder.getCurrentPath();
    }

    public void setCurrentPath(String path) {
        pathHolder.setCurrentPath(path);
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
                
                CommandInfo commandInfo = commands.get(commandPart);
                if (commandInfo != null) {
                    String[] suggestions = commandInfo.getCommand().getSuggestions(
                        Arrays.copyOfRange(parts, 1, parts.length)
                    );
                    
                    if (suggestions.length > 0) {
                        String lastPart = parts[parts.length - 1];
                        String suggestion = suggestions[0];
                        
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
                userInputStart = textPane.getDocument().getLength();
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
            backgroundColor = Color.decode(themeManager.getThemeColor("background"));
            defaultTextColor = Color.decode(themeManager.getThemeColor("foreground"));
            usernameColor = Color.decode(themeManager.getThemeColor("username"));
            directoryColor = Color.decode(themeManager.getThemeColor("directory"));
            errorColor = Color.decode(themeManager.getThemeColor("error"));
            successColor = Color.decode(themeManager.getThemeColor("success"));
            suggestionColor = Color.decode(themeManager.getThemeColor("suggestion"));
            promptColor = Color.decode(themeManager.getThemeColor("prompt"));
            
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
        eventManager.unsubscribe(EventType.STATE_CHANGED, this::handleStateChanged);
        eventManager.unsubscribe(EventType.COMMAND_COMPLETED, this::handleCommandCompleted);
        eventManager.unsubscribe(EventType.COMMAND_FAILED, this::handleCommandFailed);
        eventManager.unsubscribe(EventType.OUTPUT_UPDATED, this::handleOutputUpdated);
        eventManager.unsubscribe(EventType.THEME_CHANGED, this::handleThemeChanged);
        
        pluginManager.shutdown();
        
        eventManager.clear();
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

    public void addToFavorites(String command) {
        sidebarManager.addToFavorites(command);
    }

    public void removeFromFavorites(String command) {
        sidebarManager.removeFromFavorites(command);
    }

    public void insertCommand(String command) {
        try {
            StyledDocument doc = textPane.getStyledDocument();
            doc.remove(userInputStart, doc.getLength() - userInputStart);
            doc.insertString(userInputStart, command, promptStyle);
            textPane.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void lock() {
        isLocked = true;
        textPane.setEditable(false);
    }

    public synchronized void unlock() {
        isLocked = false;
        textPane.setEditable(true);
    }

    public boolean isLocked() {
        return isLocked;
    }

    public JTextPane getTextPane() {
        return textPane;
    }
    
    public void displayPromptPublic() {
        displayPrompt();
    }

    public void setCurrentCommand(Command command) {
        this.currentCommand = command;
        System.out.println("Current command set to: " + (command instanceof AbstractAsyncCommand || command instanceof AsyncCommand));
    }

    public int getTerminalId() {
        return terminalId;
    }

    private void setupKeyListeners() {
        textPane.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_C && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
                    e.consume();
                    handleCtrlC();
                    return;
                }

                if (isLocked) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_PAGE_UP:
                        case KeyEvent.VK_PAGE_DOWN:
                        case KeyEvent.VK_UP:
                        case KeyEvent.VK_DOWN:
                        case KeyEvent.VK_LEFT:
                        case KeyEvent.VK_RIGHT:
                        case KeyEvent.VK_HOME:
                        case KeyEvent.VK_END:
                            break;
                        default:
                            e.consume();
                            return;
                    }
                }

                if (e.getKeyCode() == KeyEvent.VK_ENTER && !isLocked) {
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
                            executeCommand(input);
                        } else {
                            displayPrompt();
                        }
                    }
                    return;
                }

                if (e.getKeyCode() == KeyEvent.VK_D && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) != 0) {
                    if (isInputMode) {
                        e.consume();
                        finishInput();
                        return;
                    }
                }

                if (!isInputMode) {
                    int caretPosition = textPane.getCaretPosition();
                    if (caretPosition < userInputStart) {
                        e.consume();
                        textPane.setCaretPosition(textPane.getDocument().getLength());
                        return;
                    }

                    if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || 
                        e.getKeyCode() == KeyEvent.VK_DELETE ||
                        (e.getKeyCode() == KeyEvent.VK_A && e.isControlDown()) ||
                        (e.getKeyCode() == KeyEvent.VK_X && e.isControlDown())) {
                        if (textPane.getSelectionStart() < userInputStart) {
                            e.consume();
                            return;
                        }
                    }
                }

                if (!textPane.isEditable() && !isInputMode) {
                    if (e.getKeyCode() != KeyEvent.VK_TAB) {
                        textPane.setEditable(true);
                        userInputStart = textPane.getDocument().getLength();
                        textPane.setCaretPosition(userInputStart);
                    }
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
                        if (textPane.getCaretPosition() < userInputStart) {
                            e.consume();
                            textPane.setCaretPosition(userInputStart);
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

            @Override
            public void keyTyped(KeyEvent e) {
                if (isLocked) {
                    e.consume();
                }
            }
        });
    }

    private void printWelcomeMessage(String version) {
        String banner = BannerGenerator.generate(
            String.format("Terminal v%s", version),
            "• Type 'help' to see available commands",
            "• Press Tab for autocompletion",
            "• Use arrow keys to navigate history"
        );
        
        Style welcomeStyle = textPane.addStyle("welcomeStyle", defaultStyle);
        StyleConstants.setForeground(welcomeStyle, usernameColor);
        StyleConstants.setBold(welcomeStyle, true);
        
        appendString(banner, welcomeStyle);
        userInputStart = textPane.getDocument().getLength();
    }
} 