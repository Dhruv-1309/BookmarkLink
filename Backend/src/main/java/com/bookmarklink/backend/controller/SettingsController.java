package com.bookmarklink.backend.controller;

import com.bookmarklink.backend.model.Settings;
import com.bookmarklink.backend.service.SettingsService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = "*")
public class SettingsController {
    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    public Settings get() {
        return settingsService.get();
    }

    @PutMapping
    public Settings update(@RequestBody Settings settings) {
        return settingsService.update(settings);
    }
}
