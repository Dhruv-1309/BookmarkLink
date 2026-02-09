package com.bookmarklink.backend.model;

public class Settings {
    private String email;
    private boolean emailEnabled = true;
    private int reminderWindow = 5;

    public Settings() {
    }

    public Settings(String email, boolean emailEnabled, int reminderWindow) {
        this.email = email;
        this.emailEnabled = emailEnabled;
        this.reminderWindow = reminderWindow;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public int getReminderWindow() {
        return reminderWindow;
    }

    public void setReminderWindow(int reminderWindow) {
        this.reminderWindow = reminderWindow;
    }
}
