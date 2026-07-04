package com.recka.event;

public class PaymentAddedEvent extends AppEvent {
    private final Long contractId;
    public PaymentAddedEvent(Long contractId) { this.contractId = contractId; }
    public Long getContractId() { return contractId; }
}
