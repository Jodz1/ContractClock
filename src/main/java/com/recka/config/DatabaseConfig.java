package com.recka.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class DatabaseConfig {
    private final Properties properties = new Properties();

    public DatabaseConfig() {
        try (InputStream in = getClass().getResourceAsStream("/application.properties")) {
            if (in != null) properties.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot load application.properties", e);
        }
        Path userOverride = Path.of(System.getProperty("user.home"), ".recka", "application.properties");
        Path legacyOverride = Path.of(System.getProperty("user.home"), ".contractclock", "application.properties");
        Path overrideToLoad = Files.exists(userOverride) ? userOverride : legacyOverride;
        if (Files.exists(overrideToLoad)) {
            try (InputStream in = Files.newInputStream(overrideToLoad)) {
                properties.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot load local override: " + overrideToLoad, e);
            }
        }
    }

    public String getUrl() { return properties.getProperty("db.url"); }
    public String getUsername() { return firstNonBlank(System.getenv("RECKA_DB_USER"), firstNonBlank(System.getenv("CONTRACTCLOCK_DB_USER"), properties.getProperty("db.username"))); }
    public String getPassword() { return firstNonBlank(System.getenv("RECKA_DB_PASSWORD"), firstNonBlank(System.getenv("CONTRACTCLOCK_DB_PASSWORD"), properties.getProperty("db.password", ""))); }
    public String getDefaultExportFolder() { return properties.getProperty("app.defaultExportFolder", "exports"); }
    public String getDefaultCurrency() { return properties.getProperty("app.defaultCurrency", "USD"); }
    public boolean isAutoInitialize() { return Boolean.parseBoolean(properties.getProperty("db.autoInitialize", "false")); }
    public String get(String key, String fallback) { return properties.getProperty(key, fallback); }

    public String getDatabaseName() {
        String url = getUrl();
        int prefix = url.indexOf("jdbc:mysql://");
        if (prefix < 0) return null;
        int slash = url.indexOf('/', "jdbc:mysql://".length());
        if (slash < 0) return null;
        int end = url.indexOf('?', slash + 1);
        String name = end >= 0 ? url.substring(slash + 1, end) : url.substring(slash + 1);
        return name.isBlank() ? null : name;
    }

    public String getServerUrl() {
        String url = getUrl();
        int slash = url.indexOf('/', "jdbc:mysql://".length());
        if (slash < 0) return url;
        int query = url.indexOf('?', slash + 1);
        String options = query >= 0 ? url.substring(query) : "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        return url.substring(0, slash + 1) + options;
    }

    private String firstNonBlank(String first, String second) {
        return first != null && !first.isBlank() ? first : second;
    }
}
