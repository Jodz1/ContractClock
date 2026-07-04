package com.recka.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class WorkSession {
    private Long id;
    private Long contractId;
    private Long rateId;
    private Long activityTagId;
    private String contractTitle;
    private String clientName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long durationSeconds;
    private BigDecimal hourlyRateSnapshot = BigDecimal.ZERO;
    private String currencyCode = "USD";
    private BigDecimal calculatedAmount = BigDecimal.ZERO;
    private BigDecimal overrideAmount;
    private BigDecimal finalAmount = BigDecimal.ZERO;
    private boolean chargeable = true;
    private String description;
    private WorkSessionSource source = WorkSessionSource.TRACKER;
    private WorkSessionStatus status = WorkSessionStatus.STOPPED;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getContractId() { return contractId; }
    public void setContractId(Long contractId) { this.contractId = contractId; }
    public Long getRateId() { return rateId; }
    public void setRateId(Long rateId) { this.rateId = rateId; }
    public Long getActivityTagId() { return activityTagId; }
    public void setActivityTagId(Long activityTagId) { this.activityTagId = activityTagId; }
    public String getContractTitle() { return contractTitle; }
    public void setContractTitle(String contractTitle) { this.contractTitle = contractTitle; }
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public long getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }
    public BigDecimal getHourlyRateSnapshot() { return hourlyRateSnapshot; }
    public void setHourlyRateSnapshot(BigDecimal hourlyRateSnapshot) { this.hourlyRateSnapshot = hourlyRateSnapshot; }
    public String getCurrencyCode() { return currencyCode; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public BigDecimal getCalculatedAmount() { return calculatedAmount; }
    public void setCalculatedAmount(BigDecimal calculatedAmount) { this.calculatedAmount = calculatedAmount; }
    public BigDecimal getOverrideAmount() { return overrideAmount; }
    public void setOverrideAmount(BigDecimal overrideAmount) { this.overrideAmount = overrideAmount; }
    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
    public boolean isChargeable() { return chargeable; }
    public void setChargeable(boolean chargeable) { this.chargeable = chargeable; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public WorkSessionSource getSource() { return source; }
    public void setSource(WorkSessionSource source) { this.source = source; }
    public WorkSessionStatus getStatus() { return status; }
    public void setStatus(WorkSessionStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
