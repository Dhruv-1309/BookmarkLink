package com.bookmarklink.backend.controller;

import com.bookmarklink.backend.model.Link;
import com.bookmarklink.backend.service.LinkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/links")
@CrossOrigin(origins = "*")
public class LinkController {
    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping
    public List<Link> getAll() {
        return linkService.getAll();
    }

    @PostMapping
    public ResponseEntity<Link> create(@RequestBody Link link) {
        if (link.getTitle() == null || link.getTitle().isBlank() || link.getUrl() == null || link.getUrl().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(linkService.create(link));
    }

    @PutMapping("/{id}/renew")
    public ResponseEntity<Link> renew(@PathVariable String id) {
        return linkService.renew(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<Link> archive(@PathVariable String id) {
        return linkService.archive(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/restore")
    public ResponseEntity<Link> restore(@PathVariable String id) {
        return linkService.restore(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (linkService.delete(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
