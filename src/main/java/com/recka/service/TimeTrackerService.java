package com.recka.service;

import com.recka.dao.*;
import com.recka.event.*;
import com.recka.model.*;
import com.recka.util.MoneyUtil;
import com.recka.util.ValidationException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class TimeTrackerService {
    private final WorkSessionDao sessionDao;
    private final RateDao rateDao;
    private final ContractDao contractDao;
    private final AppEventBus events = AppEventBus.getInstance();

    public TimeTrackerService(DaoFactory factory) {
        this.sessionDao = factory.sessions();
        this.rateDao = factory.rates();
        this.contractDao = factory.contracts();
    }

    public Optional<WorkSession> runningSession() throws SQLException { return sessionDao.findRunning(); }
    public WorkSession findSession(Long id) throws SQLException { return sessionDao.findById(id).orElseThrow(() -> new ValidationException("Session not found.")); }

    public WorkSession start(Long contractId, String description) throws SQLException {
        Optional<WorkSession> running = sessionDao.findRunning();
        if (running.isPresent()) throw new ValidationException("Another timer is already running. Stop it first.");
        Contract contract = contractDao.findById(contractId).orElseThrow(() -> new ValidationException("Contract not found."));
        ContractRate rate = rateDao.findActiveRate(contractId).orElseGet(() -> fallbackRate(contract));
        WorkSession s = new WorkSession();
        s.setContractId(contractId);
        s.setRateId(rate.getId());
        s.setStartTime(LocalDateTime.now());
        s.setEndTime(null);
        s.setDurationSeconds(0);
        s.setHourlyRateSnapshot(rate.getHourlyRate());
        s.setCurrencyCode(rate.getCurrencyCode());
        s.setCalculatedAmount(BigDecimal.ZERO);
        s.setFinalAmount(BigDecimal.ZERO);
        s.setChargeable(true);
        s.setDescription(description);
        s.setSource(WorkSessionSource.TRACKER);
        s.setStatus(WorkSessionStatus.RUNNING);
        s = sessionDao.save(s);
        events.publish(new SessionStartedEvent(s.getId(), s.getContractId()));
        return s;
    }

    public WorkSession stop(Long sessionId, String description) throws SQLException {
        WorkSession s = findSession(sessionId);
        if (s.getStatus() != WorkSessionStatus.RUNNING) throw new ValidationException("Session is not running.");
        s.setEndTime(LocalDateTime.now());
        if (description != null) s.setDescription(description);
        recalculate(s);
        s.setStatus(WorkSessionStatus.STOPPED);
        sessionDao.update(s);
        events.publish(new SessionStoppedEvent(s.getId(), s.getContractId()));
        return s;
    }

    public WorkSession stopNowForRecovery(Long sessionId) throws SQLException {
        return stop(sessionId, null);
    }

    public void discard(Long sessionId) throws SQLException {
        WorkSession s = findSession(sessionId);
        sessionDao.discard(sessionId);
        events.publish(new SessionStoppedEvent(sessionId, s.getContractId()));
    }

    public WorkSession addManualTime(Long contractId, LocalDateTime start, LocalDateTime end, String description, boolean chargeable, BigDecimal overrideAmount, boolean allowOverlap) throws SQLException {
        validateTime(contractId, start, end, null, allowOverlap);
        Contract contract = contractDao.findById(contractId).orElseThrow(() -> new ValidationException("Contract not found."));
        ContractRate rate = rateDao.findRateAt(contractId, start).orElseGet(() -> fallbackRate(contract));
        WorkSession s = new WorkSession();
        s.setContractId(contractId);
        s.setRateId(rate.getId());
        s.setStartTime(start);
        s.setEndTime(end);
        s.setHourlyRateSnapshot(rate.getHourlyRate());
        s.setCurrencyCode(rate.getCurrencyCode());
        s.setDescription(description);
        s.setChargeable(chargeable);
        s.setOverrideAmount(overrideAmount);
        s.setSource(WorkSessionSource.MANUAL);
        s.setStatus(WorkSessionStatus.STOPPED);
        recalculate(s);
        s = sessionDao.save(s);
        events.publish(new SessionStoppedEvent(s.getId(), s.getContractId()));
        return s;
    }

    public void updateSession(WorkSession s, boolean allowOverlap) throws SQLException {
        validateTime(s.getContractId(), s.getStartTime(), s.getEndTime(), s.getId(), allowOverlap);
        recalculate(s);
        sessionDao.update(s);
        events.publish(new SessionStoppedEvent(s.getId(), s.getContractId()));
    }

    public List<WorkSession> sessions(Long contractId, LocalDate from, LocalDate to, Boolean chargeableOnly) throws SQLException {
        LocalDateTime start = from == null ? null : from.atStartOfDay();
        LocalDateTime end = to == null ? null : to.plusDays(1).atStartOfDay();
        return sessionDao.findByContract(contractId, start, end, chargeableOnly);
    }

    public List<WorkSession> sessionsForExport(Long contractId, LocalDate from, LocalDate to, boolean includeNonChargeable) throws SQLException {
        return sessionDao.findStoppedByContractBetween(contractId, from.atStartOfDay(), to.plusDays(1).atStartOfDay(), includeNonChargeable);
    }

    public long trackedToday(Long contractId) throws SQLException {
        LocalDate today = LocalDate.now();
        return sessions(contractId, today, today, null).stream().mapToLong(WorkSession::getDurationSeconds).sum();
    }

    public long trackedThisWeek(Long contractId) throws SQLException {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(today.getDayOfWeek().getValue() - 1L);
        return sessions(contractId, start, today, null).stream().mapToLong(WorkSession::getDurationSeconds).sum();
    }

    private void validateTime(Long contractId, LocalDateTime start, LocalDateTime end, Long ignoreSessionId, boolean allowOverlap) throws SQLException {
        if (start == null || end == null) throw new ValidationException("Start and end time are required.");
        if (!end.isAfter(start)) throw new ValidationException("End time must be after start time.");
        if (!allowOverlap && sessionDao.hasOverlap(contractId, start, end, ignoreSessionId)) {
            throw new ValidationException("Session overlaps with an existing session for this contract.");
        }
    }

    public boolean hasOverlap(Long contractId, LocalDateTime start, LocalDateTime end, Long ignoreSessionId) throws SQLException {
        return sessionDao.hasOverlap(contractId, start, end, ignoreSessionId);
    }

    public void recalculate(WorkSession s) {
        if (s.getEndTime() == null || s.getStartTime() == null) {
            s.setDurationSeconds(0);
            s.setCalculatedAmount(BigDecimal.ZERO);
            s.setFinalAmount(BigDecimal.ZERO);
            return;
        }
        long seconds = Duration.between(s.getStartTime(), s.getEndTime()).getSeconds();
        s.setDurationSeconds(Math.max(0, seconds));
        BigDecimal calculated = MoneyUtil.calculateAmount(s.getDurationSeconds(), s.getHourlyRateSnapshot());
        s.setCalculatedAmount(calculated);
        s.setFinalAmount(MoneyUtil.finalAmount(s.isChargeable(), calculated, s.getOverrideAmount()));
    }

    private ContractRate fallbackRate(Contract contract) {
        ContractRate r = new ContractRate();
        r.setContractId(contract.getId());
        r.setHourlyRate(contract.getCurrentHourlyRate());
        r.setCurrencyCode(contract.getCurrencyCode());
        r.setValidFrom(LocalDateTime.now());
        return r;
    }
}
