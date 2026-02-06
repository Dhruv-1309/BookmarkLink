package com.bookmarklink.backend.model;

public class Settings {
    private String email;
    private String phone;
    private boolean emailEnabled = true;
    private boolean smsEnabled = false;
    private int reminderWindow = 5;

    public Settings() {
    }

    public Settings(String email, String phone, boolean emailEnabled, boolean smsEnabled, int reminderWindow) {
        this.email = email;
        this.phone = phone;
        this.emailEnabled = emailEnabled;
        this.smsEnabled = smsEnabled;
        this.reminderWindow = reminderWindow;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public boolean isSmsEnabled() {
        return smsEnabled;
    }

    public void setSmsEnabled(boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }

    public int getReminderWindow() {
        return reminderWindow;
    }

    public void setReminderWindow(int reminderWindow) {
        this.reminderWindow = reminderWindow;
    }
}
