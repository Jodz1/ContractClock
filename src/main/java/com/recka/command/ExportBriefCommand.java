package com.recka.command;

import com.recka.service.ExportService;
import java.io.File;
import java.time.LocalDate;

public class ExportBriefCommand implements Command {
    private final ExportService service;
    private final Long contractId;
    private final LocalDate from;
    private final LocalDate to;
    private final boolean includeNonChargeable;
    private final File directory;
    private final String format;

    public ExportBriefCommand(ExportService service, Long contractId, LocalDate from, LocalDate to, boolean includeNonChargeable, File directory, String format) {
        this.service = service;
        this.contractId = contractId;
        this.from = from;
        this.to = to;
        this.includeNonChargeable = includeNonChargeable;
        this.directory = directory;
        this.format = format;
    }

    public void execute() throws Exception { service.exportCombined(contractId, from, to, includeNonChargeable, directory, format); }
}
