package com.terminal.launcher;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import com.terminal.launcher.plugin.PluginManager;
import com.terminal.launcher.update.UpdateDialog;
import com.terminal.launcher.update.UpdateManager;

public class Launcher extends JFrame {
    private static class PluginInfo {
        String name;
        String description;
        String version;
        String author;
        boolean isEnabled;

        PluginInfo(String name, String description, String version, String author, boolean isEnabled) {
            this.name = name;
            this.description = description;
            this.version = version;
            this.author = author;
            this.isEnabled = isEnabled;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class IconGenerator {
        private static final int ICON_SIZE = 16;
        private static ImageIcon enabledIcon;
        private static ImageIcon disabledIcon;
        
        static {
            enabledIcon = createEnabledIcon();
            disabledIcon = createDisabledIcon();
        }
        
        private static ImageIcon createEnabledIcon() {
            BufferedImage image = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            
            // Антиалиасинг для сглаживания
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Зеленый круг для активного плагина
            g2d.setColor(new Color(40, 167, 69));
            g2d.fillOval(0, 0, ICON_SIZE, ICON_SIZE);
            
            // Белая галочка
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(3, 8, 7, 12);
            g2d.drawLine(7, 12, 13, 4);
            
            g2d.dispose();
            return new ImageIcon(image);
        }
        
        private static ImageIcon createDisabledIcon() {
            BufferedImage image = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            
            // Антиалиасинг для сглаживания
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Серый круг для неактивного плагина
            g2d.setColor(new Color(108, 117, 125));
            g2d.fillOval(0, 0, ICON_SIZE, ICON_SIZE);
            
            // Белый крестик
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(4, 4, 12, 12);
            g2d.drawLine(12, 4, 4, 12);
            
            g2d.dispose();
            return new ImageIcon(image);
        }
        
        public static ImageIcon getIcon(boolean enabled) {
            return enabled ? enabledIcon : disabledIcon;
        }

        private static ImageIcon getTabIcon(String type) {
            BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = image.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            switch (type) {
                case "home":
                    drawHomeIcon(g2d);
                    break;
                case "themes":
                    drawThemesIcon(g2d);
                    break;
                case "plugins":
                    drawPluginsIcon(g2d);
                    break;
                case "settings":
                    drawSettingsIcon(g2d);
                    break;
            }
            
            g2d.dispose();
            return new ImageIcon(image);
        }
        
        private static void drawHomeIcon(Graphics2D g2d) {
            g2d.setColor(new Color(52, 152, 219));
            g2d.fillPolygon(new int[]{8, 2, 14}, new int[]{2, 8, 8}, 3);
            g2d.fillRect(4, 8, 8, 6);
        }
        
        private static void drawThemesIcon(Graphics2D g2d) {
            g2d.setColor(new Color(155, 89, 182));
            g2d.fillRect(2, 2, 6, 6);
            g2d.fillRect(8, 8, 6, 6);
        }
        
        private static void drawPluginsIcon(Graphics2D g2d) {
            g2d.setColor(new Color(46, 204, 113));
            g2d.fillRoundRect(2, 4, 12, 8, 3, 3);
            g2d.setColor(new Color(39, 174, 96));
            g2d.fillRect(4, 2, 2, 4);
            g2d.fillRect(10, 2, 2, 4);
        }
        
        private static void drawSettingsIcon(Graphics2D g2d) {
            g2d.setColor(new Color(149, 165, 166));
            g2d.fillOval(4, 4, 8, 8);
            for (int i = 0; i < 8; i++) {
                double angle = Math.toRadians(i * 45);
                g2d.fillRect(
                    8 + (int)(6 * Math.cos(angle)) - 1,
                    8 + (int)(6 * Math.sin(angle)) - 1,
                    2, 2
                );
            }
        }
    }

    private static class PluginListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            
            if (value instanceof PluginInfo) {
                PluginInfo plugin = (PluginInfo) value;
                label.setText(plugin.name + (plugin.isEnabled ? "" : " (Отключен)"));
                label.setIcon(IconGenerator.getIcon(plugin.isEnabled));
            }
            
            return label;
        }
    }

    private static final Logger LOGGER = Logger.getLogger(Launcher.class.getName());
    private static final String GITHUB_API_URL = "https://api.github.com/repos/KiyotakkkkA/Terminal";
    private static final String GITHUB_RAW_URL = "https://raw.githubusercontent.com/KiyotakkkkA/Terminal/master";
    private static final String CONFIG_FILE = "launcher.properties";
    
    private JTextField pathField;
    private JButton browseButton;
    private JButton installButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JTabbedPane tabbedPane;
    private JPanel mainPanel;
    private JPanel themesPanel;
    private JPanel pluginsPanel;
    private JPanel settingsPanel;
    private JComboBox<String> themeComboBox;
    private JCheckBox autoUpdateCheckBox;
    private JSpinner memorySpinner;
    
    private String installPath;
    private Properties config;
    private Process currentProcess;
    private Thread shutdownHook;
    private JLabel systemStatusLabel;
    private int totalInstallSteps = 7;
    private int currentInstallStep = 0;

    private PluginManager pluginManager;
    private UpdateManager updateManager;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                LOGGER.warning("Could not set system look and feel: " + e.getMessage());
            }
            new Launcher().setVisible(true);
        });
    }

    public Launcher() {
        super("Terminal Launcher");
        setIconImage(createLauncherIcon());
        loadConfig();
        initializeUI();
        checkExistingInstallation();
        this.pluginManager = new PluginManager();
        if (installPath != null) {
            this.updateManager = new UpdateManager(installPath, new UpdateDialog(this));
        }

        // Добавляем обработчик закрытия окна
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdown();
            }
        });
    }

    private Image createLauncherIcon() {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Фон иконки
        g2d.setColor(new Color(44, 62, 80));
        g2d.fillRect(0, 0, 32, 32);
        
        // Символ терминала
        g2d.setColor(new Color(46, 204, 113));
        g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2d.drawString(">_", 4, 22);
        
        g2d.dispose();
        return image;
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(600, 400));

        JPanel mainContainer = new JPanel(new BorderLayout());
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        topPanel.setBackground(new Color(44, 62, 80));
        
        JLabel titleLabel = new JLabel("Terminal Launcher");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel, BorderLayout.WEST);
        
        JLabel timeLabel = new JLabel();
        timeLabel.setForeground(Color.WHITE);
        updateTimeLabel(timeLabel);
        topPanel.add(timeLabel, BorderLayout.EAST);
        
        mainContainer.add(topPanel, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        mainPanel = createMainPanel();
        tabbedPane.addTab("Главная", IconGenerator.getTabIcon("home"), mainPanel);

        themesPanel = createThemesPanel();
        tabbedPane.addTab("Темы", IconGenerator.getTabIcon("themes"), themesPanel);

        pluginsPanel = createPluginsPanel();
        tabbedPane.addTab("Плагины", IconGenerator.getTabIcon("plugins"), pluginsPanel);

        settingsPanel = createSettingsPanel();
        tabbedPane.addTab("Настройки", IconGenerator.getTabIcon("settings"), settingsPanel);

        mainContainer.add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new EmptyBorder(5, 10, 5, 10));
        bottomPanel.setBackground(new Color(44, 62, 80));
        
        systemStatusLabel = new JLabel("Готов к работе");
        systemStatusLabel.setForeground(Color.WHITE);
        bottomPanel.add(systemStatusLabel, BorderLayout.WEST);
        
        JLabel javaInfoLabel = new JLabel("Java " + System.getProperty("java.version"));
        javaInfoLabel.setForeground(Color.WHITE);
        bottomPanel.add(javaInfoLabel, BorderLayout.EAST);
        
        mainContainer.add(bottomPanel, BorderLayout.SOUTH);

        add(mainContainer);

        Timer timer = new Timer(1000, e -> updateTimeLabel(timeLabel));
        timer.start();
    }

    private void updateTimeLabel(JLabel timeLabel) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        timeLabel.setText(sdf.format(new Date()));
    }

    private void updateSystemStatus(String status) {
        if (systemStatusLabel != null) {
            systemStatusLabel.setText(status);
        }
        LOGGER.info(status);
    }

    private void updateInstallProgress(String status) {
        currentInstallStep++;
        int progress = (currentInstallStep * 100) / totalInstallSteps;
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
            progressBar.setValue(progress);
            updateSystemStatus("Установка: " + status + " (" + progress + "%)");
        });
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        pathField = new JTextField(installPath != null ? installPath : "");
        pathField.setEditable(false);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        panel.add(pathField, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.0;
        browseButton = new JButton("Обзор");
        browseButton.addActionListener(e -> browseForInstallPath());
        panel.add(browseButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        statusLabel = new JLabel("Выберите папку для установки");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(statusLabel, gbc);

        gbc.gridy = 2;
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        panel.add(progressBar, gbc);

        gbc.gridy = 3;
        installButton = new JButton("Установить");
        installButton.addActionListener(e -> installOrLaunch());
        panel.add(installButton, gbc);

        return panel;
    }

    private JPanel createThemesPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        if (installPath == null) {
            JLabel noInstallLabel = new JLabel("Выберите путь установки", SwingConstants.CENTER);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            panel.add(noInstallLabel, gbc);
            return panel;
        }

        File themesFile = new File(installPath, "content/themes.json");
        File userFile = new File(installPath, "content/user.json");
        
        if (!themesFile.exists()) {
            JLabel noDataLabel = new JLabel("Нет данных", SwingConstants.CENTER);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            panel.add(noDataLabel, gbc);
            return panel;
        }

        try {
            JsonObject themesJson = JsonParser.parseReader(new FileReader(themesFile)).getAsJsonObject();
            Set<String> themeNames = themesJson.keySet();

            String currentTheme = "default";
            if (userFile.exists()) {
                JsonObject userJson = JsonParser.parseReader(new FileReader(userFile)).getAsJsonObject();
                if (userJson.has("current-theme")) {
                    currentTheme = userJson.get("current-theme").getAsString();
                }
            }

            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            themeComboBox = new JComboBox<>(themeNames.toArray(new String[0]));
            themeComboBox.setSelectedItem(currentTheme);
            panel.add(themeComboBox, gbc);

            gbc.gridy = 1;
            gbc.weighty = 1.0;
            gbc.fill = GridBagConstraints.BOTH;
            JPanel previewPanel = new JPanel();
            previewPanel.setPreferredSize(new Dimension(0, 200));
            updateThemePreview(previewPanel, themesJson.getAsJsonObject(currentTheme));
            panel.add(previewPanel, gbc);

            themeComboBox.addActionListener(e -> {
                String selectedTheme = (String) themeComboBox.getSelectedItem();
                if (selectedTheme != null) {
                    updateThemePreview(previewPanel, themesJson.getAsJsonObject(selectedTheme));
                }
            });

            gbc.gridy = 2;
            gbc.weighty = 0.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            JButton applyButton = new JButton("Применить тему");
            applyButton.addActionListener(e -> {
                String selectedTheme = (String) themeComboBox.getSelectedItem();
                if (selectedTheme != null) {
                    applyTheme(selectedTheme);
                }
            });
            panel.add(applyButton, gbc);

        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Ошибка загрузки тем: " + e.getMessage(), SwingConstants.CENTER);
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.weightx = 1.0;
            panel.add(errorLabel, gbc);
        }

        return panel;
    }

    private void updateThemePreview(JPanel previewPanel, JsonObject theme) {
        previewPanel.removeAll();
        previewPanel.setLayout(new BorderLayout());
        
        JsonObject colors = theme.getAsJsonObject("colors");
        Color bgColor = Color.decode(colors.get("background").getAsString());
        Color fgColor = Color.decode(colors.get("foreground").getAsString());
        Color promptColor = Color.decode(colors.get("prompt").getAsString());
        Color usernameColor = Color.decode(colors.get("username").getAsString());
        Color directoryColor = Color.decode(colors.get("directory").getAsString());
        
        JPanel terminal = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(bgColor);
                g.fillRect(0, 0, getWidth(), getHeight());
                
                JsonObject fonts = theme.getAsJsonObject("fonts");
                String fontName = fonts.get("primary").getAsString();
                int fontSize = fonts.get("size").getAsInt();
                Font terminalFont = new Font(fontName, Font.PLAIN, fontSize);
                g.setFont(terminalFont);
                
                int y = 20;
                g.setColor(usernameColor);
                g.drawString("user", 10, y);
                g.setColor(promptColor);
                g.drawString("@", 50, y);
                g.setColor(directoryColor);
                g.drawString("~/documents", 70, y);
                g.setColor(promptColor);
                g.drawString("$ ", 150, y);
                g.setColor(fgColor);
                g.drawString("echo 'Hello, Terminal!'", 165, y);
                
                y += 25;
                g.setColor(fgColor);
                g.drawString("Hello, Terminal!", 10, y);
            }
        };
        
        // Добавляем информацию о теме
        JPanel info = new JPanel(new GridLayout(0, 2, 5, 5));
        info.setBackground(bgColor);
        info.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        addInfoLabel(info, "Версия:", theme.get("version").getAsString(), fgColor, bgColor);
        addInfoLabel(info, "Автор:", theme.get("author").getAsString(), fgColor, bgColor);
        
        previewPanel.add(terminal, BorderLayout.CENTER);
        previewPanel.add(info, BorderLayout.SOUTH);
        previewPanel.revalidate();
        previewPanel.repaint();
    }
    
    private void addInfoLabel(JPanel panel, String name, String value, Color fgColor, Color bgColor) {
        JLabel nameLabel = new JLabel(name);
        nameLabel.setForeground(fgColor);
        nameLabel.setBackground(bgColor);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(fgColor);
        valueLabel.setBackground(bgColor);
        
        panel.add(nameLabel);
        panel.add(valueLabel);
    }

    private void applyTheme(String themeName) {
        if (installPath != null) {
            try {
                // Обновляем user.json
                File userFile = new File(installPath, "content/user.json");
                JsonObject userJson;
                
                if (userFile.exists()) {
                    userJson = JsonParser.parseReader(new FileReader(userFile)).getAsJsonObject();
                } else {
                    userJson = new JsonObject();
                }
                
                userJson.addProperty("current-theme", themeName);
                
                try (FileWriter writer = new FileWriter(userFile)) {
                    new Gson().toJson(userJson, writer);
                }

                JOptionPane.showMessageDialog(this, 
                    "Тема успешно применена", 
                    "Успех", 
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                    "Ошибка при применении темы: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveSettings() {
        if (config == null) {
            config = new Properties();
        }
        config.setProperty("auto_update", String.valueOf(autoUpdateCheckBox.isSelected()));
        config.setProperty("memory", String.valueOf(memorySpinner.getValue()));
        
        try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
            config.store(out, "Terminal Launcher Configuration");
            JOptionPane.showMessageDialog(this,
                "Настройки успешно сохранены",
                "Успех",
                JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                "Ошибка при сохранении настроек: " + e.getMessage(),
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadConfig() {
        config = new Properties();
        try {
            if (Files.exists(Paths.get(CONFIG_FILE))) {
                try (InputStream in = new FileInputStream(CONFIG_FILE)) {
                    config.load(in);
                    installPath = config.getProperty("install_path");
                    if (installPath != null) {
                        checkAndCreateDirectories();
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.warning("Could not load config: " + e.getMessage());
        }
    }

    private void saveConfig() {
        try (OutputStream out = new FileOutputStream(CONFIG_FILE)) {
            config.setProperty("install_path", installPath);
            config.store(out, "Terminal Launcher Configuration");
        } catch (IOException e) {
            LOGGER.warning("Could not save config: " + e.getMessage());
        }
    }

    private void checkAndCreateDirectories() {
        if (installPath == null) return;

        try {
            // Создаем основные директории
            File[] dirs = {
                new File(installPath),
                new File(installPath, "lib"),
                new File(installPath, "content"),
                new File(installPath, "content/plugins"),
                new File(installPath, "logs")
            };

            for (File dir : dirs) {
                if (!dir.exists()) {
                    if (!dir.mkdirs()) {
                        LOGGER.warning("Не удалось создать директорию: " + dir.getPath());
                    } else {
                        LOGGER.info("Создана директория: " + dir.getPath());
                    }
                }
            }

            // Создаем базовые конфигурационные файлы, если их нет
            File themesFile = new File(installPath, "content/themes.json");
            if (!themesFile.exists()) {
                try (FileWriter writer = new FileWriter(themesFile)) {
                    JsonObject defaultTheme = new JsonObject();
                    JsonObject colors = new JsonObject();
                    colors.addProperty("background", "#1E1E1E");
                    colors.addProperty("foreground", "#FFFFFF");
                    colors.addProperty("selection", "#264F78");
                    colors.addProperty("cursor", "#FFFFFF");
                    colors.addProperty("error", "#F44747");
                    colors.addProperty("success", "#6A9955");
                    colors.addProperty("warning", "#CCA700");
                    colors.addProperty("info", "#569CD6");
                    colors.addProperty("username", "#58A6FF");
                    colors.addProperty("directory", "#F6B93B");
                    colors.addProperty("suggestion", "#8B949E");
                    colors.addProperty("prompt", "#7EE787");

                    JsonObject fonts = new JsonObject();
                    fonts.addProperty("primary", "Consolas");
                    fonts.addProperty("size", 14);
                    fonts.addProperty("lineHeight", 1.5);

                    JsonObject spacing = new JsonObject();
                    spacing.addProperty("padding", 8);
                    spacing.addProperty("margin", 4);

                    defaultTheme.add("colors", colors);
                    defaultTheme.add("fonts", fonts);
                    defaultTheme.add("spacing", spacing);
                    defaultTheme.addProperty("version", "1.0");
                    defaultTheme.addProperty("author", "Terminal");

                    JsonObject themes = new JsonObject();
                    themes.add("default", defaultTheme);

                    new Gson().toJson(themes, writer);
                    LOGGER.info("Создан файл тем по умолчанию");
                }
            }

            File userFile = new File(installPath, "content/user.json");
            if (!userFile.exists()) {
                try (FileWriter writer = new FileWriter(userFile)) {
                    JsonObject userConfig = new JsonObject();
                    userConfig.addProperty("current-theme", "default");
                    new Gson().toJson(userConfig, writer);
                    LOGGER.info("Создан файл пользовательских настроек");
                }
            }

        } catch (Exception e) {
            LOGGER.severe("Ошибка при создании директорий: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Ошибка при создании директорий: " + e.getMessage(),
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void browseForInstallPath() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle("Выберите папку для установки");
        
        if (installPath != null) {
            chooser.setCurrentDirectory(new File(installPath));
        }
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            installPath = chooser.getSelectedFile().getAbsolutePath();
            pathField.setText(installPath);
            saveConfig();
            checkAndCreateDirectories();
            checkExistingInstallation();
        }
    }

    private void checkExistingInstallation() {
        if (installPath == null) {
            statusLabel.setText("Выберите папку для установки");
            installButton.setText("Установить");
            installButton.setEnabled(false);
            return;
        }

        Path versionFile = Paths.get(installPath, "lib/project.properties");
        if (Files.exists(versionFile)) {
            try {
                Properties props = new Properties();
                try (InputStream in = new FileInputStream(versionFile.toFile())) {
                    props.load(in);
                    String currentVersion = props.getProperty("version");
                    String launcherVersion = props.getProperty("launcher.version", "1.0.0");
                    
                    if (isUpdateAvailable(currentVersion)) {
                        statusLabel.setText("Доступно обновление");
                        installButton.setText("Обновить");
                    } else {
                        statusLabel.setText(String.format("Терминал: %s | Лаунчер: %s", 
                            currentVersion, launcherVersion));
                        installButton.setText("Запустить");
                    }
                }
            } catch (IOException e) {
                LOGGER.warning("Error checking version: " + e.getMessage());
                statusLabel.setText("Ошибка проверки версии");
            }
        } else {
            statusLabel.setText("Программа не установлена");
            installButton.setText("Установить");
        }
        installButton.setEnabled(true);
    }

    private boolean isUpdateAvailable(String currentVersion) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // Проверяем обновления терминала
            HttpGet request = new HttpGet(GITHUB_API_URL + "/commits/master");
            request.addHeader("Accept", "application/vnd.github.v3+json");
            
            String response = EntityUtils.toString(client.execute(request).getEntity());
            CommitResponse commit = new Gson().fromJson(response, CommitResponse.class);
            String latestCommit = commit.sha.substring(0, 7);
            
            // Проверяем обновления лаунчера
            Properties props = new Properties();
            try (InputStream in = new FileInputStream(new File(installPath, "lib/project.properties"))) {
                props.load(in);
                String launcherVersion = props.getProperty("launcher.version");
                
                // Получаем последнюю версию лаунчера с GitHub
                HttpGet launcherRequest = new HttpGet(GITHUB_RAW_URL + "/lib/project.properties");
                String launcherResponse = EntityUtils.toString(client.execute(launcherRequest).getEntity());
                Properties remoteProps = new Properties();
                remoteProps.load(new StringReader(launcherResponse));
                String latestLauncherVersion = remoteProps.getProperty("launcher.version");
                
                // Возвращаем true если есть обновление терминала или лаунчера
                return !currentVersion.equals(latestCommit) || 
                       (launcherVersion != null && latestLauncherVersion != null && 
                        !launcherVersion.equals(latestLauncherVersion));
            }
        } catch (IOException e) {
            LOGGER.warning("Error checking for updates: " + e.getMessage());
            return false;
        }
    }

    private void installOrLaunch() {
        if (installButton.getText().equals("Запустить")) {
            launchTerminal();
        } else {
            installButton.setEnabled(false);
            browseButton.setEnabled(false);
            progressBar.setVisible(true);
            progressBar.setIndeterminate(true);
            
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    downloadAndInstall();
                    return null;
                }

                @Override
                protected void done() {
                    progressBar.setVisible(false);
                    installButton.setEnabled(true);
                    browseButton.setEnabled(true);
                    checkExistingInstallation();
                }
            }.execute();
        }
    }

    private class ProgressDialog extends JFrame {
        private JProgressBar progressBar;
        private JLabel statusLabel;
        private JLabel detailLabel;

        public ProgressDialog() {
            super("Установка Terminal");
            setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            setSize(400, 150);
            setLocationRelativeTo(Launcher.this);
            setResizable(false);
            
            JPanel panel = new JPanel(new GridBagLayout());
            panel.setBorder(new EmptyBorder(10, 10, 10, 10));
            GridBagConstraints gbc = new GridBagConstraints();
            
            // Статус
            statusLabel = new JLabel("Подготовка к установке...");
            statusLabel.setFont(statusLabel.getFont().deriveFont(Font.BOLD));
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.gridwidth = 2;
            gbc.anchor = GridBagConstraints.WEST;
            gbc.insets = new Insets(0, 0, 5, 0);
            panel.add(statusLabel, gbc);
            
            // Детали
            detailLabel = new JLabel(" ");
            gbc.gridy = 1;
            gbc.insets = new Insets(0, 0, 10, 0);
            panel.add(detailLabel, gbc);
            
            // Прогресс бар
            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            panel.add(progressBar, gbc);
            
            add(panel);
        }
        
        public void setStatus(String status) {
            SwingUtilities.invokeLater(() -> statusLabel.setText(status));
        }
        
        public void setDetail(String detail) {
            SwingUtilities.invokeLater(() -> detailLabel.setText(detail));
        }
        
        public void setProgress(int progress) {
            SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
        }
    }

    private void downloadAndInstall() {
        ProgressDialog progress = new ProgressDialog();
        progress.setVisible(true);
        
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // Шаг 1: Создание директорий
            progress.setStatus("Создание структуры директорий...");
            progress.setProgress(10);
            Files.createDirectories(Paths.get(installPath, "lib"));
            Files.createDirectories(Paths.get(installPath, "content", "plugins"));

            // Шаг 2: Получение информации о версии
            progress.setStatus("Получение информации о версии...");
            progress.setProgress(20);
            HttpGet request = new HttpGet(GITHUB_API_URL + "/commits/master");
            request.addHeader("Accept", "application/vnd.github.v3+json");
            String response = EntityUtils.toString(client.execute(request).getEntity());
            CommitResponse commit = new Gson().fromJson(response, CommitResponse.class);
            String version = commit.sha.substring(0, 7);
            
            // Шаг 3: Скачивание библиотек
            progress.setStatus("Скачивание библиотек...");
            progress.setProgress(30);
            downloadDirectoryContents(client, "lib", Paths.get(installPath, "lib"), progress);
            
            // Шаг 4: Скачивание плагинов
            progress.setStatus("Скачивание плагинов...");
            progress.setProgress(60);
            downloadDirectoryContents(client, "content/plugins", Paths.get(installPath, "content", "plugins"), progress);
            
            // Шаг 5: Завершение установки
            progress.setStatus("Завершение установки...");
            progress.setProgress(90);
            
            // Создание файла версии
            try (FileWriter writer = new FileWriter(new File(installPath, "lib/project.properties"))) {
            Properties versionProps = new Properties();
            versionProps.setProperty("version", version);
                versionProps.setProperty("launcher.version", "1.0.0");
                versionProps.store(writer, "Terminal version information");
            }
            
            progress.setStatus("Установка завершена!");
            progress.setProgress(100);
            Thread.sleep(1000); // Показываем сообщение о завершении на секунду
            
        } catch (Exception e) {
            progress.setStatus("Ошибка установки!");
            progress.setDetail(e.getMessage());
            LOGGER.severe("Installation error: " + e.getMessage());
            try {
                Thread.sleep(3000); // Показываем ошибку на 3 секунды
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        } finally {
            progress.dispose();
        }
    }

    private void downloadDirectoryContents(CloseableHttpClient client, String directory, Path targetDir, ProgressDialog progress) throws IOException {
        HttpGet request = new HttpGet(GITHUB_API_URL + "/contents/" + directory);
        request.addHeader("Accept", "application/vnd.github.v3+json");
        
        String response = EntityUtils.toString(client.execute(request).getEntity());
        GithubContent[] contents = new Gson().fromJson(response, GithubContent[].class);
        
        int totalFiles = contents.length;
        int currentFile = 0;
        
        for (GithubContent content : contents) {
            if ("file".equals(content.type) && content.name.endsWith(".jar")) {
                currentFile++;
                progress.setDetail("Скачивание: " + content.name);
                
                String rawUrl = GITHUB_RAW_URL + "/" + directory + "/" + content.name;
                Path targetPath = targetDir.resolve(content.name);
                
                try (InputStream in = new URL(rawUrl).openStream()) {
                    Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("Downloaded: " + targetPath);
                }
            }
        }
    }

    private void setupCrashHandler() {
        // Создаем файл для логов краша
        File crashDir = new File(installPath, "logs/crashes");
        crashDir.mkdirs();
        
        // Добавляем хук для корректного завершения при выходе
        shutdownHook = new Thread(() -> {
            if (currentProcess != null && currentProcess.isAlive()) {
                currentProcess.destroy();
                try {
                    // Ждем корректного завершения
                    if (!currentProcess.waitFor(5, TimeUnit.SECONDS)) {
                        currentProcess.destroyForcibly();
                    }
                } catch (InterruptedException e) {
                    currentProcess.destroyForcibly();
                }
            }
        });
        Runtime.getRuntime().addShutdownHook(shutdownHook);
    }

    private void logCrash(String errorMessage, Exception e) {
        try {
            File crashDir = new File(installPath, "logs/crashes");
            crashDir.mkdirs();
            
            String timestamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date());
            File crashLog = new File(crashDir, "crash-" + timestamp + ".log");
            
            try (PrintWriter writer = new PrintWriter(new FileWriter(crashLog))) {
                writer.println("Terminal Launcher Crash Report");
                writer.println("============================");
                writer.println("Timestamp: " + timestamp);
                writer.println("Java Version: " + System.getProperty("java.version"));
                writer.println("OS: " + System.getProperty("os.name") + " " + System.getProperty("os.version"));
                writer.println("Working Directory: " + installPath);
                writer.println("\nError Message:");
                writer.println(errorMessage);
                writer.println("\nStack Trace:");
                if (e != null) {
                    e.printStackTrace(writer);
                }
                writer.println("\nSystem Properties:");
                System.getProperties().forEach((k, v) -> writer.println(k + " = " + v));
            }
            
            LOGGER.severe("Crash log created: " + crashLog.getAbsolutePath());
        } catch (Exception ex) {
            LOGGER.severe("Failed to create crash log: " + ex.getMessage());
        }
    }

    private void cleanupProcesses() {
        if (currentProcess != null) {
            try {
                if (currentProcess.isAlive()) {
                    currentProcess.destroy();
                    if (!currentProcess.waitFor(5, TimeUnit.SECONDS)) {
                        currentProcess.destroyForcibly();
                    }
                }
            } catch (Exception e) {
                LOGGER.warning("Error cleaning up process: " + e.getMessage());
            }
        }
        
        if (shutdownHook != null) {
            try {
                Runtime.getRuntime().removeShutdownHook(shutdownHook);
            } catch (IllegalStateException e) {
                // Игнорируем, если JVM уже завершается
            }
        }
    }

    private void launchTerminal() {
        try {
            if (installPath == null || installPath.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Путь установки не выбран или некорректен",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Проверяем и создаем директории перед запуском
            checkAndCreateDirectories();
            setupCrashHandler();

            File installDir = new File(installPath);
            if (!installDir.exists() || !installDir.isDirectory()) {
                JOptionPane.showMessageDialog(this,
                    "Директория установки не существует или некорректна: " + installPath,
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            File libDir = new File(installPath, "lib");
            if (!libDir.exists() || !libDir.isDirectory()) {
                JOptionPane.showMessageDialog(this,
                    "Директория lib не найдена или некорректна",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Проверяем наличие основного JAR файла
            File mainJar = new File(libDir, "terminal.jar");
            if (!mainJar.exists()) {
                JOptionPane.showMessageDialog(this,
                    "Основной JAR файл не найден: terminal.jar",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Собираем classpath
            StringBuilder classPath = new StringBuilder();
            
            // Добавляем основные JAR файлы из lib
            File[] jarFiles = libDir.listFiles((dir, name) -> name != null && name.toLowerCase().endsWith(".jar"));
            if (jarFiles != null && jarFiles.length > 0) {
                for (File jar : jarFiles) {
                    if (jar != null && jar.exists()) {
                        if (classPath.length() > 0) classPath.append(File.pathSeparator);
                        classPath.append(jar.getAbsolutePath());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "В директории lib не найдены JAR файлы",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Добавляем включенные плагины в classpath (опционально)
            File pluginsDir = new File(installPath, "content/plugins");
            if (pluginsDir.exists() && pluginsDir.isDirectory()) {
                File[] pluginFiles = pluginsDir.listFiles((dir, name) -> 
                    name != null && name.toLowerCase().endsWith(".jar") && !name.toLowerCase().endsWith(".jar.disable"));
                if (pluginFiles != null && pluginFiles.length > 0) {
                    LOGGER.info("Найдено " + pluginFiles.length + " плагинов");
                    StringBuilder pluginsPath = new StringBuilder();
                    for (File plugin : pluginFiles) {
                        if (plugin != null && plugin.exists()) {
                            if (pluginsPath.length() > 0) pluginsPath.append(File.pathSeparator);
                            pluginsPath.append(plugin.getAbsolutePath());
                            LOGGER.info("Добавлен плагин в classpath: " + plugin.getName());
                        }
                    }
                    // Добавляем плагины в основной classpath только если они успешно загружены
                    if (pluginsPath.length() > 0) {
                        if (classPath.length() > 0) classPath.append(File.pathSeparator);
                        classPath.append(pluginsPath);
                    }
                } else {
                    LOGGER.info("Активные плагины не найдены");
                }
            } else {
                LOGGER.info("Директория плагинов не найдена или пуста");
            }

            // Проверяем, что основной classpath не пустой (только lib)
            if (classPath.length() == 0) {
                JOptionPane.showMessageDialog(this,
                    "Не удалось сформировать classpath. Проверьте наличие основных JAR файлов в директории lib.",
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Добавляем логирование classpath
            LOGGER.info("Финальный classpath: " + classPath.toString());

            // Проверяем значение памяти
            int memory = memorySpinner != null ? (int)memorySpinner.getValue() : 512;
            if (memory < 256) memory = 256; // Минимальное значение

            // Добавляем логирование classpath
            LOGGER.info("Classpath: " + classPath.toString());

            // Создаем ProcessBuilder с проверками
            ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-Xmx" + memory + "m",
                "-Dapp.name=TerminalLauncher",
                "-Djava.library.path=" + new File(installPath, "lib").getAbsolutePath(),
                "-Dplugins.dir=" + pluginsDir.getAbsolutePath(), // Добавляем путь к директории плагинов
                "-cp",
                classPath.toString(),
                "com.terminal.Main"
            );
            pb.directory(new File(installPath));

            // Проверяем и создаем директорию для логов
            File logsDir = new File(installPath, "logs");
            if (!logsDir.exists()) {
                logsDir.mkdirs();
            }

            // Добавляем логирование
            LOGGER.info("Launching terminal with command: " + String.join(" ", pb.command()));
            LOGGER.info("Working directory: " + pb.directory().getAbsolutePath());

            // Перенаправляем потоки вывода и ошибок процесса в лог-файл
            File logFile = new File(logsDir, "terminal.log");
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(logFile));
            pb.redirectError(ProcessBuilder.Redirect.appendTo(logFile));

            // Запускаем процесс с обработкой ошибок
            try {
                currentProcess = pb.start();
                
                // Создаем поток для чтения вывода процесса
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(currentProcess.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            LOGGER.info("Terminal output: " + line);
                            // Если видим успешную загрузку плагинов и тем, считаем что запуск успешен
                            if (line.contains("PluginManager: Плагин успешно загружен") ||
                                line.contains("EventManager: Подписка на событие")) {
                                return;
                            }
                        }
        } catch (IOException e) {
                        LOGGER.warning("Error reading process output: " + e.getMessage());
                    }
                }).start();

                // Создаем поток для чтения ошибок процесса
                new Thread(() -> {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(currentProcess.getErrorStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            LOGGER.severe("Terminal error: " + line);
                        }
                    } catch (IOException e) {
                        LOGGER.warning("Error reading process error stream: " + e.getMessage());
                    }
                }).start();

                // Ждем достаточное время для инициализации
                Thread.sleep(5000);
                
                if (currentProcess == null || !currentProcess.isAlive()) {
                    int exitCode = currentProcess != null ? currentProcess.exitValue() : -1;
                    String errorMessage = "Процесс завершился с кодом: " + exitCode + "\n" +
                        "Проверьте лог-файл: " + logFile.getAbsolutePath();
                    LOGGER.severe(errorMessage);
                    logCrash(errorMessage, null);
                    cleanupProcesses();
                    
            JOptionPane.showMessageDialog(this,
                        errorMessage + "\n" +
                        "Создан лог краша в директории logs/crashes",
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Процесс успешно запущен и работает
                if (currentProcess.isAlive()) {
                    // Скрываем окно лаунчера вместо закрытия
                    setVisible(false);
                    
                    // Создаем поток для мониторинга процесса
                    new Thread(() -> {
                        try {
                            // Ждем завершения процесса
                            currentProcess.waitFor();
                            // Когда процесс завершился, закрываем лаунчер
                            SwingUtilities.invokeLater(() -> System.exit(0));
                        } catch (InterruptedException e) {
                            LOGGER.warning("Process monitor interrupted: " + e.getMessage());
                        }
                    }).start();
                }
            } catch (Exception e) {
                String errorMessage = "Ошибка при запуске процесса: " + e.getMessage();
                LOGGER.severe(errorMessage);
                logCrash(errorMessage, e);
                cleanupProcesses();
                JOptionPane.showMessageDialog(this,
                    errorMessage + "\nПроверьте лог-файл: " + logFile.getAbsolutePath(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (Exception e) {
            String errorMessage = "Критическая ошибка при запуске: " + e.getMessage() + "\n" +
                "Проверьте, что установлена Java и все необходимые файлы присутствуют.";
            LOGGER.severe(errorMessage);
            logCrash(errorMessage, e);
            cleanupProcesses();
            
            JOptionPane.showMessageDialog(this,
                errorMessage + "\n" +
                "Создан лог краша в директории logs/crashes",
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void disablePlugin(String pluginName) {
        if (!pluginManager.disablePlugin(pluginName)) {
            showError("Ошибка при выключении плагина", "Не удалось корректно отключить плагин " + pluginName);
        }
    }

    private void shutdown() {
        try {
            pluginManager.shutdown();
            // Закрываем все открытые ресурсы
            if (currentProcess != null) {
                currentProcess.destroy();
            }
            // Записываем лог о корректном завершении
            LOGGER.info("Приложение успешно завершено");
        } catch (Exception e) {
            LOGGER.severe("Ошибка при завершении работы: " + e.getMessage());
        } finally {
            System.exit(0);
        }
    }

    private static class CommitResponse {
        @SerializedName("sha")
        private String sha;
    }

    private static class GithubRelease {
        @SerializedName("tag_name")
        private String tagName;
        
        @SerializedName("assets")
        private Asset[] assets;
    }

    private static class Asset {
        @SerializedName("name")
        private String name;
        
        @SerializedName("browser_download_url")
        private String downloadUrl;
    }

    private static class GithubContent {
        @SerializedName("name")
        private String name;
        
        @SerializedName("download_url")
        private String downloadUrl;
        
        @SerializedName("type")
        private String type;
    }

    private JPanel createPluginsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        // Проверяем путь установки
        if (installPath == null) {
            JLabel noInstallLabel = new JLabel("Выберите путь установки", SwingConstants.CENTER);
            panel.add(noInstallLabel, BorderLayout.CENTER);
            return panel;
        }
        
        // Проверяем директорию плагинов
        File pluginsDir = new File(installPath, "content/plugins");
        if (!pluginsDir.exists() || !pluginsDir.isDirectory()) {
            JLabel noDataLabel = new JLabel("Нет данных", SwingConstants.CENTER);
            panel.add(noDataLabel, BorderLayout.CENTER);
            return panel;
        }

        try {
            // Получаем список плагинов
            File[] pluginFiles = pluginsDir.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".jar") || name.toLowerCase().endsWith(".jar.disable"));
            
            if (pluginFiles == null || pluginFiles.length == 0) {
                JLabel noPluginsLabel = new JLabel("Плагины не найдены", SwingConstants.CENTER);
                panel.add(noPluginsLabel, BorderLayout.CENTER);
                return panel;
            }

            // Создаем модель данных для списка плагинов
            DefaultListModel<PluginInfo> pluginListModel = new DefaultListModel<>();
            for (File pluginFile : pluginFiles) {
                String name = pluginFile.getName();
                boolean isEnabled = !name.toLowerCase().endsWith(".disable");
                String baseName = isEnabled ? name : name.substring(0, name.length() - 8); // remove .disable
                
                // Читаем информацию о плагине из plugin.json если есть
                JsonObject pluginInfo = null;
                File infoFile = new File(pluginFile.getParentFile(), baseName + ".json");
                if (infoFile.exists()) {
                    try {
                        pluginInfo = JsonParser.parseReader(new FileReader(infoFile)).getAsJsonObject();
                    } catch (Exception e) {
                        LOGGER.warning("Ошибка чтения информации о плагине " + baseName + ": " + e.getMessage());
                    }
                }
                
                String description = pluginInfo != null && pluginInfo.has("description") ? 
                    pluginInfo.get("description").getAsString() : "Нет описания";
                String version = pluginInfo != null && pluginInfo.has("version") ? 
                    pluginInfo.get("version").getAsString() : "1.0";
                String author = pluginInfo != null && pluginInfo.has("author") ? 
                    pluginInfo.get("author").getAsString() : "Неизвестен";
                
                pluginListModel.addElement(new PluginInfo(baseName, description, version, author, isEnabled));
            }

            // Создаем список с кастомным рендерером
            JList<PluginInfo> pluginsList = new JList<>(pluginListModel);
            pluginsList.setCellRenderer(new PluginListCellRenderer());
            pluginsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            
            // Панель с информацией о выбранном плагине
            JPanel infoPanel = new JPanel(new GridBagLayout());
            infoPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(2, 5, 2, 5);
            
            JLabel nameLabel = new JLabel();
            JLabel versionLabel = new JLabel();
            JLabel authorLabel = new JLabel();
            JTextArea descriptionArea = new JTextArea();
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setLineWrap(true);
            descriptionArea.setEditable(false);
            descriptionArea.setBackground(infoPanel.getBackground());
            
            infoPanel.add(nameLabel, gbc);
            infoPanel.add(versionLabel, gbc);
            infoPanel.add(authorLabel, gbc);
            infoPanel.add(descriptionArea, gbc);
            
            // Обработчик выбора плагина
            pluginsList.addListSelectionListener(e -> {
                PluginInfo selected = pluginsList.getSelectedValue();
                if (selected != null) {
                    nameLabel.setText("Название: " + selected.name);
                    versionLabel.setText("Версия: " + selected.version);
                    authorLabel.setText("Автор: " + selected.author);
                    descriptionArea.setText("Описание: " + selected.description);
                }
            });

            // Панель кнопок
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton toggleButton = new JButton("Включить/Выключить");
            toggleButton.addActionListener(e -> {
                int selectedIndex = pluginsList.getSelectedIndex();
                if (selectedIndex != -1) {
                    PluginInfo plugin = pluginListModel.getElementAt(selectedIndex);
                    File pluginFile = new File(pluginsDir, plugin.name + (plugin.isEnabled ? "" : ".disable") + ".jar");
                    File newFile = new File(pluginsDir, plugin.name + (!plugin.isEnabled ? "" : ".disable") + ".jar");
                    
                    if (pluginFile.renameTo(newFile)) {
                        plugin.isEnabled = !plugin.isEnabled;
                        pluginsList.repaint();
                        JOptionPane.showMessageDialog(this,
                            "Плагин " + (plugin.isEnabled ? "включен" : "выключен"),
                            "Успех",
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Ошибка при " + (plugin.isEnabled ? "выключении" : "включении") + " плагина",
                            "Ошибка",
                            JOptionPane.ERROR_MESSAGE);
                    }
                }
            });
            buttonPanel.add(toggleButton);

            // Добавляем компоненты на панель
            JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(pluginsList), infoPanel);
            splitPane.setDividerLocation(200);
            
            panel.add(splitPane, BorderLayout.CENTER);
            panel.add(buttonPanel, BorderLayout.SOUTH);

        } catch (Exception e) {
            JLabel errorLabel = new JLabel("Ошибка загрузки плагинов: " + e.getMessage(), SwingConstants.CENTER);
            panel.add(errorLabel, BorderLayout.CENTER);
        }

        return panel;
    }

    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Автообновление
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        autoUpdateCheckBox = new JCheckBox("Автоматически проверять обновления");
        autoUpdateCheckBox.setSelected(true);
        panel.add(autoUpdateCheckBox, gbc);

        // Память
        gbc.gridy = 1;
        JPanel memoryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        memoryPanel.add(new JLabel("Выделенная память (MB):"));
        memorySpinner = new JSpinner(new SpinnerNumberModel(512, 256, 4096, 128));
        memoryPanel.add(memorySpinner);
        panel.add(memoryPanel, gbc);

        // Кнопка сохранения
        gbc.gridy = 2;
        JButton saveButton = new JButton("Сохранить настройки");
        saveButton.addActionListener(e -> saveSettings());
        panel.add(saveButton, gbc);

        return panel;
    }

    private void checkForUpdates() {
        if (updateManager != null && autoUpdateCheckBox.isSelected()) {
            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return updateManager.checkForUpdates();
                }
                
                @Override
                protected void done() {
                    try {
                        if (get()) {
                            int response = JOptionPane.showConfirmDialog(
                                Launcher.this,
                                "Доступно обновление. Хотите установить?",
                                "Обновление",
                                JOptionPane.YES_NO_OPTION
                            );
                            
                            if (response == JOptionPane.YES_OPTION) {
                                UpdateDialog dialog = new UpdateDialog(Launcher.this);
                                new SwingWorker<Void, Void>() {
                                    @Override
                                    protected Void doInBackground() throws Exception {
                                        updateManager.performUpdate();
                                        return null;
                                    }
                                }.execute();
                                dialog.setVisible(true);
                                
                                if (!dialog.isCancelled()) {
                                    checkExistingInstallation();
                                }
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.severe("Ошибка при проверке обновлений: " + e.getMessage());
                    }
                }
            }.execute();
        }
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
        LOGGER.severe(title + ": " + message);
    }
} 