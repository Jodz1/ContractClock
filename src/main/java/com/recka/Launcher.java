package com.recka;

/**
 * Plain Java launcher used by Maven exec/classpath runs.
 *
 * MainApp extends javafx.application.Application. When a JavaFX Application
 * class is launched directly from a plain jar, some JDKs print:
 * "JavaFX runtime components are missing".
 *
 * This wrapper avoids that launcher check and delegates to the real JavaFX app.
 */
public final class Launcher {
    private Launcher() {
    }

    public static void main(String[] args) {
        MainApp.main(args);
    }
}
