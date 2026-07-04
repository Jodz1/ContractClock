package com.recka.view;

import com.recka.AppContext;
import com.recka.command.StartTimerCommand;
import com.recka.command.StopTimerCommand;
import com.recka.model.DashboardContractRow;
import com.recka.model.WorkSession;
import com.recka.util.DateTimeUtil;
import com.recka.util.MoneyUtil;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public class TrackerWindow {
    private final AppContext context;
    private final Stage stage = new Stage();
    private final ComboBox<DashboardContractRow> contracts = new ComboBox<>();
    private final Label rateLabel = ViewUtils.label("-", "pill");
    private final Label timerLabel = ViewUtils.label("0:00:00", "timer-label");
    private final Label todayLabel = ViewUtils.label("Today: -", "muted");
    private final Label weekLabel = ViewUtils.label("This week: -", "muted");
    private final TextArea memo = ViewUtils.area("What are you working on?");
    private final Button start = ViewUtils.button("Start", "primary-button");
    private final Button stop = ViewUtils.button("Stop", "danger-button");
    private WorkSession running;
    private Timeline timeline;

    public TrackerWindow(AppContext context, Stage owner, Optional<DashboardContractRow> selected) {
        this.context = context;
        stage.setTitle("Recka · Time Tracker");
        stage.initOwner(owner);
        stage.initModality(Modality.NONE);
        VBox root = build();
        Scene scene = new Scene(root, 430, 560);
        scene.getStylesheets().add(getClass().getResource("/css/app.css").toExternalForm());
        stage.setScene(scene);
        context.windowStateManager().restore(stage, "tracker", 430, 560);
        stage.setOnCloseRequest(e -> {
            if (running != null) {
                boolean close = ViewUtils.confirm("Timer is running", "A timer is currently running. Close the tracker window anyway?");
                if (!close) e.consume();
            }
            context.windowStateManager().save(stage, "tracker", contracts.getValue() == null ? null : contracts.getValue().getContractId());
        });
        loadContracts(selected);
        detectRunning();
    }

    private VBox build() {
        Label title = ViewUtils.label("Time Tracker", "app-title");
        contracts.setMaxWidth(Double.MAX_VALUE);
        contracts.setOnAction(e -> refreshStats());
        HBox rateBox = new HBox(8, ViewUtils.label("Current rate", "muted"), rateLabel);
        stop.setDisable(true);
        start.setOnAction(e -> startTimer());
        stop.setOnAction(e -> stopTimer());
        HBox buttons = new HBox(10, start, stop);
        buttons.setFillHeight(true);
        HBox.setHgrow(start, Priority.ALWAYS);
        HBox.setHgrow(stop, Priority.ALWAYS);
        start.setMaxWidth(Double.MAX_VALUE);
        stop.setMaxWidth(Double.MAX_VALUE);
        VBox root = new VBox(16, title, contracts, rateBox, timerLabel, todayLabel, weekLabel, memo, buttons);
        root.setPadding(new Insets(22));
        root.getStyleClass().add("card");
        VBox.setVgrow(memo, Priority.ALWAYS);
        return root;
    }

    private void loadContracts(Optional<DashboardContractRow> selected) {
        try {
            ObservableList<DashboardContractRow> list = FXCollections.observableArrayList(context.contractService().dashboard(""));
            contracts.setItems(list.filtered(r -> r.getStatus() != com.recka.model.ContractStatus.ARCHIVED));
            selected.ifPresent(s -> contracts.getSelectionModel().select(list.stream().filter(x -> x.getContractId().equals(s.getContractId())).findFirst().orElse(null)));
            if (contracts.getValue() == null && !contracts.getItems().isEmpty()) contracts.getSelectionModel().selectFirst();
            refreshStats();
        } catch (Exception e) {
            ViewUtils.error("Cannot load contracts", e);
        }
    }

    private void detectRunning() {
        try {
            context.timeTrackerService().runningSession().ifPresent(s -> {
                running = s;
                memo.setText(s.getDescription());
                contracts.getItems().stream().filter(c -> c.getContractId().equals(s.getContractId())).findFirst().ifPresent(contracts::setValue);
                start.setDisable(true);
                stop.setDisable(false);
                startTimeline();
            });
        } catch (Exception e) {
            ViewUtils.error("Cannot detect running session", e);
        }
    }

    private void refreshStats() {
        DashboardContractRow row = contracts.getValue();
        if (row == null) return;
        rateLabel.setText(MoneyUtil.format(row.getCurrentHourlyRate(), row.getCurrencyCode()) + "/h");
        try {
            todayLabel.setText("Today: " + DateTimeUtil.formatDuration(context.timeTrackerService().trackedToday(row.getContractId())));
            weekLabel.setText("This week: " + DateTimeUtil.formatDuration(context.timeTrackerService().trackedThisWeek(row.getContractId())));
        } catch (Exception ignored) {
        }
    }

    private void startTimer() {
        DashboardContractRow row = contracts.getValue();
        if (row == null) return;
        try {
            new StartTimerCommand(context.timeTrackerService(), row.getContractId(), memo.getText(), s -> {
                running = s;
                start.setDisable(true);
                stop.setDisable(false);
                startTimeline();
            }).execute();
        } catch (Exception e) {
            ViewUtils.error("Cannot start timer", e);
        }
    }

    private void stopTimer() {
        if (running == null) return;
        try {
            new StopTimerCommand(context.timeTrackerService(), running.getId(), memo.getText(), s -> {
                running = null;
                stopTimeline();
                timerLabel.setText("0:00:00");
                start.setDisable(false);
                stop.setDisable(true);
                refreshStats();
            }).execute();
        } catch (Exception e) {
            ViewUtils.error("Cannot stop timer", e);
        }
    }

    private void startTimeline() {
        stopTimeline();
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimer()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        updateTimer();
    }

    private void stopTimeline() {
        if (timeline != null) timeline.stop();
    }

    private void updateTimer() {
        if (running != null && running.getStartTime() != null) {
            timerLabel.setText(DateTimeUtil.formatTimer(java.time.Duration.between(running.getStartTime(), LocalDateTime.now())));
        }
    }

    public void show() { stage.show(); }
}
