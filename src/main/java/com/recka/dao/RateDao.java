package com.recka.dao;

import com.recka.model.ContractRate;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public interface RateDao {
    ContractRate save(ContractRate rate) throws SQLException;
    Optional<ContractRate> findActiveRate(Long contractId) throws SQLException;
    Optional<ContractRate> findRateAt(Long contractId, LocalDateTime at) throws SQLException;
    void closeActiveRate(Long contractId, LocalDateTime validTo) throws SQLException;
}
