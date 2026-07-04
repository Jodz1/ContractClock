package com.recka.service;

import com.recka.dao.SettingsDao;
import java.sql.SQLException;

/** Singleton pattern: central application settings accessor. */
public final class SettingsManager {
    private static SettingsManager instance;
    private final SettingsDao settingsDao;

    private SettingsManager(SettingsDao settingsDao) {
        this.settingsDao = settingsDao;
    }

    public static synchronized SettingsManager init(SettingsDao settingsDao) {
        if (instance == null) instance = new SettingsManager(settingsDao);
        return instance;
    }

    public static SettingsManager getInstance() {
        if (instance == null) throw new IllegalStateException("SettingsManager not initialized.");
        return instance;
    }

    public String get(String key, String fallback) {
        try {
            return settingsDao.getSetting(key).orElse(fallback);
        } catch (SQLException e) {
            return fallback;
        }
    }

    public void set(String key, String value) {
        try {
            settingsDao.setSetting(key, value);
        } catch (SQLException ignored) {
        }
    }
}
