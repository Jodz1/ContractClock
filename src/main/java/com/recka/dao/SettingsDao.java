package com.recka.dao;

import com.recka.model.WindowState;
import java.sql.SQLException;
import java.util.Optional;

public interface SettingsDao {
    Optional<String> getSetting(String key) throws SQLException;
    void setSetting(String key, String value) throws SQLException;
    Optional<WindowState> loadWindowState(String windowName) throws SQLException;
    void saveWindowState(WindowState state) throws SQLException;
}
