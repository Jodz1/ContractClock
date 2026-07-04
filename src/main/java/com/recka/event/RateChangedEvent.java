package com.recka.event;

public class RateChangedEvent extends AppEvent {
    private final Long contractId;
    public RateChangedEvent(Long contractId) { this.contractId = contractId; }
    public Long getContractId() { return contractId; }
}
