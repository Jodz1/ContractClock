package com.recka.command;

import com.recka.model.WorkSession;
import com.recka.service.TimeTrackerService;
import java.util.function.Consumer;

public class StartTimerCommand implements Command {
    private final TimeTrackerService service;
    private final Long contractId;
    private final String description;
    private final Consumer<WorkSession> afterStart;
    public StartTimerCommand(TimeTrackerService service, Long contractId, String description, Consumer<WorkSession> afterStart) {
        this.service = service;
        this.contractId = contractId;
        this.description = description;
        this.afterStart = afterStart;
    }
    public void execute() throws Exception {
        WorkSession session = service.start(contractId, description);
        if (afterStart != null) afterStart.accept(session);
    }
}
