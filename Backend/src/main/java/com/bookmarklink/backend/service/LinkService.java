package com.bookmarklink.backend.service;

import com.bookmarklink.backend.model.Link;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LinkService {
    private final Map<String, Link> store = new ConcurrentHashMap<>();

    public LinkService() {
        seed();
    }

    public List<Link> getAll() {
        List<Link> links = new ArrayList<>(store.values());
        links.sort(Comparator.comparing(Link::getCreatedAt).reversed());
        return links;
    }

    public Link create(Link input) {
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
        store.put(id, link);
        return link;
    }

    public Optional<Link> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<Link> renew(String id) {
        Link link = store.get(id);
        if (link == null) {
            return Optional.empty();
        }
        link.setRenewedAt(Instant.now());
        link.setStatus("active");
        return Optional.of(link);
    }

    public Optional<Link> archive(String id) {
        Link link = store.get(id);
        if (link == null) {
            return Optional.empty();
        }
        link.setStatus("archived");
        link.setArchivedAt(Instant.now());
        return Optional.of(link);
    }

    public Optional<Link> restore(String id) {
        Link link = store.get(id);
        if (link == null) {
            return Optional.empty();
        }
        link.setStatus("active");
        link.setRenewedAt(Instant.now());
        link.setArchivedAt(null);
        return Optional.of(link);
    }

    public boolean delete(String id) {
        return store.remove(id) != null;
    }

    private void seed() {
        Instant now = Instant.now();
        Link first = new Link(
                UUID.randomUUID().toString(),
                "Design system typography",
                "https://example.com/typography",
                List.of("design", "ui"),
                "Use for heading scale",
                now.minusSeconds(60L * 60 * 24 * 6),
                "active",
                null,
                null);
        Link second = new Link(
                UUID.randomUUID().toString(),
                "Next.js routing cheatsheet",
                "https://example.com/nextjs",
                List.of("frontend", "docs"),
                "",
                now.minusSeconds(60L * 60 * 24 * 40),
                "active",
                null,
                null);
        Link third = new Link(
                UUID.randomUUID().toString(),
                "AI prompts library",
                "https://example.com/prompts",
                List.of("ai"),
                "Review weekly",
                now.minusSeconds(60L * 60 * 24 * 52),
                "archived",
                now.minusSeconds(60L * 60 * 24 * 10),
                now.minusSeconds(60L * 60 * 24 * 10));
        store.put(first.getId(), first);
        store.put(second.getId(), second);
        store.put(third.getId(), third);
    }
}
