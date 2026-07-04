package com.recka.dao.impl;

import com.recka.db.DatabaseConnectionManager;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

abstract class JdbcSupport {
    protected Connection connection() throws SQLException {
        return DatabaseConnectionManager.getInstance().getConnection();
    }

    protected LocalDateTime ldt(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    protected Timestamp ts(LocalDateTime value) {
        return value == null ? null : Timestamp.valueOf(value);
    }

    protected Date date(LocalDate value) {
        return value == null ? null : Date.valueOf(value);
    }

    protected LocalDate ld(Date date) {
        return date == null ? null : date.toLocalDate();
    }
}
