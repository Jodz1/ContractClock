package com.recka.dao.impl;

import com.recka.dao.ClientDao;
import com.recka.model.Client;
import java.sql.*;
import java.util.*;

public class JdbcClientDao extends JdbcSupport implements ClientDao {
    @Override
    public Client save(Client client) throws SQLException {
        String sql = "INSERT INTO clients(name, email, company_name, notes) VALUES(?,?,?,?)";
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, client.getName());
            ps.setString(2, client.getEmail());
            ps.setString(3, client.getCompanyName());
            ps.setString(4, client.getNotes());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) client.setId(rs.getLong(1));
            }
            return client;
        }
    }

    @Override
    public void update(Client client) throws SQLException {
        String sql = "UPDATE clients SET name=?, email=?, company_name=?, notes=? WHERE id=?";
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, client.getName());
            ps.setString(2, client.getEmail());
            ps.setString(3, client.getCompanyName());
            ps.setString(4, client.getNotes());
            ps.setLong(5, client.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(Long id) throws SQLException {
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement("DELETE FROM clients WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<Client> findById(Long id) throws SQLException {
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM clients WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    @Override
    public List<Client> findAll() throws SQLException {
        List<Client> out = new ArrayList<>();
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM clients ORDER BY name"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        }
        return out;
    }

    private Client map(ResultSet rs) throws SQLException {
        return new Client(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("email"),
                rs.getString("company_name"),
                rs.getString("notes"),
                ldt(rs.getTimestamp("created_at")),
                ldt(rs.getTimestamp("updated_at"))
        );
    }
}
