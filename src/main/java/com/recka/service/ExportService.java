package com.recka.service;

import com.recka.dao.DaoFactory;
import com.recka.event.*;
import com.recka.model.Contract;
import com.recka.model.WorkSession;
import com.recka.strategy.export.*;
import com.recka.util.FileNameUtil;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ExportService {
    private final ContractService contractService;
    private final TimeTrackerService timeTrackerService;
    private final AppEventBus events = AppEventBus.getInstance();

    public ExportService(DaoFactory factory, ContractService contractService, TimeTrackerService timeTrackerService) {
        this.contractService = contractService;
        this.timeTrackerService = timeTrackerService;
    }

    public File exportSingle(Long sessionId, File directory, String format) throws SQLException, IOException {
        WorkSession session = timeTrackerService.findSession(sessionId);
        Contract contract = contractService.findContract(session.getContractId());
        BriefExportStrategy strategy = strategy(format);
        File preferred = new File(directory, FileNameUtil.singleBriefName(session.getStartTime().toLocalDate(), strategy.extension()));
        File file = strategy.exportSingle(session, contract, preferred);
        events.publish(new BriefExportedEvent(file));
        return file;
    }

    public File exportCombined(Long contractId, LocalDate from, LocalDate to, boolean includeNonChargeable, File directory, String format) throws SQLException, IOException {
        if (from == null || to == null || to.isBefore(from)) throw new IllegalArgumentException("Invalid export period.");
        Contract contract = contractService.findContract(contractId);
        List<WorkSession> sessions = timeTrackerService.sessionsForExport(contractId, from, to, includeNonChargeable);
        BriefExportStrategy strategy = strategy(format);
        File preferred = new File(directory, FileNameUtil.combinedBriefName(from, to, strategy.extension()));
        File file = strategy.exportCombined(contract, from, to, sessions, preferred);
        events.publish(new BriefExportedEvent(file));
        return file;
    }

    private BriefExportStrategy strategy(String format) {
        if (format != null && format.equalsIgnoreCase("PDF")) return new PdfBriefExportStrategy();
        return new TxtBriefExportStrategy();
    }
}
