package com.antigravity.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.UUID;

public class ServerConfigService {

  private static final String CONFIG_FILE = "server_config.json";
  private final File configFile;
  private final ObjectMapper mapper;
  private final Config config;

  public ServerConfigService() {
    String appDataDir =
        System.getProperty(
            "app.data.dir", Paths.get(System.getProperty("user.dir"), "app_data").toString());
    this.configFile = Paths.get(appDataDir, CONFIG_FILE).toFile();
    this.mapper = new ObjectMapper();
    this.config = loadConfig();
  }

  private Config loadConfig() {
    if (configFile.exists()) {
      try {
        return mapper.readValue(configFile, Config.class);
      } catch (IOException e) {
        System.err.println("Failed to load server config: " + e.getMessage());
      }
    }
    return new Config();
  }

  private void saveConfig() {
    try {
      mapper.writerWithDefaultPrettyPrinter().writeValue(configFile, config);
    } catch (IOException e) {
      System.err.println("Failed to save server config: " + e.getMessage());
    }
  }

  public String getLastActiveDatabase() {
    return config.lastActiveDatabase;
  }

  public void setLastActiveDatabase(String databaseName) {
    config.lastActiveDatabase = databaseName;
    saveConfig();
  }

  public boolean isShareAnalyticsEnabled() {
    return config.shareAnalytics;
  }

  public void setShareAnalyticsEnabled(boolean enabled) {
    config.shareAnalytics = enabled;
    saveConfig();
  }

  public String getAnalyticsClientId() {
    if (config.analyticsClientId == null || config.analyticsClientId.isEmpty()) {
      config.analyticsClientId = "rc-desktop-" + UUID.randomUUID().toString();
      saveConfig();
    }
    return config.analyticsClientId;
  }

  private static class Config {

    public String lastActiveDatabase;
    public boolean shareAnalytics = true;
    public String analyticsClientId;
  }
}
