package com.recka.strategy.export;

import com.recka.model.Contract;
import com.recka.model.WorkSession;
import com.recka.util.DateTimeUtil;
import com.recka.util.FileNameUtil;
import com.recka.util.MoneyUtil;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

public class TxtBriefExportStrategy implements BriefExportStrategy {
    @Override
    public File exportSingle(WorkSession s, Contract contract, File preferredFile) throws IOException {
        File file = FileNameUtil.uniqueFile(preferredFile);
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write("Date: " + DateTimeUtil.DATE.format(s.getStartTime().toLocalDate()) + "\n");
            w.write("Contract: " + contract.getTitle() + "\n");
            w.write("Client: " + contract.getClientName() + "\n");
            w.write("Time: " + DateTimeUtil.TIME.format(s.getStartTime()) + " - " + DateTimeUtil.TIME.format(s.getEndTime()) + "\n");
            w.write("Duration: " + DateTimeUtil.formatDuration(s.getDurationSeconds()) + "\n");
            w.write("Hourly Rate: " + MoneyUtil.format(s.getHourlyRateSnapshot(), s.getCurrencyCode()) + "/h\n");
            w.write("Amount: " + MoneyUtil.format(s.getFinalAmount(), s.getCurrencyCode()) + "\n\n");
            w.write("Brief:\n");
            w.write((s.getDescription() == null || s.getDescription().isBlank()) ? "No brief added" : s.getDescription());
            w.write("\n");
        }
        return file;
    }

    @Override
    public File exportCombined(Contract contract, LocalDate from, LocalDate to, List<WorkSession> sessions, File preferredFile) throws IOException {
        File file = FileNameUtil.uniqueFile(preferredFile);
        try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            w.write("Combined Brief\n");
            w.write("Contract: " + contract.getTitle() + "\n");
            w.write("Client: " + contract.getClientName() + "\n");
            w.write("Period: " + DateTimeUtil.DATE.format(from) + " - " + DateTimeUtil.DATE.format(to) + "\n");
            w.write("----------------------------------------\n");
            for (WorkSession s : sessions) {
                w.write("Date: " + DateTimeUtil.DATE.format(s.getStartTime().toLocalDate()) + "\n");
                w.write("Time: " + DateTimeUtil.TIME.format(s.getStartTime()) + " - " + DateTimeUtil.TIME.format(s.getEndTime()) + "\n");
                w.write("Duration: " + DateTimeUtil.formatDuration(s.getDurationSeconds()) + "\n");
                w.write("Amount: " + MoneyUtil.format(s.getFinalAmount(), s.getCurrencyCode()) + "\n");
                w.write("Brief:\n");
                w.write((s.getDescription() == null || s.getDescription().isBlank()) ? "No brief added" : s.getDescription());
                w.write("\n----------------------------------------\n");
            }
        }
        return file;
    }

    @Override
    public String extension() { return "txt"; }
}
