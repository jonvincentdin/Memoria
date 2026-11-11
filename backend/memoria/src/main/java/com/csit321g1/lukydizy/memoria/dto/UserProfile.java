package com.csit321g1.lukydizy.memoria.dto;

import java.time.LocalDateTime;

public record UserProfile(String userId, String name, String email, LocalDateTime createdAt) {
}
