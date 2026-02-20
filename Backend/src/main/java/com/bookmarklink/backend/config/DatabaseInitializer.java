package com.bookmarklink.backend.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.sql.DataSource;

@Component
public class DatabaseInitializer {
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final Path dbDirPath = Paths.get("Backend", "data");

    public DatabaseInitializer(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @PostConstruct
    public void initialize() {
        if (isSqlite()) {
            ensureDbDirectory();
        }
        createTables();
        ensureOwnerIdColumn();
    }

    private boolean isSqlite() {
        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            return url != null && url.startsWith("jdbc:sqlite");
        } catch (SQLException ignored) {
            return false;
        }
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
        if (columnExists("links", "owner_id")) {
            return;
        }

        jdbcTemplate.execute("ALTER TABLE links ADD COLUMN owner_id TEXT NOT NULL DEFAULT ''");
    }

    private boolean columnExists(String tableName, String columnName) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            if (columnExists(metaData, tableName, columnName)) {
                return true;
            }
            return columnExists(metaData, tableName.toUpperCase(), columnName.toUpperCase());
        } catch (SQLException ignored) {
            return false;
        }
    }

    private boolean columnExists(DatabaseMetaData metaData, String tableName, String columnName)
            throws SQLException {
        try (ResultSet resultSet = metaData.getColumns(null, null, tableName, columnName)) {
            return resultSet.next();
        }
    }
}
