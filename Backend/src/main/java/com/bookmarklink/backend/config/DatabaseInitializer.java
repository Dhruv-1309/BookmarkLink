package com.bookmarklink.backend.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Component
public class DatabaseInitializer {
    private final JdbcTemplate jdbcTemplate;
    private final Path dbDirPath = Paths.get("Backend", "data");

    public DatabaseInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initialize() {
        ensureDbDirectory();
        createTables();
        ensureOwnerIdColumn();
    }

    private void ensureDbDirectory() {
        try {
            if (Files.notExists(dbDirPath)) {
                Files.createDirectories(dbDirPath);
            }
        } catch (IOException ignored) {
        }
    }

    private void createTables() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (" +
                "id TEXT PRIMARY KEY, " +
                "name TEXT NOT NULL, " +
                "email TEXT NOT NULL UNIQUE, " +
                "password TEXT NOT NULL" +
                ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS links (" +
                "id TEXT PRIMARY KEY, " +
                "owner_id TEXT NOT NULL, " +
                "title TEXT NOT NULL, " +
                "url TEXT NOT NULL, " +
                "tags TEXT, " +
                "notes TEXT, " +
                "created_at TEXT, " +
                "status TEXT, " +
                "renewed_at TEXT, " +
                "archived_at TEXT" +
                ")");
    }

    private void ensureOwnerIdColumn() {
        List<Map<String, Object>> columns = jdbcTemplate.queryForList("PRAGMA table_info(links)");
        for (Map<String, Object> column : columns) {
            Object name = column.get("name");
            if (name != null && "owner_id".equalsIgnoreCase(name.toString())) {
                return;
            }
        }

        jdbcTemplate.execute("ALTER TABLE links ADD COLUMN owner_id TEXT NOT NULL DEFAULT ''");
    }
}
