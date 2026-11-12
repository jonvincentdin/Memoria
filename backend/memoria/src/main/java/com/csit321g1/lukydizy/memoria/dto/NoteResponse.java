package com.csit321g1.lukydizy.memoria.dto;

import java.time.LocalDateTime;

/** Public note fields returned to the frontend, without persistence relationships. */
public record NoteResponse(
    String id,
    String title,
    String content,
    String sourceType,
    LocalDateTime createdAt
) {
}
