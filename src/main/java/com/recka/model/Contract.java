package com.recka.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Contract {
    private Long id;
    private Long clientId;
    private String clientName;
    private String title;
    private String description;
    private ContractStatus status = ContractStatus.ACTIVE;
    private BigDecimal currentHourlyRate = BigDecimal.ZERO;
    private String currencyCode = "USD";
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Contract() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ContractStatus getStatus() { return status; }
    public void setStatus(ContractStatus status) { this.status = status; }
    public BigDecimal getCurrentHourlyRate() { return currentHourlyRate; }
    public void setCurrentHourlyRate(BigDecimal currentHourlyRate) { this.currentHourlyRate = currentHourlyRate; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return title + (clientName == null ? "" : " · " + clientName);
    }
}
