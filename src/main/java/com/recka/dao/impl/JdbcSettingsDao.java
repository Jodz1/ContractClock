package com.recka.dao.impl;

import com.recka.dao.SettingsDao;
import com.recka.model.WindowState;
import java.sql.*;
import java.util.Optional;

public class JdbcSettingsDao extends JdbcSupport implements SettingsDao {
    @Override
    public Optional<String> getSetting(String key) throws SQLException {
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement("SELECT setting_value FROM app_settings WHERE setting_key=?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.ofNullable(rs.getString(1));
                return Optional.empty();
            }
        }
    }

    @Override
    public void setSetting(String key, String value) throws SQLException {
        String sql = "INSERT INTO app_settings(setting_key, setting_value) VALUES(?,?) ON DUPLICATE KEY UPDATE setting_value=VALUES(setting_value)";
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<WindowState> loadWindowState(String windowName) throws SQLException {
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM window_state WHERE window_name=?")) {
            ps.setString(1, windowName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    WindowState s = new WindowState();
                    s.setId(rs.getLong("id"));
                    s.setWindowName(rs.getString("window_name"));
                    s.setX((Double)rs.getObject("x"));
                    s.setY((Double)rs.getObject("y"));
                    s.setWidth((Double)rs.getObject("width"));
                    s.setHeight((Double)rs.getObject("height"));
                    s.setMaximized(rs.getBoolean("maximized"));
                    Object last = rs.getObject("last_opened_contract_id");
                    s.setLastOpenedContractId(last == null ? null : ((Number) last).longValue());
                    return Optional.of(s);
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public void saveWindowState(WindowState s) throws SQLException {
        String sql = """
                INSERT INTO window_state(window_name, x, y, width, height, maximized, last_opened_contract_id)
                VALUES(?,?,?,?,?,?,?)
                ON DUPLICATE KEY UPDATE x=VALUES(x), y=VALUES(y), width=VALUES(width), height=VALUES(height),
                maximized=VALUES(maximized), last_opened_contract_id=VALUES(last_opened_contract_id)
                """;
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getWindowName());
            ps.setObject(2, s.getX());
            ps.setObject(3, s.getY());
            ps.setObject(4, s.getWidth());
            ps.setObject(5, s.getHeight());
            ps.setBoolean(6, s.isMaximized());
            ps.setObject(7, s.getLastOpenedContractId());
            ps.executeUpdate();
        }
    }
}
