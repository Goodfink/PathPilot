package org.example.pathpilot.repository;

import org.example.pathpilot.model.llm.ClassificationResult;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;

@Repository
public class PendingMovesRepository {

    private final JdbcTemplate jdbcTemplate;

    public PendingMovesRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void insertPendingMove(ClassificationResult classificationResult, Path from_path) {
        jdbcTemplate.update("INSERT INTO pending_moves (from_path, to_path, confidence) VALUES (? ? ? ?)",
                (ps -> {
                    ps.setString(1, from_path.toString());
                    ps.setString(2, classificationResult.getPath().toString());
                    ps.setDouble(3, classificationResult.getConfidence());
                }));
    }
}
