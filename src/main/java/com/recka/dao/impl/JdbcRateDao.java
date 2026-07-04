package com.recka.dao.impl;

import com.recka.dao.RateDao;
import com.recka.model.ContractRate;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

public class JdbcRateDao extends JdbcSupport implements RateDao {
    @Override
    public ContractRate save(ContractRate rate) throws SQLException {
        String sql = "INSERT INTO contract_rates(contract_id, hourly_rate, currency_code, valid_from, valid_to, note) VALUES(?,?,?,?,?,?)";
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, rate.getContractId());
            ps.setBigDecimal(2, rate.getHourlyRate());
            ps.setString(3, rate.getCurrencyCode());
            ps.setTimestamp(4, ts(rate.getValidFrom()));
            ps.setTimestamp(5, ts(rate.getValidTo()));
            ps.setString(6, rate.getNote());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) rate.setId(rs.getLong(1));
            }
            return rate;
        }
    }

    @Override
    public Optional<ContractRate> findActiveRate(Long contractId) throws SQLException {
        String sql = "SELECT * FROM contract_rates WHERE contract_id=? AND valid_to IS NULL ORDER BY valid_from DESC LIMIT 1";
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<ContractRate> findRateAt(Long contractId, LocalDateTime at) throws SQLException {
        String sql = """
                SELECT * FROM contract_rates
                WHERE contract_id=?
                  AND valid_from <= ?
                  AND (valid_to IS NULL OR valid_to > ?)
                ORDER BY valid_from DESC
                LIMIT 1
                """;
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, contractId);
            ps.setTimestamp(2, ts(at));
            ps.setTimestamp(3, ts(at));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return findActiveRate(contractId);
            }
        }
    }

    @Override
    public void closeActiveRate(Long contractId, LocalDateTime validTo) throws SQLException {
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement("UPDATE contract_rates SET valid_to=? WHERE contract_id=? AND valid_to IS NULL")) {
            ps.setTimestamp(1, ts(validTo));
            ps.setLong(2, contractId);
            ps.executeUpdate();
        }
    }

    private ContractRate map(ResultSet rs) throws SQLException {
        ContractRate r = new ContractRate();
        r.setId(rs.getLong("id"));
        r.setContractId(rs.getLong("contract_id"));
        r.setHourlyRate(rs.getBigDecimal("hourly_rate"));
        r.setCurrencyCode(rs.getString("currency_code"));
        r.setValidFrom(ldt(rs.getTimestamp("valid_from")));
        r.setValidTo(ldt(rs.getTimestamp("valid_to")));
        r.setNote(rs.getString("note"));
        r.setCreatedAt(ldt(rs.getTimestamp("created_at")));
        return r;
    }
}
