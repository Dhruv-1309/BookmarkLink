package com.bookmarklink.backend.controller;

import com.bookmarklink.backend.model.NotificationSummary;
import com.bookmarklink.backend.service.NotificationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping("/run")
    public NotificationSummary runNow() {
        return notificationService.runNotifications("manual");
    }
}
