package com.bookmarklink.backend.service;

import com.bookmarklink.backend.model.Link;
import com.bookmarklink.backend.model.NotificationSummary;
import com.bookmarklink.backend.model.Settings;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {
    private static final int HEALTH_DAYS = 45;

    private final LinkService linkService;
    private final SettingsService settingsService;
    private final JavaMailSender mailSender;

    public NotificationService(
            LinkService linkService,
            SettingsService settingsService,
            JavaMailSender mailSender) {
        this.linkService = linkService;
        this.settingsService = settingsService;
        this.mailSender = mailSender;
    }

    @Scheduled(cron = "${bookmarklink.notifications.cron:0 0 9 * * *}")
    public void scheduledReminders() {
        runNotifications("scheduled");
    }

    public NotificationSummary runNotifications(String source) {
        Settings settings = settingsService.get();
        List<Link> expiring = findExpiringLinks(settings.getReminderWindow());

        if (expiring.isEmpty()) {
            return new NotificationSummary(0, false, "No expiring links found (" + source + ").");
        }

        if (!settings.isEmailEnabled()) {
            return new NotificationSummary(expiring.size(), false, "Email reminders disabled in settings.");
        }

        String recipient = settings.getEmail();
        if (recipient == null || recipient.isBlank()) {
            return new NotificationSummary(expiring.size(), false, "Email address is missing in settings.");
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipient);
            message.setSubject("BookmarkLink reminders: " + expiring.size() + " link(s) expiring");
            message.setText(buildEmailBody(expiring, settings.getReminderWindow()));
            mailSender.send(message);
            return new NotificationSummary(expiring.size(), true, "Email reminder sent to " + recipient + ".");
        } catch (Exception ex) {
            return new NotificationSummary(expiring.size(), false, "Failed to send email: " + ex.getMessage());
        }
    }

    private List<Link> findExpiringLinks(int reminderWindow) {
        List<Link> expiring = new ArrayList<>();
        Instant now = Instant.now();

        for (Link link : linkService.getAllForNotifications()) {
            if (!"active".equalsIgnoreCase(link.getStatus())) {
                continue;
            }

            Instant base = link.getRenewedAt() != null ? link.getRenewedAt() : link.getCreatedAt();
            if (base == null) {
                continue;
            }

            long ageDays = Duration.between(base, now).toDays();
            long daysLeft = HEALTH_DAYS - ageDays;
            if (daysLeft > 0 && daysLeft <= reminderWindow) {
                expiring.add(link);
            }
        }

        return expiring;
    }

    private String buildEmailBody(List<Link> expiring, int reminderWindow) {
        StringBuilder body = new StringBuilder();
        body.append("Your BookmarkLink reminders (within ").append(reminderWindow).append(" day(s)):\n\n");

        for (Link link : expiring) {
            body.append("- ").append(link.getTitle()).append("\n");
            body.append("  URL: ").append(link.getUrl()).append("\n");
        }

        body.append("\nOpen the app to renew or archive these links.");
        return body.toString();
    }

}
