package com.recka.dao.impl;

import com.recka.dao.ActivityTagDao;
import com.recka.model.ActivityTag;
import java.sql.*;
import java.util.*;

public class JdbcActivityTagDao extends JdbcSupport implements ActivityTagDao {
    @Override
    public ActivityTag save(ActivityTag tag) throws SQLException {
        String sql = "INSERT INTO activity_tags(name, color_hex) VALUES(?,?)";
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, tag.getName());
            ps.setString(2, tag.getColorHex());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) tag.setId(rs.getLong(1));
            }
            return tag;
        }
    }

    @Override
    public List<ActivityTag> findAll() throws SQLException {
        List<ActivityTag> out = new ArrayList<>();
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM activity_tags ORDER BY name"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) out.add(map(rs));
        }
        return out;
    }

    @Override
    public Optional<ActivityTag> findById(Long id) throws SQLException {
        try (Connection c = connection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM activity_tags WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        }
    }

    private ActivityTag map(ResultSet rs) throws SQLException {
        ActivityTag tag = new ActivityTag();
        tag.setId(rs.getLong("id"));
        tag.setName(rs.getString("name"));
        tag.setColorHex(rs.getString("color_hex"));
        tag.setCreatedAt(ldt(rs.getTimestamp("created_at")));
        return tag;
    }
}
