package com.csit321g1.lukydizy.memoria.dto;

import java.time.LocalDateTime;

/** Public flashcard fields returned without persistence relationships. */
public record FlashcardResponse(
    String id,
    String reviewerId,
    String front,
    String back,
    LocalDateTime createdAt
) {
}
