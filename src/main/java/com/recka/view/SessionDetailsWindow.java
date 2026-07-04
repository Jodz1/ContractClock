package com.recka.view;

import com.recka.AppContext;
import com.recka.model.WorkSession;
import com.recka.model.WorkSessionStatus;
import com.recka.util.DateTimeUtil;
import com.recka.util.MoneyUtil;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class SessionDetailsWindow {
    private final AppContext context;
    private final Stage stage = new Stage();
    private final WorkSession session;
    private final DatePicker date = new DatePicker();
    private final TextField startTime = ViewUtils.text("14:23");
    private final TextField endTime = ViewUtils.text("22:11");
    private final CheckBox chargeable = new CheckBox("Chargeable");
    private final TextField overrideAmount = ViewUtils.text("Override amount");
    private final TextArea description = ViewUtils.area("Brief / description");
    private final Label calculated = ViewUtils.label("Calculated: -", "muted");
    private final Label finalAmount = ViewUtils.label("Final: -", "pill");

    public SessionDetailsWindow(AppContext context, Stage owner, WorkSession session) {
        this.context = context;
        this.session = session;
        stage.setTitle("Recka · Work Session");
        stage.initOwner(owner);
        stage.initModality(Modality.NONE);
        stage.setScene(new Scene(build(), 650, 640));
        stage.getScene().getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        fill();
    }

    private Pane build() {
        Label title = ViewUtils.label("Work Session Details", "app-title");
        Label contract = ViewUtils.label(session.getContractTitle() + " · " + session.getClientName(), "muted");
        GridPane form = ViewUtils.form();
        form.addRow(0, new Label("Date"), date);
        form.addRow(1, new Label("Start"), startTime);
        form.addRow(2, new Label("End"), endTime);
        form.addRow(3, new Label("Rate used"), ViewUtils.label(MoneyUtil.format(session.getHourlyRateSnapshot(), session.getCurrencyCode()) + "/h", "pill"));
        form.addRow(4, new Label("Chargeable"), chargeable);
        form.addRow(5, new Label("Override amount"), overrideAmount);
        form.addRow(6, new Label("Calculated"), calculated);
        form.addRow(7, new Label("Final"), finalAmount);
        form.addRow(8, new Label("Brief"), description);
        GridPane.setHgrow(description, Priority.ALWAYS);

        Button save = ViewUtils.button("Save changes", "primary-button");
        Button exportTxt = ViewUtils.button("Export Brief to TXT", "dark-button");
        Button exportPdf = ViewUtils.button("Export Brief to PDF", "ghost-button");
        save.setOnAction(e -> save());
        exportTxt.setOnAction(e -> export("TXT"));
        exportPdf.setOnAction(e -> export("PDF"));
        HBox buttons = new HBox(10, save, exportTxt, exportPdf);
        VBox root = new VBox(14, title, contract, form, buttons);
        root.setPadding(new Insets(22));
        root.getStyleClass().add("card");
        VBox.setVgrow(description, Priority.ALWAYS);
        return root;
    }

    private void fill() {
        date.setValue(session.getStartTime().toLocalDate());
        startTime.setText(DateTimeUtil.TIME.format(session.getStartTime()));
        endTime.setText(session.getEndTime() == null ? "" : DateTimeUtil.TIME.format(session.getEndTime()));
        chargeable.setSelected(session.isChargeable());
        overrideAmount.setText(session.getOverrideAmount() == null ? "" : session.getOverrideAmount().toPlainString());
        description.setText(session.getDescription());
        calculated.setText("Calculated: " + MoneyUtil.format(session.getCalculatedAmount(), session.getCurrencyCode()));
        finalAmount.setText("Final: " + MoneyUtil.format(session.getFinalAmount(), session.getCurrencyCode()));
    }

    private void save() {
        try {
            if (session.getStatus() == WorkSessionStatus.RUNNING) throw new IllegalStateException("Stop the running timer before editing this session.");
            LocalDate d = date.getValue();
            LocalTime st = ViewUtils.time(startTime.getText());
            LocalTime et = ViewUtils.time(endTime.getText());
            LocalDateTime sdt = LocalDateTime.of(d, st);
            LocalDateTime edt = LocalDateTime.of(d, et);
            if (context.timeTrackerService().hasOverlap(session.getContractId(), sdt, edt, session.getId())) {
                if (!ViewUtils.confirm("Overlap detected", "This time range overlaps with another session. Save anyway?")) return;
            }
            session.setStartTime(sdt);
            session.setEndTime(edt);
            session.setChargeable(chargeable.isSelected());
            session.setOverrideAmount(ViewUtils.decimal(overrideAmount.getText()));
            session.setDescription(description.getText());
            context.timeTrackerService().updateSession(session, true);
            ViewUtils.info("Saved", "Session updated successfully.");
            fill();
        } catch (Exception e) {
            ViewUtils.error("Cannot save session", e);
        }
    }

    private void export(String format) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Choose export folder");
        File dir = chooser.showDialog(stage);
        if (dir == null) return;
        try {
            File file = context.exportService().exportSingle(session.getId(), dir, format);
            ViewUtils.info("Export done", "Saved file:\n" + file.getAbsolutePath());
        } catch (Exception e) {
            ViewUtils.error("Cannot export brief", e);
        }
    }

    public void show() { stage.show(); }
}
