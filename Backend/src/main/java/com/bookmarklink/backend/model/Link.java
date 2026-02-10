package com.bookmarklink.backend.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Link {
    private String id;
    private String title;
    private String url;
    private List<String> tags = new ArrayList<>();
    private String notes;
    private Instant createdAt;
    private String status;
    private Instant renewedAt;
    private Instant archivedAt;
    private String ownerId;

    public Link() {
    }

    public Link(String id, String title, String url, List<String> tags, String notes,
            Instant createdAt, String status, Instant renewedAt, Instant archivedAt) {
        this.id = id;
        this.title = title;
        this.url = url;
        if (tags != null) {
            this.tags = new ArrayList<>(tags);
        }
        this.notes = notes;
        this.createdAt = createdAt;
        this.status = status;
        this.renewedAt = renewedAt;
        this.archivedAt = archivedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getRenewedAt() {
        return renewedAt;
    }

    public void setRenewedAt(Instant renewedAt) {
        this.renewedAt = renewedAt;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(Instant archivedAt) {
        this.archivedAt = archivedAt;
    }

    @JsonIgnore
    public String getOwnerId() {
        return ownerId;
    }

    @JsonIgnore
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
