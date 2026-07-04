package com.recka.memento;

import com.recka.dao.SettingsDao;
import com.recka.model.WindowState;
import javafx.stage.Stage;
import java.sql.SQLException;

/** Memento pattern: captures and restores window geometry/state through the database. */
public class WindowStateManager {
    private final SettingsDao settingsDao;

    public WindowStateManager(SettingsDao settingsDao) {
        this.settingsDao = settingsDao;
    }

    public void restore(Stage stage, String windowName, double defaultW, double defaultH) {
        try {
            settingsDao.loadWindowState(windowName).ifPresentOrElse(s -> {
                if (s.getWidth() != null && s.getHeight() != null) {
                    stage.setWidth(s.getWidth());
                    stage.setHeight(s.getHeight());
                } else {
                    stage.setWidth(defaultW);
                    stage.setHeight(defaultH);
                }
                if (s.getX() != null) stage.setX(s.getX());
                if (s.getY() != null) stage.setY(s.getY());
                stage.setMaximized(s.isMaximized());
            }, () -> {
                stage.setWidth(defaultW);
                stage.setHeight(defaultH);
            });
        } catch (SQLException ignored) {
            stage.setWidth(defaultW);
            stage.setHeight(defaultH);
        }
    }

    public void save(Stage stage, String windowName, Long lastOpenedContractId) {
        try {
            WindowState s = new WindowState();
            s.setWindowName(windowName);
            s.setX(stage.getX());
            s.setY(stage.getY());
            s.setWidth(stage.getWidth());
            s.setHeight(stage.getHeight());
            s.setMaximized(stage.isMaximized());
            s.setLastOpenedContractId(lastOpenedContractId);
            settingsDao.saveWindowState(s);
        } catch (SQLException ignored) {
            // Window state must not break the main workflow.
        }
    }
}
