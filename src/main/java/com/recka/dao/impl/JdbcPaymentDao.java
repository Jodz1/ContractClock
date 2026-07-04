package com.recka.dao.impl;

import com.recka.dao.PaymentDao;
import com.recka.model.Payment;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

public class JdbcPaymentDao extends JdbcSupport implements PaymentDao {
    @Override
    public Payment save(Payment p) throws SQLException {
        String sql = "INSERT INTO payments(contract_id, amount, currency_code, payment_date, note) VALUES(?,?,?,?,?)";
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, p.getContractId());
            ps.setBigDecimal(2, p.getAmount());
            ps.setString(3, p.getCurrencyCode());
            ps.setDate(4, date(p.getPaymentDate()));
            ps.setString(5, p.getNote());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getLong(1));
            }
            return p;
        }
    }

    @Override
    public List<Payment> findByContract(Long contractId) throws SQLException {
        List<Payment> out = new ArrayList<>();
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM payments WHERE contract_id=? ORDER BY payment_date DESC, id DESC")) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) out.add(map(rs));
            }
        }
        return out;
    }

    @Override
    public BigDecimal totalPaid(Long contractId) throws SQLException {
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement("SELECT COALESCE(SUM(amount),0) FROM payments WHERE contract_id=?")) {
            ps.setLong(1, contractId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
                return BigDecimal.ZERO;
            }
        }
    }

    private Payment map(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getLong("id"));
        p.setContractId(rs.getLong("contract_id"));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setCurrencyCode(rs.getString("currency_code"));
        p.setPaymentDate(ld(rs.getDate("payment_date")));
        p.setNote(rs.getString("note"));
        p.setCreatedAt(ldt(rs.getTimestamp("created_at")));
        return p;
    }
}
