package com.recka.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DashboardContractRow {
    private Long contractId;
    private Long clientId;
    private String contractTitle;
    private String clientName;
    private ContractStatus status;
    private BigDecimal currentHourlyRate;
    private String currencyCode;
    private long totalSeconds;
    private BigDecimal totalEarned;
    private BigDecimal totalPaid;
    private BigDecimal unpaidBalance;
    private LocalDateTime lastActivity;

    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }
    public String getContractTitle() { return contractTitle; }
    public void setContractTitle(String contractTitle) { this.contractTitle = contractTitle; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public ContractStatus getStatus() { return status; }
    public void setStatus(ContractStatus status) { this.status = status; }
    public BigDecimal getCurrentHourlyRate() { return currentHourlyRate; }
    public void setCurrentHourlyRate(BigDecimal currentHourlyRate) { this.currentHourlyRate = currentHourlyRate; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public long getTotalSeconds() { return totalSeconds; }
    public void setTotalSeconds(long totalSeconds) { this.totalSeconds = totalSeconds; }
    public BigDecimal getTotalEarned() { return totalEarned; }
    public void setTotalEarned(BigDecimal totalEarned) { this.totalEarned = totalEarned; }
    public BigDecimal getTotalPaid() { return totalPaid; }
    public void setTotalPaid(BigDecimal totalPaid) { this.totalPaid = totalPaid; }
    public BigDecimal getUnpaidBalance() { return unpaidBalance; }
    public void setUnpaidBalance(BigDecimal unpaidBalance) { this.unpaidBalance = unpaidBalance; }
    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }

    @Override
    public String toString() {
        return contractTitle + " · " + clientName;
    }
}
