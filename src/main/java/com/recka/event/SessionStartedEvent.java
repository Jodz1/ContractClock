package com.recka.event;

public class SessionStartedEvent extends AppEvent {
    private final Long sessionId;
    private final Long contractId;
    public SessionStartedEvent(Long sessionId, Long contractId) {
        this.sessionId = sessionId;
        this.contractId = contractId;
    }
    public Long getSessionId() { return sessionId; }
    public Long getContractId() { return contractId; }
}
