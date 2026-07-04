package com.recka.db;

import com.recka.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Singleton pattern: one database connection manager for the whole application. */
public final class DatabaseConnectionManager {
    private static DatabaseConnectionManager instance;
    private final DatabaseConfig config;

    private DatabaseConnectionManager() {
        this.config = new DatabaseConfig();
    }

    public static synchronized DatabaseConnectionManager getInstance() {
        if (instance == null) instance = new DatabaseConnectionManager();
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
    }

    public void testConnection() throws SQLException {
        new DatabaseInitializer(config).initializeIfEnabled();
        try (Connection ignored = getConnection()) {
            // Only tests that the connection can be opened.
        }
    }

    public DatabaseConfig getConfig() { return config; }
}
