package com.environment.quiz.dto.auth;

import java.time.LocalDateTime;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String role,
        LocalDateTime memberSince
) {}