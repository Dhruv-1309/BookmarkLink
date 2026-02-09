package com.bookmarklink.backend.service;

import com.bookmarklink.backend.model.SignupRequest;
import com.bookmarklink.backend.model.User;
import com.bookmarklink.backend.model.UserPublic;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {
    private static final String DEFAULT_EMAIL = "demo@bookmarklink.com";
    private static final String DEFAULT_PASSWORD = "demo123";
    private static final String DEFAULT_NAME = "Demo User";

    private final ObjectMapper objectMapper;
    private final Path usersPath;
    private final Map<String, String> tokenToUserId = new ConcurrentHashMap<>();
    private List<User> cachedUsers = new ArrayList<>();

    public UserService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.usersPath = Paths.get("data", "users.json");
        this.cachedUsers = loadUsers();
    }

    public synchronized Optional<User> authenticate(String email, String password) {
        return cachedUsers.stream()
                .filter(user -> user.getEmail().equalsIgnoreCase(email))
                .filter(user -> user.getPassword().equals(password))
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
                request.getPassword());
        cachedUsers.add(user);
        saveUsers();
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
        try {
            if (Files.notExists(usersPath)) {
                seedDefaultUser();
            }

            List<User> users = objectMapper.readValue(usersPath.toFile(), new TypeReference<List<User>>() {
            });
            if (users == null || users.isEmpty()) {
                seedDefaultUser();
                return objectMapper.readValue(usersPath.toFile(), new TypeReference<List<User>>() {
                });
            }
            return users;
        } catch (IOException ex) {
            seedDefaultUser();
            try {
                return objectMapper.readValue(usersPath.toFile(), new TypeReference<List<User>>() {
                });
            } catch (IOException ignored) {
                return new ArrayList<>();
            }
        }
    }

    private void saveUsers() {
        try {
            if (Files.notExists(usersPath.getParent())) {
                Files.createDirectories(usersPath.getParent());
            }
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(usersPath.toFile(), cachedUsers);
        } catch (IOException ignored) {
        }
    }

    private void seedDefaultUser() {
        try {
            if (Files.notExists(usersPath.getParent())) {
                Files.createDirectories(usersPath.getParent());
            }
            List<User> seed = new ArrayList<>();
            seed.add(new User(UUID.randomUUID().toString(), DEFAULT_NAME, DEFAULT_EMAIL, DEFAULT_PASSWORD));
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(usersPath.toFile(), seed);
        } catch (IOException ignored) {
        }
    }
}
