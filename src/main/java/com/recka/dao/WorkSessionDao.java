package com.recka.dao;

import com.recka.model.WorkSession;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WorkSessionDao {
    WorkSession save(WorkSession session) throws SQLException;
    void update(WorkSession session) throws SQLException;
    Optional<WorkSession> findById(Long id) throws SQLException;
    Optional<WorkSession> findRunning() throws SQLException;
    List<WorkSession> findByContract(Long contractId, LocalDateTime from, LocalDateTime to, Boolean chargeableOnly) throws SQLException;
    List<WorkSession> findStoppedByContractBetween(Long contractId, LocalDateTime from, LocalDateTime to, boolean includeNonChargeable) throws SQLException;
    boolean hasOverlap(Long contractId, LocalDateTime start, LocalDateTime end, Long ignoreSessionId) throws SQLException;
    void discard(Long id) throws SQLException;
}
