package com.recka.view;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public final class ViewUtils {
    private ViewUtils() {}

    public static Button button(String text, String styleClass) {
        Button b = new Button(text);
        b.getStyleClass().add(styleClass);
        return b;
    }

    public static Label label(String text, String styleClass) {
        Label l = new Label(text);
        if (styleClass != null) l.getStyleClass().add(styleClass);
        return l;
    }

    public static VBox metric(String label, String value) {
        Label v = label(value, "metric-value");
        Label l = label(label, "metric-label");
        VBox box = new VBox(4, v, l);
        box.getStyleClass().add("card");
        box.setMinWidth(150);
        return box;
    }

    public static TextField text(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        return tf;
    }

    public static TextArea area(String prompt) {
        TextArea ta = new TextArea();
        ta.setPromptText(prompt);
        ta.setWrapText(true);
        ta.setPrefRowCount(4);
        return ta;
    }

    public static GridPane form() {
        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(10);
        g.setPadding(new Insets(10));
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setMinWidth(120);
        ColumnConstraints c2 = new ColumnConstraints();
        c2.setHgrow(Priority.ALWAYS);
        g.getColumnConstraints().addAll(c1, c2);
        return g;
    }

    public static void error(String title, Throwable e) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(e.getMessage() == null ? e.toString() : e.getMessage());
        a.showAndWait();
    }

    public static void info(String title, String text) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(title);
        a.setContentText(text);
        a.showAndWait();
    }

    public static boolean confirm(String title, String text) {
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, text, ButtonType.CANCEL, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(title);
        return a.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    public static BigDecimal decimal(String value) {
        if (value == null || value.isBlank()) return null;
        return new BigDecimal(value.trim().replace(',', '.'));
    }

    public static LocalTime time(String value) {
        return LocalTime.parse(value.trim(), DateTimeFormatter.ofPattern("H:mm"));
    }
}
