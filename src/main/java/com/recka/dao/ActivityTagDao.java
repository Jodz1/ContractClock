package com.recka.dao;

import com.recka.model.ActivityTag;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ActivityTagDao {
    ActivityTag save(ActivityTag tag) throws SQLException;
    List<ActivityTag> findAll() throws SQLException;
    Optional<ActivityTag> findById(Long id) throws SQLException;
}
