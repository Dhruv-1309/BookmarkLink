package com.bookmarklink.backend.service;

import com.bookmarklink.backend.model.Settings;
import org.springframework.stereotype.Service;

@Service
public class SettingsService {
    private Settings settings = new Settings();

    public Settings get() {
        return settings;
    }

    public Settings update(Settings incoming) {
        if (incoming == null) {
            return settings;
        }
        settings.setEmail(incoming.getEmail());
        settings.setPhone(incoming.getPhone());
        settings.setEmailEnabled(incoming.isEmailEnabled());
        settings.setSmsEnabled(incoming.isSmsEnabled());
        settings.setReminderWindow(incoming.getReminderWindow());
        return settings;
    }
}
