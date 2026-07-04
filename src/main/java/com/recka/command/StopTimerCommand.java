package com.recka.command;

import com.recka.model.WorkSession;
import com.recka.service.TimeTrackerService;
import java.util.function.Consumer;

public class StopTimerCommand implements Command {
    private final TimeTrackerService service;
    private final Long sessionId;
    private final String description;
    private final Consumer<WorkSession> afterStop;
    public StopTimerCommand(TimeTrackerService service, Long sessionId, String description, Consumer<WorkSession> afterStop) {
        this.service = service;
        this.sessionId = sessionId;
        this.description = description;
        this.afterStop = afterStop;
    }
    public void execute() throws Exception {
        WorkSession session = service.stop(sessionId, description);
        if (afterStop != null) afterStop.accept(session);
    }
}
