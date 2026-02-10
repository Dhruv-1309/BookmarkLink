package com.bookmarklink.backend.service;

import com.bookmarklink.backend.model.SignupRequest;
import com.bookmarklink.backend.model.User;
import com.bookmarklink.backend.model.UserPublic;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@DependsOn("databaseInitializer")
public class UserService {
    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Map<String, String> tokenToUserId = new ConcurrentHashMap<>();
    private List<User> cachedUsers = new ArrayList<>();
    private final RowMapper<User> userRowMapper = (rs, rowNum) -> new User(
            rs.getString("id"),
            rs.getString("name"),
            rs.getString("email"),
            rs.getString("password"));

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.cachedUsers = loadUsers();
    }

    public synchronized Optional<User> authenticate(String email, String password) {
        return cachedUsers.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .findFirst();
    }

    public synchronized boolean emailExists(String email) {
        return cachedUsers.stream()
                .anyMatch(user -> user.getEmail().equalsIgnoreCase(email));
    }

    public synchronized User createUser(SignupRequest request) {
        User user = new User(
                UUID.randomUUID().toString(),
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()));
        jdbcTemplate.update(
                "INSERT INTO users (id, name, email, password) VALUES (?, ?, ?, ?)",
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPassword());
        cachedUsers.add(user);
        return user;
    }

    public String issueToken(User user) {
        String token = UUID.randomUUID().toString();
        tokenToUserId.put(token, user.getId());
        return token;
    }

    public boolean isValidToken(String token) {
        return token != null && tokenToUserId.containsKey(token);
    }

    public Optional<String> getUserIdForToken(String token) {
        if (token == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(tokenToUserId.get(token));
    }

    public Optional<UserPublic> getUserByToken(String token) {
        String userId = tokenToUserId.get(token);
        if (userId == null) {
            return Optional.empty();
        }
        return cachedUsers.stream()
                .filter(user -> user.getId().equals(userId))
                .findFirst()
                .map(this::toPublic);
    }

    public UserPublic toPublic(User user) {
        return new UserPublic(user.getId(), user.getName(), user.getEmail());
    }

    private synchronized List<User> loadUsers() {
        List<User> users = jdbcTemplate.query("SELECT id, name, email, password FROM users", userRowMapper);
        boolean updated = ensureHashedPasswords(users);
        if (updated) {
            cachedUsers = users;
        }
        return users;
    }

    private boolean ensureHashedPasswords(List<User> users) {
        boolean updated = false;
        for (User user : users) {
            String password = user.getPassword();
            if (password == null || password.startsWith("$2")) {
                continue;
            }
            String hashed = passwordEncoder.encode(password);
            user.setPassword(hashed);
            jdbcTemplate.update("UPDATE users SET password = ? WHERE id = ?", hashed, user.getId());
            updated = true;
        }
        return updated;
    }
}
