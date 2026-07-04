package com.recka.dao;

import com.recka.model.Payment;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public interface PaymentDao {
    Payment save(Payment payment) throws SQLException;
    List<Payment> findByContract(Long contractId) throws SQLException;
    BigDecimal totalPaid(Long contractId) throws SQLException;
}
