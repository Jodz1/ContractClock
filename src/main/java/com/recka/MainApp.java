package com.recka;

import com.recka.db.DatabaseConnectionManager;
import com.recka.view.DashboardView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class MainApp extends Application {
    private AppContext context;
    private DashboardView dashboardView;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Recka");
        try {
            DatabaseConnectionManager.getInstance().testConnection();
            context = new AppContext();
            dashboardView = new DashboardView(context, primaryStage);
            Scene scene = new Scene(dashboardView, 1280, 780);
            scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
            primaryStage.setScene(scene);
            context.windowStateManager().restore(primaryStage, "main", 1280, 780);
            primaryStage.setOnCloseRequest(e -> context.windowStateManager().save(primaryStage, "main", null));
            primaryStage.show();
            Platform.runLater(() -> dashboardView.recoverIfNeeded());
        } catch (Exception e) {
            showDatabaseError(primaryStage, e);
        }
    }

    private void showDatabaseError(Stage stage, Exception e) {
        Label title = new Label("Recka cannot connect to MySQL");
        title.getStyleClass().add("app-title");
        TextArea details = new TextArea("Error: " + e.getMessage() + "\n\n" +
                "Fix steps:\n" +
                "1. Start MySQL Server.\n" +
                "2. Check db.url, db.username and db.password in application.properties or ~/.recka/application.properties.\n" +
                "3. If you already have important data, do not run schema.sql or seed.sql.\n" +
                "4. Run: mvn clean javafx:run");
        details.setWrapText(true);
        details.setEditable(false);
        Button quit = new Button("Quit");
        quit.getStyleClass().add("danger-button");
        quit.setOnAction(ev -> Platform.exit());
        VBox root = new VBox(16, title, details, quit);
        root.setPadding(new Insets(26));
        Scene scene = new Scene(root, 760, 420);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
