package com.bookmarklink.backend.model;

public class NotificationSummary {
    private int expiringCount;
    private boolean emailSent;
    private String message;

    public NotificationSummary() {
    }

    public NotificationSummary(int expiringCount, boolean emailSent, String message) {
        this.expiringCount = expiringCount;
        this.emailSent = emailSent;
        this.message = message;
    }

    public int getExpiringCount() {
        return expiringCount;
    }

    public void setExpiringCount(int expiringCount) {
        this.expiringCount = expiringCount;
    }

    public boolean isEmailSent() {
        return emailSent;
    }

    public void setEmailSent(boolean emailSent) {
        this.emailSent = emailSent;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
