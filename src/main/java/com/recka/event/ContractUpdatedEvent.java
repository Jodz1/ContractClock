package com.recka.event;

public class ContractUpdatedEvent extends AppEvent {
    private final Long contractId;
    public ContractUpdatedEvent(Long contractId) { this.contractId = contractId; }
    public Long getContractId() { return contractId; }
}
