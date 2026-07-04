package com.recka.db;

import com.recka.config.DatabaseConfig;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Small JDBC bootstrap helper.
 * It lets the desktop app create the MySQL database/tables automatically when MySQL Server is running,
 * so the user does not have to open MySQL Workbench just to run schema.sql manually.
 */
public final class DatabaseInitializer {
    private final DatabaseConfig config;

    public DatabaseInitializer(DatabaseConfig config) {
        this.config = config;
    }

    public void initializeIfEnabled() throws SQLException {
        if (!config.isAutoInitialize()) return;
        String databaseName = config.getDatabaseName();
        if (databaseName == null || databaseName.isBlank()) return;

        createDatabaseIfMissing(databaseName);
        try (Connection connection = DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword())) {
            if (!tableExists(connection, "clients")) {
                executeScript(connection, "/database/schema.sql");
                executeScript(connection, "/database/seed.sql");
            }
        }
    }

    private void createDatabaseIfMissing(String databaseName) throws SQLException {
        try (Connection connection = DriverManager.getConnection(config.getServerUrl(), config.getUsername(), config.getPassword());
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + escapeIdentifier(databaseName) + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
        }
    }

    private boolean tableExists(Connection connection, String tableName) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet rs = metaData.getTables(connection.getCatalog(), null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    private void executeScript(Connection connection, String resourcePath) throws SQLException {
        List<String> statements = loadSqlStatements(resourcePath);
        try (Statement statement = connection.createStatement()) {
            for (String sql : statements) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) statement.execute(trimmed);
            }
        }
    }

    private List<String> loadSqlStatements(String resourcePath) throws SQLException {
        try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
            if (in == null) throw new SQLException("Missing SQL resource: " + resourcePath);
            StringBuilder current = new StringBuilder();
            List<String> statements = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty() || trimmed.startsWith("--") || trimmed.startsWith("#")) continue;
                    current.append(line).append('\n');
                    if (trimmed.endsWith(";")) {
                        int lastSemicolon = current.lastIndexOf(";");
                        statements.add(current.substring(0, lastSemicolon));
                        current.setLength(0);
                    }
                }
                if (!current.toString().trim().isEmpty()) statements.add(current.toString());
            }
            return statements;
        } catch (IOException e) {
            throw new SQLException("Cannot load SQL resource: " + resourcePath, e);
        }
    }

    private String escapeIdentifier(String value) {
        return value.replace("`", "``");
    }
}
