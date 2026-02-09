package com.bookmarklink.backend.controller;

import com.bookmarklink.backend.model.AuthRequest;
import com.bookmarklink.backend.model.AuthResponse;
import com.bookmarklink.backend.model.SignupRequest;
import com.bookmarklink.backend.model.User;
import com.bookmarklink.backend.model.UserPublic;
import com.bookmarklink.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        if (request == null || isBlank(request.getEmail()) || isBlank(request.getPassword())) {
            return ResponseEntity.badRequest().build();
        }

        Optional<User> user = userService.authenticate(request.getEmail(), request.getPassword());
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = userService.issueToken(user.get());
        UserPublic publicUser = userService.toPublic(user.get());
        return ResponseEntity.ok(new AuthResponse(token, publicUser));
    }

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest request) {
        if (request == null
                || isBlank(request.getName())
                || isBlank(request.getEmail())
                || isBlank(request.getPassword())) {
            return ResponseEntity.badRequest().build();
        }

        if (userService.emailExists(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = userService.createUser(request);
        String token = userService.issueToken(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(token, userService.toPublic(user)));
    }

    @GetMapping("/me")
    public ResponseEntity<UserPublic> me(@RequestHeader("X-Auth-Token") String token) {
        return userService.getUserByToken(token)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
