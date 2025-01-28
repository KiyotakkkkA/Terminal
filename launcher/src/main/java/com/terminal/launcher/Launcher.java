package com.terminal.launcher;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import java.util.logging.Logger;

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
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class Launcher extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(Launcher.class.getName());
    private static final String GITHUB_API_URL = "https://api.github.com/repos/KiyotakkkkA/Terminal";
    private static final String GITHUB_RAW_URL = "https://raw.githubusercontent.com/KiyotakkkkA/Terminal/master";
    private static final String VERSION_FILE = "version.properties";
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
        loadConfig();
        initializeUI();
        checkExistingInstallation();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
        setResizable(false);

        tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Главная панель
        mainPanel = createMainPanel();
        tabbedPane.addTab("Главная", new ImageIcon("icons/home.png"), mainPanel);

        // Панель тем
        themesPanel = createThemesPanel();
        tabbedPane.addTab("Темы", new ImageIcon("icons/themes.png"), themesPanel);

        // Панель плагинов
        pluginsPanel = createPluginsPanel();
        tabbedPane.addTab("Плагины", new ImageIcon("icons/plugins.png"), pluginsPanel);

        // Панель настроек
        settingsPanel = createSettingsPanel();
        tabbedPane.addTab("Настройки", new ImageIcon("icons/settings.png"), settingsPanel);

        add(tabbedPane);
    }

    private JPanel createMainPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Путь установки
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        pathField = new JTextField(installPath != null ? installPath : "");
        pathField.setEditable(false);
        panel.add(pathField, gbc);

        // Кнопка обзора
        gbc.gridx = 1;
        gbc.weightx = 0.0;
        browseButton = new JButton("Обзор");
        browseButton.addActionListener(e -> browseForInstallPath());
        panel.add(browseButton, gbc);

        // Статус
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        statusLabel = new JLabel("Выберите папку для установки");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(statusLabel, gbc);

        // Прогресс
        gbc.gridy = 2;
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        panel.add(progressBar, gbc);

        // Кнопка установки/запуска
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

        // Список тем
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        themeComboBox = new JComboBox<>(new String[]{"Default", "Dark", "Light", "Monokai", "Solarized"});
        panel.add(themeComboBox, gbc);

        // Кнопка применения темы
        gbc.gridy = 1;
        JButton applyThemeButton = new JButton("Применить тему");
        applyThemeButton.addActionListener(e -> applyTheme((String)themeComboBox.getSelectedItem()));
        panel.add(applyThemeButton, gbc);

        return panel;
    }

    private JPanel createPluginsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Список плагинов
        JList<String> pluginsList = new JList<>(new String[]{
            "Notes - Создание и управление заметками",
            "STheme - Управление темами оформления",
            "FileManager - Расширенный файловый менеджер",
            "NetworkTools - Дополнительные сетевые инструменты"
        });
        JScrollPane scrollPane = new JScrollPane(pluginsList);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Панель кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton installPluginButton = new JButton("Установить плагин");
        JButton removePluginButton = new JButton("Удалить плагин");
        buttonPanel.add(installPluginButton);
        buttonPanel.add(removePluginButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

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

    private void applyTheme(String themeName) {
        if (installPath != null) {
            try {
                Properties props = new Properties();
                props.setProperty("theme", themeName);
                File themeConfig = new File(installPath, "content/themes.json");
                themeConfig.getParentFile().mkdirs();
                try (FileWriter writer = new FileWriter(themeConfig)) {
                    writer.write(String.format("{\n  \"current_theme\": \"%s\"\n}", themeName));
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

        Path versionFile = Paths.get(installPath, VERSION_FILE);
        if (Files.exists(versionFile)) {
            try {
                Properties props = new Properties();
                try (InputStream in = new FileInputStream(versionFile.toFile())) {
                    props.load(in);
                    String currentVersion = props.getProperty("version");
                    if (isUpdateAvailable(currentVersion)) {
                        statusLabel.setText("Доступно обновление");
                        installButton.setText("Обновить");
                    } else {
                        statusLabel.setText("Установленная версия: " + currentVersion);
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
            HttpGet request = new HttpGet(GITHUB_API_URL + "/commits/master");
            request.addHeader("Accept", "application/vnd.github.v3+json");
            
            String response = EntityUtils.toString(client.execute(request).getEntity());
            CommitResponse commit = new Gson().fromJson(response, CommitResponse.class);
            String latestCommit = commit.sha.substring(0, 7);
            
            return !currentVersion.equals(latestCommit);
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

    private void downloadAndInstall() {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // Создаем необходимые директории
            Files.createDirectories(Paths.get(installPath, "lib"));
            Files.createDirectories(Paths.get(installPath, "content", "plugins"));

            // Получаем информацию о последнем коммите для версии
            HttpGet request = new HttpGet(GITHUB_API_URL + "/commits/master");
            request.addHeader("Accept", "application/vnd.github.v3+json");
            String response = EntityUtils.toString(client.execute(request).getEntity());
            CommitResponse commit = new Gson().fromJson(response, CommitResponse.class);
            String version = commit.sha.substring(0, 7); // Используем первые 7 символов SHA коммита

            // Скачиваем файлы из lib
            downloadDirectoryContents(client, "lib", Paths.get(installPath, "lib"));
            
            // Скачиваем файлы из content/plugins
            downloadDirectoryContents(client, "content/plugins", Paths.get(installPath, "content", "plugins"));

            // Создаем version file
            Properties versionProps = new Properties();
            versionProps.setProperty("version", version);
            try (OutputStream out = new FileOutputStream(new File(installPath, VERSION_FILE))) {
                versionProps.store(out, "Terminal Version");
            }

            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Установка завершена");
                JOptionPane.showMessageDialog(this,
                    "Установка успешно завершена!",
                    "Установка завершена",
                    JOptionPane.INFORMATION_MESSAGE);
            });
        } catch (IOException e) {
            LOGGER.severe("Error during installation: " + e.getMessage());
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Ошибка установки");
                JOptionPane.showMessageDialog(this,
                    "Ошибка при установке: " + e.getMessage(),
                    "Ошибка",
                    JOptionPane.ERROR_MESSAGE);
            });
        }
    }

    private void downloadDirectoryContents(CloseableHttpClient client, String directory, Path targetDir) throws IOException {
        HttpGet request = new HttpGet(GITHUB_API_URL + "/contents/" + directory);
        request.addHeader("Accept", "application/vnd.github.v3+json");
        
        String response = EntityUtils.toString(client.execute(request).getEntity());
        GithubContent[] contents = new Gson().fromJson(response, GithubContent[].class);
        
        for (GithubContent content : contents) {
            if ("file".equals(content.type) && content.name.endsWith(".jar")) {
                // Скачиваем файл напрямую из raw URL
                String rawUrl = GITHUB_RAW_URL + "/" + directory + "/" + content.name;
                Path targetPath = targetDir.resolve(content.name);
                
                try (InputStream in = new URL(rawUrl).openStream()) {
                    Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    LOGGER.info("Downloaded: " + targetPath);
                }
            }
        }
    }

    private void launchTerminal() {
        try {
            int memory = (int)memorySpinner.getValue();
            ProcessBuilder pb = new ProcessBuilder(
                "java",
                "-Xmx" + memory + "m",
                "-cp",
                "lib/*",
                "com.terminal.Main"
            );
            pb.directory(new File(installPath));
            pb.start();
            System.exit(0);
        } catch (IOException e) {
            LOGGER.severe("Error launching terminal: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                "Ошибка при запуске: " + e.getMessage(),
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
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
} 