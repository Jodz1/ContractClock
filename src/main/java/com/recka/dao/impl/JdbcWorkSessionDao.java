package com.recka.dao.impl;

import com.recka.dao.WorkSessionDao;
import com.recka.model.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class JdbcWorkSessionDao extends JdbcSupport implements WorkSessionDao {
    @Override
    public WorkSession save(WorkSession s) throws SQLException {
        String sql = """
                INSERT INTO work_sessions(contract_id, rate_id, activity_tag_id, start_time, end_time, duration_seconds,
                  hourly_rate_snapshot, currency_code, calculated_amount, override_amount, final_amount,
                  chargeable, description, source, status)
                VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                """;
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, s, false);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) s.setId(rs.getLong(1));
            }
            return s;
        }
    }

    @Override
    public void update(WorkSession s) throws SQLException {
        String sql = """
                UPDATE work_sessions SET contract_id=?, rate_id=?, activity_tag_id=?, start_time=?, end_time=?, duration_seconds=?,
                  hourly_rate_snapshot=?, currency_code=?, calculated_amount=?, override_amount=?, final_amount=?,
                  chargeable=?, description=?, source=?, status=? WHERE id=?
                """;
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, s, true);
            ps.executeUpdate();
        }
    }

    private void bind(PreparedStatement ps, WorkSession s, boolean withId) throws SQLException {
        ps.setLong(1, s.getContractId());
        if (s.getRateId() == null) ps.setNull(2, Types.BIGINT); else ps.setLong(2, s.getRateId());
        if (s.getActivityTagId() == null) ps.setNull(3, Types.BIGINT); else ps.setLong(3, s.getActivityTagId());
        ps.setTimestamp(4, ts(s.getStartTime()));
        ps.setTimestamp(5, ts(s.getEndTime()));
        ps.setLong(6, s.getDurationSeconds());
        ps.setBigDecimal(7, s.getHourlyRateSnapshot());
        ps.setString(8, s.getCurrencyCode());
        ps.setBigDecimal(9, s.getCalculatedAmount());
        ps.setBigDecimal(10, s.getOverrideAmount());
        ps.setBigDecimal(11, s.getFinalAmount());
        ps.setBoolean(12, s.isChargeable());
        ps.setString(13, s.getDescription());
        ps.setString(14, s.getSource().name());
        ps.setString(15, s.getStatus().name());
        if (withId) ps.setLong(16, s.getId());
    }

    @Override
    public Optional<WorkSession> findById(Long id) throws SQLException {
        String sql = baseSelect() + " WHERE ws.id=?";
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<WorkSession> findRunning() throws SQLException {
        String sql = baseSelect() + " WHERE ws.status='RUNNING' ORDER BY ws.start_time DESC LIMIT 1";
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return Optional.of(map(rs));
            return Optional.empty();
        }
    }

    @Override
    public List<WorkSession> findByContract(Long contractId, LocalDateTime from, LocalDateTime to, Boolean chargeableOnly) throws SQLException {
        StringBuilder sql = new StringBuilder(baseSelect()).append(" WHERE ws.contract_id=? ");
        if (from != null) sql.append(" AND ws.start_time >= ? ");
        if (to != null) sql.append(" AND ws.start_time < ? ");
        if (chargeableOnly != null) sql.append(" AND ws.chargeable = ? ");
        sql.append(" ORDER BY ws.start_time DESC");
        List<WorkSession> out = new ArrayList<>();
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int i = 1;
            ps.setLong(i++, contractId);
            if (from != null) ps.setTimestamp(i++, ts(from));
            if (to != null) ps.setTimestamp(i++, ts(to));
            if (chargeableOnly != null) ps.setBoolean(i, chargeableOnly);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    @Override
    public List<WorkSession> findStoppedByContractBetween(Long contractId, LocalDateTime from, LocalDateTime to, boolean includeNonChargeable) throws SQLException {
        String sql = baseSelect() + " WHERE ws.contract_id=? AND ws.status='STOPPED' AND ws.start_time >= ? AND ws.start_time < ? " +
                (includeNonChargeable ? "" : " AND ws.chargeable=TRUE ") + " ORDER BY ws.start_time ASC";
        List<WorkSession> out = new ArrayList<>();
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.setTimestamp(2, ts(from));
            ps.setTimestamp(3, ts(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    @Override
    public boolean hasOverlap(Long contractId, LocalDateTime start, LocalDateTime end, Long ignoreSessionId) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM work_sessions
                WHERE contract_id=?
                  AND status <> 'DISCARDED'
                  AND end_time IS NOT NULL
                  AND start_time < ?
                  AND end_time > ?
                """ + (ignoreSessionId == null ? "" : " AND id <> ?");
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.setTimestamp(2, ts(end));
            ps.setTimestamp(3, ts(start));
            if (ignoreSessionId != null) ps.setLong(4, ignoreSessionId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getLong(1) > 0;
            }
        }
    }

    @Override
    public void discard(Long id) throws SQLException {
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement("UPDATE work_sessions SET status='DISCARDED' WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private String baseSelect() {
        return """
                SELECT ws.*, ct.title AS contract_title, cl.name AS client_name
                FROM work_sessions ws
                JOIN contracts ct ON ct.id = ws.contract_id
                JOIN clients cl ON cl.id = ct.client_id
                """;
    }

    private WorkSession map(ResultSet rs) throws SQLException {
        WorkSession s = new WorkSession();
        s.setId(rs.getLong("id"));
        s.setContractId(rs.getLong("contract_id"));
        long rateId = rs.getLong("rate_id");
        s.setRateId(rs.wasNull() ? null : rateId);
        long tagId = rs.getLong("activity_tag_id");
        s.setActivityTagId(rs.wasNull() ? null : tagId);
        s.setContractTitle(rs.getString("contract_title"));
        s.setClientName(rs.getString("client_name"));
        s.setStartTime(ldt(rs.getTimestamp("start_time")));
        s.setEndTime(ldt(rs.getTimestamp("end_time")));
        s.setDurationSeconds(rs.getLong("duration_seconds"));
        s.setHourlyRateSnapshot(rs.getBigDecimal("hourly_rate_snapshot"));
        s.setCurrencyCode(rs.getString("currency_code"));
        s.setCalculatedAmount(rs.getBigDecimal("calculated_amount"));
        s.setOverrideAmount(rs.getBigDecimal("override_amount"));
        s.setFinalAmount(rs.getBigDecimal("final_amount"));
        s.setChargeable(rs.getBoolean("chargeable"));
        s.setDescription(rs.getString("description"));
        s.setSource(WorkSessionSource.valueOf(rs.getString("source")));
        s.setStatus(WorkSessionStatus.valueOf(rs.getString("status")));
        s.setCreatedAt(ldt(rs.getTimestamp("created_at")));
        s.setUpdatedAt(ldt(rs.getTimestamp("updated_at")));
        return s;
    }
}
