package org.example.pathpilot.repository;

import org.example.pathpilot.model.folder.FolderInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;

@Repository
public class FoldersRepository {

    private final JdbcTemplate jdbcTemplate;

    public FoldersRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void populateFolders(List<FolderInfo> folders) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO folders (name, path, parent_path, depth) VALUES (?, ?, ?, ?)",
                folders,
                folders.size(),
                (ps, folder) -> {
                    ps.setString(1, folder.getName());
                    ps.setString(2, folder.getPath().toString());
                    ps.setString(3, folder.getParentPath().toString());
                    ps.setInt(4, folder.getDepth());
                }
        );
    }

    public List<FolderInfo> getFolders() {
        return jdbcTemplate.query(
                "SELECT * FROM folders",
                (rs, rowNum) -> FolderInfo.builder()
                        .name(rs.getString("name"))
                        .path(Path.of(rs.getString("path")))
                        .id(rs.getLong("id"))
                        .parentPath(Path.of(rs.getString("parent_path")))
                        .depth(rs.getInt("depth"))
                        .build()
        );
    }

    public void insertFolder(FolderInfo folder) {
        jdbcTemplate.update("INSERT INTO folders (name, path, parent_path, depth) VALUES (?, ?, ?, ?)",
                (ps -> {
                    ps.setString(1, folder.getName());
                    ps.setString(2, folder.getPath().toString());
                    ps.setString(3, folder.getParentPath().toString());
                    ps.setInt(4, folder.getDepth());
                }));
    }

    public int deleteFolderByPath(Path folderPath) {
        return jdbcTemplate.update("DELETE FROM folders WHERE path = ?", folderPath.toString());
    }

    public FolderInfo getFolder(FolderInfo folder) {
        return jdbcTemplate.queryForObject(
                "SELECT * FROM folders WHERE id = ?",
                (rs, rowNum) -> FolderInfo.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .path(Path.of(rs.getString("path")))
                        .parentPath(Path.of(rs.getString("parent_path")))
                        .depth(rs.getInt("depth"))
                        .build(),
                folder.getId()
        );
    }

    public FolderInfo getFolderByPath(Path folderPath) {
        return jdbcTemplate.queryForObject("SELECT * FROM folders WHERE path = ?",
                (rs, rowNum) -> FolderInfo.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .path(Path.of(rs.getString("path")))
                        .parentPath(Path.of(rs.getString("parent_path")))
                        .depth(rs.getInt("depth"))
                        .build(),
                folderPath.toString()
        );
    }
}
