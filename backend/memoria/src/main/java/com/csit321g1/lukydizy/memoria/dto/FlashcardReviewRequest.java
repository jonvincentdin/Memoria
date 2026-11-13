package com.csit321g1.lukydizy.memoria.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Body for submitting a study rating on a flashcard. */
public record FlashcardReviewRequest(
    @NotNull(message = "Rating is required.")
    @Min(value = 1, message = "Rating must be between 1 and 5.")
    @Max(value = 5, message = "Rating must be between 1 and 5.")
    Integer rating
) {
}
