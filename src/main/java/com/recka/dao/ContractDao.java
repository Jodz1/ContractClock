package com.recka.dao;

import com.recka.model.Contract;
import com.recka.model.ContractStatus;
import com.recka.model.DashboardContractRow;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ContractDao {
    Contract save(Contract contract) throws SQLException;
    void update(Contract contract) throws SQLException;
    void updateStatus(Long contractId, ContractStatus status) throws SQLException;
    void updateCurrentRate(Long contractId, java.math.BigDecimal rate, String currencyCode) throws SQLException;
    void delete(Long contractId) throws SQLException;
    Optional<Contract> findById(Long id) throws SQLException;
    List<DashboardContractRow> findDashboardRows(String search) throws SQLException;
    Map<String, BigDecimal> calculateEarnedTotals(List<Long> contractIds, LocalDate fromDate, LocalDate toDate) throws SQLException;
}
