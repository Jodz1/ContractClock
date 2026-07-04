package com.recka.command;

import com.recka.model.WorkSession;
import com.recka.service.TimeTrackerService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.function.Consumer;

public class AddManualTimeCommand implements Command {
    private final TimeTrackerService service;
    private final Long contractId;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final String description;
    private final boolean chargeable;
    private final BigDecimal overrideAmount;
    private final boolean allowOverlap;
    private final Consumer<WorkSession> afterSave;

    public AddManualTimeCommand(TimeTrackerService service, Long contractId, LocalDateTime start, LocalDateTime end, String description,
                                boolean chargeable, BigDecimal overrideAmount, boolean allowOverlap, Consumer<WorkSession> afterSave) {
        this.service = service;
        this.contractId = contractId;
        this.start = start;
        this.end = end;
        this.description = description;
        this.chargeable = chargeable;
        this.overrideAmount = overrideAmount;
        this.allowOverlap = allowOverlap;
        this.afterSave = afterSave;
    }
    public void execute() throws Exception {
        WorkSession session = service.addManualTime(contractId, start, end, description, chargeable, overrideAmount, allowOverlap);
        if (afterSave != null) afterSave.accept(session);
    }
}
