package org.example.pathpilot.repository;

import org.example.pathpilot.model.pendingMove.OperationType;
import org.example.pathpilot.model.pendingMove.PendingMove;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.sql.Statement;
import java.util.List;

@Repository
public class PendingMovesRepository {

    private final JdbcTemplate jdbcTemplate;

    public PendingMovesRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int insertPendingMove(PendingMove pendingMove) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(
                    "INSERT INTO pending_moves (from_path, to_path, confidence, file_name, operation_type) VALUES (?, ?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setString(1, pendingMove.getFromPath().toString());

            if (pendingMove.getToPath() != null) {
                ps.setString(2, pendingMove.getToPath().toString());
            } else {
                ps.setNull(2, java.sql.Types.VARCHAR);
            }

            ps.setDouble(3, pendingMove.getConfidence());
            ps.setString(4, pendingMove.getFileName());
            ps.setString(5, pendingMove.getOperationType().name());

            return ps;
        }, keyHolder);

        return keyHolder.getKey().intValue();
    }

    public List<PendingMove> getPendingMoves() {
        return jdbcTemplate.query("SELECT * FROM pending_moves",
                (rs, rowNum) -> PendingMove.builder()
                        .toPath(rs.getString("to_path") != null ? Path.of(rs.getString("to_path")) : null)
                        .fromPath(Path.of(rs.getString("from_path")))
                        .fileName(rs.getString("file_name"))
                        .confidence(rs.getDouble("confidence"))
                        .id(rs.getInt("id"))
                        .operationType(OperationType.valueOf(rs.getString("operation_type")))
                        .build());
    }

    public PendingMove getPendingMove(int id) {
        return jdbcTemplate.queryForObject("SELECT * FROM pending_moves WHERE id = ?",
                (rs, rowNum) -> PendingMove.builder()
                        .toPath(rs.getString("to_path") != null ? Path.of(rs.getString("to_path")) : null)
                        .fromPath(Path.of(rs.getString("from_path")))
                        .fileName(rs.getString("file_name"))
                        .confidence(rs.getDouble("confidence"))
                        .id(rs.getInt("id"))
                        .operationType(OperationType.valueOf(rs.getString("operation_type")))
                        .build(),
                id
        );

    }

    public void deletePendingMoveById(int id) {
        jdbcTemplate.update("DELETE FROM pending_moves WHERE id = ?", id);
    }
}

