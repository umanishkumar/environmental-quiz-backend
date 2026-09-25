package com.environment.quiz.dto.auth;

public record AuthResponse(
        String token,
        String username,
        String role
) {}