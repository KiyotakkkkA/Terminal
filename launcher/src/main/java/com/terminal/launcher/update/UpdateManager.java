package com.terminal.launcher.update;

import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.logging.Logger;
import java.util.Properties;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class UpdateManager {
    private static final Logger LOGGER = Logger.getLogger(UpdateManager.class.getName());
    private static final String GITHUB_API_URL = "https://api.github.com/repos/KiyotakkkkA/Terminal";
    private static final String GITHUB_RAW_URL = "https://raw.githubusercontent.com/KiyotakkkkA/Terminal/master";
    
    private String installPath;
    private UpdateCallback callback;

    public UpdateManager(String installPath, UpdateCallback callback) {
        this.installPath = installPath;
        this.callback = callback;
    }

    public boolean checkForUpdates() {
        try {
            Properties localProps = getLocalVersion();
            Properties remoteProps = getRemoteVersion();
            
            String localVersion = localProps.getProperty("version", "0.0.0");
            String remoteVersion = remoteProps.getProperty("version", "0.0.0");
            
            return !localVersion.equals(remoteVersion);
        } catch (Exception e) {
            LOGGER.warning("Ошибка при проверке обновлений: " + e.getMessage());
            return false;
        }
    }

    public void performUpdate() throws IOException {
        callback.onUpdateStart();
        
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // Получаем список файлов для обновления
            HttpGet request = new HttpGet(GITHUB_API_URL + "/contents");
            String response = EntityUtils.toString(client.execute(request).getEntity());
            JsonObject[] files = new Gson().fromJson(response, JsonObject[].class);
            
            int totalFiles = files.length;
            int currentFile = 0;
            
            for (JsonObject file : files) {
                String fileName = file.get("name").getAsString();
                String downloadUrl = file.get("download_url").getAsString();
                
                // Пропускаем директории и ненужные файлы
                if (file.get("type").getAsString().equals("dir") || 
                    fileName.startsWith(".") || 
                    fileName.equals("README.md")) {
                    continue;
                }
                
                currentFile++;
                float progress = (float) currentFile / totalFiles;
                callback.onUpdateProgress(progress, "Обновление: " + fileName);
                
                // Скачиваем и сохраняем файл
                Path targetPath = Paths.get(installPath, fileName);
                try (InputStream in = new URL(downloadUrl).openStream()) {
                    Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            
            // Обновляем версию в локальном файле
            Properties remoteProps = getRemoteVersion();
            try (FileWriter writer = new FileWriter(new File(installPath, "lib/project.properties"))) {
                remoteProps.store(writer, "Terminal version information");
            }
            
            callback.onUpdateComplete();
        } catch (Exception e) {
            LOGGER.severe("Ошибка при обновлении: " + e.getMessage());
            callback.onUpdateError(e.getMessage());
            throw new IOException("Ошибка обновления", e);
        }
    }

    private Properties getLocalVersion() throws IOException {
        Properties props = new Properties();
        File versionFile = new File(installPath, "lib/project.properties");
        if (versionFile.exists()) {
            try (FileInputStream in = new FileInputStream(versionFile)) {
                props.load(in);
            }
        }
        return props;
    }

    private Properties getRemoteVersion() throws IOException {
        Properties props = new Properties();
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(GITHUB_RAW_URL + "/lib/project.properties");
            String response = EntityUtils.toString(client.execute(request).getEntity());
            props.load(new StringReader(response));
        }
        return props;
    }
} 