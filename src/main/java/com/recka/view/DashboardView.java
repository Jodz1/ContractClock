package com.recka.view;

import com.recka.AppContext;
import com.recka.event.*;
import com.recka.model.*;
import com.recka.util.DateTimeUtil;
import com.recka.util.MoneyUtil;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DashboardView extends BorderPane {
    private final AppContext context;
    private final Stage owner;
    private final WindowFactory windows;
    private final ObservableList<DashboardContractRow> rows = FXCollections.observableArrayList();
    private final TableView<DashboardContractRow> table = new TableView<>(rows);
    private final TextField search = ViewUtils.text("Search contracts or clients...");
    private ContractDetailsView detailsView;

    public DashboardView(AppContext context, Stage owner) {
        this.context = context;
        this.owner = owner;
        this.windows = new WindowFactory(context);
        buildShell();
        showDashboard();
        AppEventBus.getInstance().subscribe(this::onEvent);
    }

    private void buildShell() {
        VBox sidebar = new VBox(16);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(230);
        Label logo = ViewUtils.label("Recka", "sidebar-title");
        Label cap = ViewUtils.label("Desktop Time Tracker", "sidebar-caption");
        Button dash = ViewUtils.button("Dashboard", "ghost-button");
        Button tracker = ViewUtils.button("Track Your Time", "primary-button");
        Button refresh = ViewUtils.button("Refresh", "ghost-button");
        dash.setMaxWidth(Double.MAX_VALUE);
        tracker.setMaxWidth(Double.MAX_VALUE);
        refresh.setMaxWidth(Double.MAX_VALUE);
        dash.setOnAction(e -> showDashboard());
        tracker.setOnAction(e -> windows.openTracker(owner, Optional.empty()));
        refresh.setOnAction(e -> loadRows());
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Label hint = ViewUtils.label("MySQL + JDBC\nJavaFX + DAO", "sidebar-caption");
        sidebar.getChildren().addAll(logo, cap, dash, tracker, refresh, spacer, hint);
        setLeft(sidebar);
    }

    private void showDashboard() {
        detailsView = null;
        VBox root = new VBox(16);
        root.setPadding(new Insets(22));
        HBox titleBar = new HBox(12);
        Label title = ViewUtils.label("Dashboard", "app-title");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button newContract = ViewUtils.button("New contract", "dark-button");
        Button totals = ViewUtils.button("Contract totals", "ghost-button");
        Button track = ViewUtils.button("Track Your Time", "primary-button");
        newContract.setOnAction(e -> newContractDialog());
        totals.setOnAction(e -> contractTotalsDialog());
        track.setOnAction(e -> windows.openTracker(owner, Optional.ofNullable(table.getSelectionModel().getSelectedItem())));
        titleBar.getChildren().addAll(title, spacer, newContract, totals, track);

        search.textProperty().addListener((obs, old, val) -> loadRows());
        HBox toolbar = new HBox(12, search);
        toolbar.getStyleClass().add("toolbar-card");
        HBox.setHgrow(search, Priority.ALWAYS);
        buildTable();
        VBox.setVgrow(table, Priority.ALWAYS);
        root.getChildren().addAll(titleBar, toolbar, table);
        setCenter(root);
        loadRows();
    }

    private void buildTable() {
        if (!table.getColumns().isEmpty()) return;
        TableColumn<DashboardContractRow, String> contract = new TableColumn<>("Contract");
        contract.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getContractTitle()));
        TableColumn<DashboardContractRow, String> client = new TableColumn<>("Client");
        client.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getClientName()));
        TableColumn<DashboardContractRow, String> status = new TableColumn<>("Status");
        status.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().getStatus().name()));
        TableColumn<DashboardContractRow, String> rate = new TableColumn<>("Rate");
        rate.setCellValueFactory(v -> new SimpleStringProperty(MoneyUtil.format(v.getValue().getCurrentHourlyRate(), v.getValue().getCurrencyCode()) + "/h"));
        TableColumn<DashboardContractRow, String> hours = new TableColumn<>("Hours");
        hours.setCellValueFactory(v -> new SimpleStringProperty(DateTimeUtil.formatDuration(v.getValue().getTotalSeconds())));
        TableColumn<DashboardContractRow, String> earned = new TableColumn<>("Earned");
        earned.setCellValueFactory(v -> new SimpleStringProperty(MoneyUtil.format(v.getValue().getTotalEarned(), v.getValue().getCurrencyCode())));
        TableColumn<DashboardContractRow, String> paid = new TableColumn<>("Paid");
        paid.setCellValueFactory(v -> new SimpleStringProperty(MoneyUtil.format(v.getValue().getTotalPaid(), v.getValue().getCurrencyCode())));
        TableColumn<DashboardContractRow, String> unpaid = new TableColumn<>("Unpaid");
        unpaid.setCellValueFactory(v -> new SimpleStringProperty(MoneyUtil.format(v.getValue().getUnpaidBalance(), v.getValue().getCurrencyCode())));
        TableColumn<DashboardContractRow, String> last = new TableColumn<>("Last activity");
        last.setCellValueFactory(v -> new SimpleStringProperty(DateTimeUtil.safeDateTime(v.getValue().getLastActivity())));

        TableColumn<DashboardContractRow, Void> actions = new TableColumn<>("Actions");
        actions.setCellFactory(col -> new TableCell<>() {
            private final Button open = ViewUtils.button("Open", "ghost-button");
            private final Button start = ViewUtils.button("Start", "primary-button");
            private final HBox box = new HBox(6, open, start);
            {
                open.setOnAction(e -> openDetails(getTableView().getItems().get(getIndex())));
                start.setOnAction(e -> windows.openTracker(owner, Optional.of(getTableView().getItems().get(getIndex()))));
            }
            protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : box); }
        });
        table.getColumns().setAll(contract, client, status, rate, hours, earned, paid, unpaid, last, actions);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setRowFactory(tv -> {
            TableRow<DashboardContractRow> r = new TableRow<>();
            r.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !r.isEmpty()) openDetails(r.getItem()); });
            return r;
        });
    }

    private void loadRows() {
        try {
            rows.setAll(context.contractService().dashboard(search.getText()));
            refreshDetailsIfOpen();
        } catch (Exception e) {
            ViewUtils.error("Cannot load dashboard", e);
        }
    }

    private void openDetails(DashboardContractRow row) {
        detailsView = new ContractDetailsView(context, owner, windows, row, this::showDashboard);
        setCenter(detailsView);
    }

    private void refreshDetailsIfOpen() {
        if (detailsView == null) return;
        try {
            Node center = getCenter();
            if (center != detailsView) return;
            DashboardContractRow selected = table.getSelectionModel().getSelectedItem();
            if (selected == null && !rows.isEmpty()) selected = rows.get(0);
            DashboardContractRow finalSelected = selected;
            if (finalSelected != null) rows.stream().filter(r -> r.getContractId().equals(finalSelected.getContractId())).findFirst().ifPresent(detailsView::refresh);
        } catch (Exception ignored) {}
    }


    private void contractTotalsDialog() {
        try {
            List<DashboardContractRow> contracts = context.contractService().dashboard(null);
            if (contracts.isEmpty()) {
                ViewUtils.info("Contract totals", "There are no contracts to calculate.");
                return;
            }

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(owner);
            dialog.setTitle("Contract totals");
            dialog.setHeaderText("Calculate total earned amount");

            DatePicker fromDate = new DatePicker();
            fromDate.setPromptText("From date");
            DatePicker toDate = new DatePicker();
            toDate.setPromptText("To date");

            Set<Long> selectedContractIds = new HashSet<>();
            contracts.forEach(row -> selectedContractIds.add(row.getContractId()));

            CheckBox selectAll = new CheckBox("Select all contracts");
            selectAll.setSelected(true);

            ListView<DashboardContractRow> contractList = new ListView<>(FXCollections.observableArrayList(contracts));
            contractList.setPrefHeight(280);
            contractList.setCellFactory(list -> new ListCell<>() {
                private final CheckBox checkBox = new CheckBox();

                {
                    checkBox.setOnAction(event -> {
                        DashboardContractRow row = getItem();
                        if (row == null) return;
                        if (checkBox.isSelected()) {
                            selectedContractIds.add(row.getContractId());
                        } else {
                            selectedContractIds.remove(row.getContractId());
                        }
                        selectAll.setSelected(selectedContractIds.size() == contracts.size());
                    });
                }

                @Override
                protected void updateItem(DashboardContractRow row, boolean empty) {
                    super.updateItem(row, empty);
                    if (empty || row == null) {
                        setGraphic(null);
                        setText(null);
                    } else {
                        checkBox.setText(row.getContractTitle() + " — " + row.getClientName());
                        checkBox.setSelected(selectedContractIds.contains(row.getContractId()));
                        setGraphic(checkBox);
                    }
                }
            });

            selectAll.setOnAction(event -> {
                selectedContractIds.clear();
                if (selectAll.isSelected()) contracts.forEach(row -> selectedContractIds.add(row.getContractId()));
                contractList.refresh();
            });

            Button clearSelection = ViewUtils.button("Clear", "ghost-button");
            clearSelection.setOnAction(event -> {
                selectedContractIds.clear();
                selectAll.setSelected(false);
                contractList.refresh();
            });

            Label totalLabel = ViewUtils.label("Total: —", "metric-value");
            Label note = ViewUtils.label("Read-only calculation. Database data is not changed.", "muted");
            Button calculate = ViewUtils.button("Calculate", "primary-button");
            calculate.setOnAction(event -> {
                try {
                    LocalDate from = fromDate.getValue();
                    LocalDate to = toDate.getValue();
                    if (from != null && to != null && from.isAfter(to)) {
                        totalLabel.setText("Total: invalid date range");
                        return;
                    }
                    if (selectedContractIds.isEmpty()) {
                        totalLabel.setText("Total: select at least one contract");
                        return;
                    }
                    Map<String, BigDecimal> totals = context.contractService().earnedTotals(new ArrayList<>(selectedContractIds), from, to);
                    totalLabel.setText("Total: " + formatTotals(totals));
                } catch (Exception e) {
                    ViewUtils.error("Cannot calculate contract totals", e);
                }
            });

            HBox dates = new HBox(10, new Label("From"), fromDate, new Label("To"), toDate);
            HBox selectionActions = new HBox(10, selectAll, clearSelection);
            HBox resultRow = new HBox(14, calculate, totalLabel);
            VBox content = new VBox(14, dates, selectionActions, contractList, resultRow, note);
            content.setPrefWidth(680);
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            calculate.fire();
            dialog.showAndWait();
        } catch (Exception e) {
            ViewUtils.error("Cannot open contract totals", e);
        }
    }

    private String formatTotals(Map<String, BigDecimal> totals) {
        if (totals == null || totals.isEmpty()) return "USD 0.00";
        return totals.entrySet().stream()
                .map(entry -> MoneyUtil.format(entry.getValue(), entry.getKey()))
                .collect(Collectors.joining(" + "));
    }

    private void newContractDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("New contract");
        TextField clientName = ViewUtils.text("Client name");
        TextField clientEmail = ViewUtils.text("Client email");
        TextField company = ViewUtils.text("Company name");
        TextField contractTitle = ViewUtils.text("Contract title");
        TextField rate = ViewUtils.text("15.00");
        TextField currency = ViewUtils.text("USD");
        TextArea description = ViewUtils.area("Description");
        GridPane form = ViewUtils.form();
        form.addRow(0, new Label("Client"), clientName);
        form.addRow(1, new Label("Email"), clientEmail);
        form.addRow(2, new Label("Company"), company);
        form.addRow(3, new Label("Contract"), contractTitle);
        form.addRow(4, new Label("Hourly rate"), rate);
        form.addRow(5, new Label("Currency"), currency);
        form.addRow(6, new Label("Description"), description);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                try {
                    BigDecimal parsedRate = ViewUtils.decimal(rate.getText());
                    context.contractService().createContract(clientName.getText(), clientEmail.getText(), company.getText(), contractTitle.getText(), description.getText(), parsedRate, currency.getText());
                    loadRows();
                } catch (Exception e) {
                    ViewUtils.error("Cannot create contract", e);
                }
            }
        });
    }

    private void onEvent(AppEvent event) {
        if (event instanceof SessionStartedEvent || event instanceof SessionStoppedEvent || event instanceof ContractUpdatedEvent || event instanceof PaymentAddedEvent || event instanceof RateChangedEvent) {
            loadRows();
        }
    }

    public void recoverIfNeeded() {
        try {
            Optional<WorkSession> running = context.timeTrackerService().runningSession();
            if (running.isEmpty()) return;
            WorkSession s = running.get();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Unfinished session found");
            alert.setHeaderText("Recka found a running session from " + DateTimeUtil.safeDateTime(s.getStartTime()));
            alert.setContentText("Choose what to do with the unfinished session.");
            ButtonType continueBtn = new ButtonType("Continue session");
            ButtonType stopBtn = new ButtonType("Stop session now");
            ButtonType discardBtn = new ButtonType("Discard session");
            alert.getButtonTypes().setAll(continueBtn, stopBtn, discardBtn);
            ButtonType result = alert.showAndWait().orElse(continueBtn);
            if (result == continueBtn) {
                windows.openTracker(owner, Optional.empty());
            } else if (result == stopBtn) {
                context.timeTrackerService().stopNowForRecovery(s.getId());
            } else if (result == discardBtn) {
                context.timeTrackerService().discard(s.getId());
            }
        } catch (Exception e) {
            ViewUtils.error("Recovery failed", e);
        }
    }
}
