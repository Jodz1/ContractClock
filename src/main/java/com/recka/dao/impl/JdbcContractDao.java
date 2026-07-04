package com.recka.dao.impl;

import com.recka.dao.ContractDao;
import com.recka.model.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class JdbcContractDao extends JdbcSupport implements ContractDao {
    @Override
    public Contract save(Contract contract) throws SQLException {
        String sql = "INSERT INTO contracts(client_id, title, description, status, current_hourly_rate, currency_code) VALUES(?,?,?,?,?,?)";
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, contract.getClientId());
            ps.setString(2, contract.getTitle());
            ps.setString(3, contract.getDescription());
            ps.setString(4, contract.getStatus().name());
            ps.setBigDecimal(5, contract.getCurrentHourlyRate());
            ps.setString(6, contract.getCurrencyCode());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) contract.setId(rs.getLong(1));
            }
            return contract;
        }
    }

    @Override
    public void update(Contract contract) throws SQLException {
        String sql = "UPDATE contracts SET client_id=?, title=?, description=?, status=?, current_hourly_rate=?, currency_code=? WHERE id=?";
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, contract.getClientId());
            ps.setString(2, contract.getTitle());
            ps.setString(3, contract.getDescription());
            ps.setString(4, contract.getStatus().name());
            ps.setBigDecimal(5, contract.getCurrentHourlyRate());
            ps.setString(6, contract.getCurrencyCode());
            ps.setLong(7, contract.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void updateStatus(Long contractId, ContractStatus status) throws SQLException {
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement("UPDATE contracts SET status=? WHERE id=?")) {
            ps.setString(1, status.name());
            ps.setLong(2, contractId);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateCurrentRate(Long contractId, BigDecimal rate, String currencyCode) throws SQLException {
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement("UPDATE contracts SET current_hourly_rate=?, currency_code=? WHERE id=?")) {
            ps.setBigDecimal(1, rate);
            ps.setString(2, currencyCode);
            ps.setLong(3, contractId);
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long contractId) throws SQLException {
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement("DELETE FROM contracts WHERE id=?")) {
            ps.setLong(1, contractId);
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<Contract> findById(Long id) throws SQLException {
        String sql = "SELECT ct.*, c.name AS client_name FROM contracts ct JOIN clients c ON c.id=ct.client_id WHERE ct.id=?";
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    @Override
    public List<DashboardContractRow> findDashboardRows(String search) throws SQLException {
        String where = "";
        if (search != null && !search.isBlank()) {
            where = " WHERE LOWER(ct.title) LIKE ? OR LOWER(c.name) LIKE ? ";
        }
        String sql = """
                SELECT ct.id AS contract_id,
                       ct.client_id,
                       ct.title,
                       c.name AS client_name,
                       ct.status,
                       ct.current_hourly_rate,
                       ct.currency_code,
                       COALESCE(SUM(CASE WHEN ws.status='STOPPED' THEN ws.duration_seconds ELSE 0 END), 0) AS total_seconds,
                       COALESCE(SUM(CASE WHEN ws.status='STOPPED' THEN ws.final_amount ELSE 0 END), 0) AS total_earned,
                       COALESCE((SELECT SUM(p.amount) FROM payments p WHERE p.contract_id = ct.id), 0) AS total_paid,
                       MAX(ws.end_time) AS last_activity
                FROM contracts ct
                JOIN clients c ON c.id = ct.client_id
                LEFT JOIN work_sessions ws ON ws.contract_id = ct.id
                """ + where + " GROUP BY ct.id, ct.client_id, ct.title, c.name, ct.status, ct.current_hourly_rate, ct.currency_code ORDER BY ct.updated_at DESC, ct.title";
        List<DashboardContractRow> rows = new ArrayList<>();
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (search != null && !search.isBlank()) {
                String like = "%" + search.toLowerCase(Locale.ROOT) + "%";
                ps.setString(1, like);
                ps.setString(2, like);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DashboardContractRow row = new DashboardContractRow();
                    row.setContractId(rs.getLong("contract_id"));
                    row.setClientId(rs.getLong("client_id"));
                    row.setContractTitle(rs.getString("title"));
                    row.setClientName(rs.getString("client_name"));
                    row.setStatus(ContractStatus.valueOf(rs.getString("status")));
                    row.setCurrentHourlyRate(rs.getBigDecimal("current_hourly_rate"));
                    row.setCurrencyCode(rs.getString("currency_code"));
                    row.setTotalSeconds(rs.getLong("total_seconds"));
                    BigDecimal earned = rs.getBigDecimal("total_earned");
                    BigDecimal paid = rs.getBigDecimal("total_paid");
                    row.setTotalEarned(earned == null ? BigDecimal.ZERO : earned);
                    row.setTotalPaid(paid == null ? BigDecimal.ZERO : paid);
                    row.setUnpaidBalance(row.getTotalEarned().subtract(row.getTotalPaid()));
                    row.setLastActivity(ldt(rs.getTimestamp("last_activity")));
                    rows.add(row);
                }
            }
        }
        return rows;
    }


    @Override
    public Map<String, BigDecimal> calculateEarnedTotals(List<Long> contractIds, LocalDate fromDate, LocalDate toDate) throws SQLException {
        if (contractIds == null || contractIds.isEmpty()) return Collections.emptyMap();

        String placeholders = String.join(",", Collections.nCopies(contractIds.size(), "?"));
        StringBuilder sql = new StringBuilder("""
                SELECT COALESCE(ws.currency_code, ct.currency_code) AS currency_code,
                       COALESCE(SUM(COALESCE(ws.final_amount, 0)), 0) AS total_amount
                FROM contracts ct
                JOIN work_sessions ws ON ws.contract_id = ct.id
                WHERE ws.status = 'STOPPED'
                  AND ct.id IN (
                """).append(placeholders).append(")");

        if (fromDate != null) sql.append(" AND ws.start_time >= ?");
        if (toDate != null) sql.append(" AND ws.start_time < ?");
        sql.append(" GROUP BY COALESCE(ws.currency_code, ct.currency_code) ORDER BY currency_code");

        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            int index = 1;
            for (Long contractId : contractIds) ps.setLong(index++, contractId);
            if (fromDate != null) ps.setTimestamp(index++, Timestamp.valueOf(fromDate.atStartOfDay()));
            if (toDate != null) {
                LocalDateTime exclusiveEnd = toDate.plusDays(1).atStartOfDay();
                ps.setTimestamp(index, Timestamp.valueOf(exclusiveEnd));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String currency = rs.getString("currency_code");
                    BigDecimal amount = rs.getBigDecimal("total_amount");
                    totals.put(currency == null || currency.isBlank() ? "USD" : currency, amount == null ? BigDecimal.ZERO : amount);
                }
            }
        }
        return totals;
    }

    private Contract map(ResultSet rs) throws SQLException {
        Contract c = new Contract();
        c.setId(rs.getLong("id"));
        c.setClientId(rs.getLong("client_id"));
        c.setClientName(rs.getString("client_name"));
        c.setTitle(rs.getString("title"));
        c.setDescription(rs.getString("description"));
        c.setStatus(ContractStatus.valueOf(rs.getString("status")));
        c.setCurrentHourlyRate(rs.getBigDecimal("current_hourly_rate"));
        c.setCurrencyCode(rs.getString("currency_code"));
        c.setCreatedAt(ldt(rs.getTimestamp("created_at")));
        c.setUpdatedAt(ldt(rs.getTimestamp("updated_at")));
        return c;
    }
}
