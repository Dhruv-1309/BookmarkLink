package com.bookmarklink.backend.service;

import com.bookmarklink.backend.model.Link;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@DependsOn("databaseInitializer")
public class LinkService {
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<Link> linkRowMapper = (rs, rowNum) -> new Link(
            rs.getString("id"),
            rs.getString("title"),
            rs.getString("url"),
            parseTags(rs.getString("tags")),
            rs.getString("notes"),
            parseInstant(rs.getString("created_at")),
            rs.getString("status"),
            parseInstant(rs.getString("renewed_at")),
            parseInstant(rs.getString("archived_at")));

    public LinkService(ObjectMapper objectMapper, JdbcTemplate jdbcTemplate) {
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Link> getAllForNotifications() {
        List<Link> links = jdbcTemplate.query(
                "SELECT id, title, url, tags, notes, created_at, status, renewed_at, archived_at " +
                        "FROM links ORDER BY created_at DESC",
                linkRowMapper);
        for (Link link : links) {
            link.setOwnerId(null);
        }
        links.sort(Comparator.comparing(Link::getCreatedAt).reversed());
        return links;
    }

    public List<Link> getAllForUser(String ownerId) {
        List<Link> links = jdbcTemplate.query(
                "SELECT id, title, url, tags, notes, created_at, status, renewed_at, archived_at, owner_id " +
                        "FROM links WHERE owner_id = ? ORDER BY created_at DESC",
                (rs, rowNum) -> {
                    Link link = linkRowMapper.mapRow(rs, rowNum);
                    if (link != null) {
                        link.setOwnerId(rs.getString("owner_id"));
                    }
                    return link;
                },
                ownerId);
        links.sort(Comparator.comparing(Link::getCreatedAt).reversed());
        return links;
    }

    public Link create(String ownerId, Link input) {
        String id = UUID.randomUUID().toString();
        Instant now = Instant.now();
        Link link = new Link(
                id,
                input.getTitle(),
                input.getUrl(),
                input.getTags(),
                input.getNotes(),
                now,
                "active",
                null,
                null);
        link.setOwnerId(ownerId);
        insertLink(link);
        return link;
    }

    public Optional<Link> findById(String ownerId, String id) {
        List<Link> links = jdbcTemplate.query(
                "SELECT id, title, url, tags, notes, created_at, status, renewed_at, archived_at, owner_id " +
                        "FROM links WHERE id = ? AND owner_id = ?",
                (rs, rowNum) -> {
                    Link link = linkRowMapper.mapRow(rs, rowNum);
                    if (link != null) {
                        link.setOwnerId(rs.getString("owner_id"));
                    }
                    return link;
                },
                id,
                ownerId);
        return links.stream().findFirst();
    }

    public Optional<Link> renew(String ownerId, String id) {
        Instant now = Instant.now();
        int updated = jdbcTemplate.update(
                "UPDATE links SET renewed_at = ?, status = ? WHERE id = ? AND owner_id = ?",
                now.toString(),
                "active",
                id,
                ownerId);
        return updated > 0 ? findById(ownerId, id) : Optional.empty();
    }

    public Optional<Link> archive(String ownerId, String id) {
        Instant now = Instant.now();
        int updated = jdbcTemplate.update(
                "UPDATE links SET status = ?, archived_at = ? WHERE id = ? AND owner_id = ?",
                "archived",
                now.toString(),
                id,
                ownerId);
        return updated > 0 ? findById(ownerId, id) : Optional.empty();
    }

    public Optional<Link> restore(String ownerId, String id) {
        Instant now = Instant.now();
        int updated = jdbcTemplate.update(
                "UPDATE links SET status = ?, renewed_at = ?, archived_at = NULL WHERE id = ? AND owner_id = ?",
                "active",
                now.toString(),
                id,
                ownerId);
        return updated > 0 ? findById(ownerId, id) : Optional.empty();
    }

    public boolean delete(String ownerId, String id) {
        return jdbcTemplate.update("DELETE FROM links WHERE id = ? AND owner_id = ?", id, ownerId) > 0;
    }

    private void insertLink(Link link) {
        jdbcTemplate.update(
                "INSERT INTO links (id, owner_id, title, url, tags, notes, created_at, status, renewed_at, archived_at) "
                        +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                link.getId(),
                link.getOwnerId(),
                link.getTitle(),
                link.getUrl(),
                toTagsJson(link.getTags()),
                link.getNotes(),
                toIso(link.getCreatedAt()),
                link.getStatus(),
                toIso(link.getRenewedAt()),
                toIso(link.getArchivedAt()));
    }

    private String toIso(Instant instant) {
        return instant == null ? null : instant.toString();
    }

    private Instant parseInstant(String value) {
        return value == null || value.isBlank() ? null : Instant.parse(value);
    }

    private List<String> parseTags(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<List<String>>() {
            });
        } catch (IOException ignored) {
            return new ArrayList<>();
        }
    }

    private String toTagsJson(List<String> tags) {
        try {
            return objectMapper.writeValueAsString(tags == null ? new ArrayList<>() : tags);
        } catch (IOException ignored) {
            return "[]";
        }
    }
}
