package com.bookmarklink.backend.config;

import com.bookmarklink.backend.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {
    private final UserService userService;

    public AuthInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!path.startsWith("/api")) {
            return true;
        }

        if (path.startsWith("/api/auth")) {
            return true;
        }

        String token = request.getHeader("X-Auth-Token");
        if (!userService.isValidToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }

        userService.getUserIdForToken(token).ifPresent(userId -> request.setAttribute("userId", userId));

        return true;
    }
}
