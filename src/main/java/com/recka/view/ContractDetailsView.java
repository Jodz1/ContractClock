package com.recka.view;

import com.recka.AppContext;
import com.recka.command.*;
import com.recka.model.*;
import com.recka.util.DateTimeUtil;
import com.recka.util.MoneyUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;

public class ContractDetailsView extends BorderPane {
    private final AppContext context;
    private final Stage owner;
    private final WindowFactory windows;
    private final Runnable back;
    private Contract contract;
    private DashboardContractRow row;
    private final ObservableList<WorkSession> sessions = FXCollections.observableArrayList();
    private final TableView<WorkSession> table = new TableView<>(sessions);
    private final DatePicker from = new DatePicker();
    private final DatePicker to = new DatePicker();
    private final Label title = ViewUtils.label("Contract", "app-title");
    private final HBox metrics = new HBox(12);

    public ContractDetailsView(AppContext context, Stage owner, WindowFactory windows, DashboardContractRow row, Runnable back) {
        this.context = context;
        this.owner = owner;
        this.windows = windows;
        this.row = row;
        this.back = back;
        getStyleClass().add("root");
        setPadding(new Insets(22));
        build();
        load();
    }

    private void build() {
        Button backBtn = ViewUtils.button("← Dashboard", "ghost-button");
        backBtn.setOnAction(e -> back.run());
        Button edit = ViewUtils.button("Edit contract", "ghost-button");
        Button rate = ViewUtils.button("Change hourly rate", "dark-button");
        Button manual = ViewUtils.button("Add manual time", "primary-button");
        Button payment = ViewUtils.button("Add payment", "ghost-button");
        Button markPaid = ViewUtils.button("Mark balance as paid", "warning-button");
        Button combine = ViewUtils.button("Export combined briefs", "dark-button");
        Button archive = ViewUtils.button("Archive", "danger-button");
        edit.setOnAction(e -> editContract());
        rate.setOnAction(e -> changeRate());
        manual.setOnAction(e -> addManualTime());
        payment.setOnAction(e -> addPayment());
        markPaid.setOnAction(e -> markPaid());
        combine.setOnAction(e -> exportCombined());
        archive.setOnAction(e -> archive());
        FlowPane buttons = new FlowPane(10, 10, edit, rate, manual, payment, markPaid, combine, archive);
        VBox header = new VBox(14, new HBox(10, backBtn, title), metrics, buttons);
        header.getStyleClass().add("card");

        buildTable();
        Button applyFilter = ViewUtils.button("Apply session filter", "ghost-button");
        applyFilter.setOnAction(e -> loadSessions());
        HBox filter = new HBox(10, new Label("From"), from, new Label("To"), to, applyFilter);
        filter.getStyleClass().add("toolbar-card");
        VBox center = new VBox(14, filter, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        setTop(header);
        setCenter(center);
    }

    private void buildTable() {
        TableColumn<WorkSession, String> date = new TableColumn<>("Date");
        date.setCellValueFactory(v -> new SimpleStringProperty(DateTimeUtil.DATE.format(v.getValue().getStartTime().toLocalDate())));
        TableColumn<WorkSession, String> time = new TableColumn<>("Time");
        time.setCellValueFactory(v -> new SimpleStringProperty(DateTimeUtil.TIME.format(v.getValue().getStartTime()) + " - " + (v.getValue().getEndTime() == null ? "running" : DateTimeUtil.TIME.format(v.getValue().getEndTime()))));
        TableColumn<WorkSession, String> duration = new TableColumn<>("Duration");
        duration.setCellValueFactory(v -> new SimpleStringProperty(DateTimeUtil.formatDuration(v.getValue().getDurationSeconds())));
        TableColumn<WorkSession, String> rate = new TableColumn<>("Rate");
        rate.setCellValueFactory(v -> new SimpleStringProperty(MoneyUtil.format(v.getValue().getHourlyRateSnapshot(), v.getValue().getCurrencyCode()) + "/h"));
        TableColumn<WorkSession, String> amount = new TableColumn<>("Final amount");
        amount.setCellValueFactory(v -> new SimpleStringProperty(v.getValue().isChargeable() ? MoneyUtil.format(v.getValue().getFinalAmount(), v.getValue().getCurrencyCode()) : "Off charge"));
        TableColumn<WorkSession, String> brief = new TableColumn<>("Brief preview");
        brief.setCellValueFactory(v -> new SimpleStringProperty(preview(v.getValue().getDescription())));
        TableColumn<WorkSession, Void> open = new TableColumn<>("Open");
        open.setCellFactory(col -> new TableCell<>() {
            private final Button b = ViewUtils.button("Open/Edit", "ghost-button");
            { b.setOnAction(e -> windows.openSessionDetails(owner, getTableView().getItems().get(getIndex()))); }
            protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : b); }
        });
        table.getColumns().setAll(date, time, duration, rate, amount, brief, open);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setRowFactory(tv -> {
            TableRow<WorkSession> r = new TableRow<>();
            r.setOnMouseClicked(e -> { if (e.getClickCount() == 2 && !r.isEmpty()) windows.openSessionDetails(owner, r.getItem()); });
            return r;
        });
    }

    private void load() {
        try {
            contract = context.contractService().findContract(row.getContractId());
            title.setText(contract.getTitle());
            metrics.getChildren().setAll(
                ViewUtils.metric("Client", contract.getClientName()),
                ViewUtils.metric("Rate", MoneyUtil.format(contract.getCurrentHourlyRate(), contract.getCurrencyCode()) + "/h"),
                ViewUtils.metric("Status", contract.getStatus().name()),
                ViewUtils.metric("Tracked", DateTimeUtil.formatDuration(row.getTotalSeconds())),
                ViewUtils.metric("Earned", MoneyUtil.format(row.getTotalEarned(), row.getCurrencyCode())),
                ViewUtils.metric("Paid", MoneyUtil.format(row.getTotalPaid(), row.getCurrencyCode())),
                ViewUtils.metric("Unpaid", MoneyUtil.format(row.getUnpaidBalance(), row.getCurrencyCode()))
            );
            loadSessions();
        } catch (Exception e) {
            ViewUtils.error("Cannot load contract", e);
        }
    }

    private void loadSessions() {
        try {
            sessions.setAll(context.timeTrackerService().sessions(row.getContractId(), from.getValue(), to.getValue(), null));
        } catch (Exception e) {
            ViewUtils.error("Cannot load sessions", e);
        }
    }

    public void refresh(DashboardContractRow newRow) {
        this.row = newRow;
        load();
    }

    private void editContract() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit contract");
        TextField titleField = ViewUtils.text("Contract title");
        TextArea desc = ViewUtils.area("Description");
        ComboBox<ContractStatus> status = new ComboBox<>(FXCollections.observableArrayList(ContractStatus.values()));
        titleField.setText(contract.getTitle());
        desc.setText(contract.getDescription());
        status.setValue(contract.getStatus());
        GridPane form = ViewUtils.form();
        form.addRow(0, new Label("Title"), titleField);
        form.addRow(1, new Label("Description"), desc);
        form.addRow(2, new Label("Status"), status);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                try {
                    contract.setTitle(titleField.getText());
                    contract.setDescription(desc.getText());
                    contract.setStatus(status.getValue());
                    context.contractService().updateContract(contract);
                    load();
                } catch (Exception e) { ViewUtils.error("Cannot update contract", e); }
            }
        });
    }

    private void changeRate() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Change hourly rate");
        TextField rateField = ViewUtils.text("20.00");
        TextField currency = ViewUtils.text("USD");
        TextField note = ViewUtils.text("Reason / note");
        rateField.setText(contract.getCurrentHourlyRate().toPlainString());
        currency.setText(contract.getCurrencyCode());
        GridPane form = ViewUtils.form();
        form.addRow(0, new Label("New rate"), rateField);
        form.addRow(1, new Label("Currency"), currency);
        form.addRow(2, new Label("Note"), note);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                try {
                    new ChangeRateCommand(context.contractService(), contract.getId(), ViewUtils.decimal(rateField.getText()), currency.getText(), note.getText()).execute();
                } catch (Exception e) { ViewUtils.error("Cannot change rate", e); }
            }
        });
    }

    private void addManualTime() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add manual time");
        DatePicker d = new DatePicker(LocalDate.now());
        TextField st = ViewUtils.text("09:00");
        TextField et = ViewUtils.text("17:00");
        CheckBox chargeable = new CheckBox("Chargeable");
        chargeable.setSelected(true);
        TextField override = ViewUtils.text("Optional override amount");
        TextArea desc = ViewUtils.area("What did you work on?");
        GridPane form = ViewUtils.form();
        form.addRow(0, new Label("Date"), d);
        form.addRow(1, new Label("Start"), st);
        form.addRow(2, new Label("End"), et);
        form.addRow(3, new Label("Chargeable"), chargeable);
        form.addRow(4, new Label("Override"), override);
        form.addRow(5, new Label("Description"), desc);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                try {
                    LocalDateTime start = LocalDateTime.of(d.getValue(), ViewUtils.time(st.getText()));
                    LocalDateTime end = LocalDateTime.of(d.getValue(), ViewUtils.time(et.getText()));
                    boolean allowOverlap = false;
                    if (context.timeTrackerService().hasOverlap(contract.getId(), start, end, null)) {
                        allowOverlap = ViewUtils.confirm("Overlap detected", "This session overlaps with an existing session. Save anyway?");
                        if (!allowOverlap) return;
                    }
                    new AddManualTimeCommand(context.timeTrackerService(), contract.getId(), start, end, desc.getText(), chargeable.isSelected(), ViewUtils.decimal(override.getText()), allowOverlap, s -> loadSessions()).execute();
                } catch (Exception e) { ViewUtils.error("Cannot add manual time", e); }
            }
        });
    }

    private void addPayment() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add payment");
        TextField amount = ViewUtils.text("100.00");
        DatePicker date = new DatePicker(LocalDate.now());
        TextField note = ViewUtils.text("Payment note");
        GridPane form = ViewUtils.form();
        form.addRow(0, new Label("Amount"), amount);
        form.addRow(1, new Label("Date"), date);
        form.addRow(2, new Label("Note"), note);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                try {
                    new AddPaymentCommand(context.paymentService(), contract.getId(), ViewUtils.decimal(amount.getText()), date.getValue(), note.getText(), contract.getCurrencyCode()).execute();
                } catch (Exception e) { ViewUtils.error("Cannot add payment", e); }
            }
        });
    }

    private void markPaid() {
        try {
            if (!ViewUtils.confirm("Mark paid", "Create a payment for current unpaid balance?")) return;
            context.paymentService().markBalancePaid(contract.getId(), row.getUnpaidBalance(), row.getCurrencyCode());
        } catch (Exception e) { ViewUtils.error("Cannot mark balance as paid", e); }
    }

    private void exportCombined() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Export combined briefs");
        DatePicker start = new DatePicker(from.getValue() == null ? LocalDate.now().withDayOfMonth(1) : from.getValue());
        DatePicker end = new DatePicker(to.getValue() == null ? LocalDate.now() : to.getValue());
        CheckBox includeNon = new CheckBox("Include non-chargeable sessions");
        ComboBox<String> format = new ComboBox<>(FXCollections.observableArrayList("TXT", "PDF"));
        format.setValue("TXT");
        GridPane form = ViewUtils.form();
        form.addRow(0, new Label("From"), start);
        form.addRow(1, new Label("To"), end);
        form.addRow(2, new Label("Format"), format);
        form.addRow(3, new Label("Options"), includeNon);
        dialog.getDialogPane().setContent(form);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL, ButtonType.OK);
        dialog.showAndWait().ifPresent(type -> {
            if (type == ButtonType.OK) {
                DirectoryChooser chooser = new DirectoryChooser();
                chooser.setTitle("Choose export folder");
                File dir = chooser.showDialog(owner);
                if (dir == null) return;
                try {
                    File file = context.exportService().exportCombined(contract.getId(), start.getValue(), end.getValue(), includeNon.isSelected(), dir, format.getValue());
                    ViewUtils.info("Export done", "Saved file:\n" + file.getAbsolutePath());
                } catch (Exception e) { ViewUtils.error("Cannot export combined briefs", e); }
            }
        });
    }

    private void archive() {
        if (!ViewUtils.confirm("Archive contract", "Archive this contract? It will stay in the database.")) return;
        try {
            context.contractService().archive(contract.getId());
            back.run();
        } catch (Exception e) { ViewUtils.error("Cannot archive contract", e); }
    }

    private String preview(String text) {
        if (text == null || text.isBlank()) return "No brief added";
        return text.length() <= 80 ? text : text.substring(0, 77) + "...";
    }
}
