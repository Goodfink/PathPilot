package org.example.pathpilot.repository;

import org.example.pathpilot.model.folder.RootFolderInfo;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.nio.file.Path;
import java.util.List;

@Repository
public class RootFoldersRepository {

    private final JdbcTemplate jdbcTemplate;

    public RootFoldersRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void populateRootFolders(List<String> rootFolderPaths) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO root_folders(path) VALUES (?)",
                rootFolderPaths,
                rootFolderPaths.size(),
                (ps, path) -> ps.setString(1, path)
        );
    }

    public List<RootFolderInfo> getRootFolders() {
        return jdbcTemplate.query(
                "SELECT id, path FROM root_folders",
                (rs, rowNum) -> {
                    RootFolderInfo rootFolder = RootFolderInfo.builder()
                            .id(rs.getLong("id"))
                            .path(Path.of(rs.getString("path")))
                            .build();
                    return rootFolder;
                }
        );
    }
}
