package com.bookmarklink.backend.model;

public class AuthResponse {
    private String token;
    private UserPublic user;

    public AuthResponse() {
    }

    public AuthResponse(String token, UserPublic user) {
        this.token = token;
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserPublic getUser() {
        return user;
    }

    public void setUser(UserPublic user) {
        this.user = user;
    }
}
