package com.recka.dao;

import com.recka.model.Client;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ClientDao {
    Client save(Client client) throws SQLException;
    void update(Client client) throws SQLException;
    void delete(Long id) throws SQLException;
    Optional<Client> findById(Long id) throws SQLException;
    List<Client> findAll() throws SQLException;
}
