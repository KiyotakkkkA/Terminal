package com.terminal.launcher;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class Launcher extends JFrame {
    private static final Logger LOGGER = Logger.getLogger(Launcher.class.getName());
    private static final String GITHUB_API_URL = "https://api.github.com/repos/KiyotakkkkA/Terminal/releases";
    private static final String VERSION_FILE = "version.properties";
    private static final String CONFIG_FILE = "launcher.properties";
    
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
    
    private JTextField pathField;
    private JButton browseButton;
    private JButton installButton;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
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

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 200);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Path field
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        pathField = new JTextField(installPath != null ? installPath : "");
        pathField.setEditable(false);
        mainPanel.add(pathField, gbc);

        // Browse button
        gbc.gridx = 1;
        gbc.weightx = 0.0;
        browseButton = new JButton("Обзор");
        browseButton.addActionListener(e -> browseForInstallPath());
        mainPanel.add(browseButton, gbc);

        // Status label
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        statusLabel = new JLabel("Выберите папку для установки");
        mainPanel.add(statusLabel, gbc);

        // Progress bar
        gbc.gridy = 2;
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setVisible(false);
        mainPanel.add(progressBar, gbc);

        // Install/Launch button
        gbc.gridy = 3;
        installButton = new JButton("Установить");
        installButton.addActionListener(e -> installOrLaunch());
        mainPanel.add(installButton, gbc);

        add(mainPanel);
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
            HttpGet request = new HttpGet(GITHUB_API_URL);
            request.addHeader("Accept", "application/vnd.github.v3+json");
            
            String response = EntityUtils.toString(client.execute(request).getEntity());
            GithubRelease[] releases = new Gson().fromJson(response, GithubRelease[].class);
            
            if (releases.length > 0) {
                // Берем первый релиз из списка (самый новый)
                return !currentVersion.equals(releases[0].tagName);
            }
            return false;
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
            HttpGet request = new HttpGet(GITHUB_API_URL);
            request.addHeader("Accept", "application/vnd.github.v3+json");
            
            String response = EntityUtils.toString(client.execute(request).getEntity());
            GithubRelease[] releases = new Gson().fromJson(response, GithubRelease[].class);
            
            if (releases.length > 0) {
                // Создаем необходимые директории
                Files.createDirectories(Paths.get(installPath, "lib"));
                Files.createDirectories(Paths.get(installPath, "content", "plugins"));
                
                // Скачиваем основные JAR файлы
                for (Asset asset : releases[0].assets) {
                    String fileName = asset.name.toLowerCase();
                    Path targetPath;
                    
                    if (fileName.startsWith("terminal") && !fileName.contains("sdk")) {
                        targetPath = Paths.get(installPath, "lib", "terminal.jar");
                    } else if (fileName.contains("sdk")) {
                        targetPath = Paths.get(installPath, "lib", "terminal-sdk.jar");
                    } else if (fileName.endsWith(".jar")) {
                        targetPath = Paths.get(installPath, "content", "plugins", asset.name);
                    } else {
                        continue;
                    }
                    
                    // Скачиваем файл
                    try (InputStream in = new URL(asset.downloadUrl).openStream()) {
                        Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        LOGGER.info("Downloaded: " + targetPath);
                    }
                }
                
                // Создаем version file
                Properties versionProps = new Properties();
                versionProps.setProperty("version", releases[0].tagName);
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
            }
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

    private void launchTerminal() {
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "-cp", 
                "lib/*", "com.terminal.Main");
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
} 