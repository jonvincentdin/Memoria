package com.csit321g1.lukydizy.memoria.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class FlashcardDto {

    private FlashcardDto() {
    }

    /** Body for creating or updating a flashcard; ownership and creation time are set by the server. */
    public record Request(
        @NotBlank(message = "Reviewer ID is required.")
        String reviewerId,

        @NotBlank(message = "Front is required.")
        String front,

        @NotBlank(message = "Back is required.")
        String back
    ) {
    }

    /** Public flashcard fields returned without persistence relationships. */
    public record Response(
        String id,
        String reviewerId,
        String front,
        String back,
        LocalDateTime createdAt
    ) {
    }

    /** Body for submitting a study rating on a flashcard. */
    public record ReviewRequest(
        @NotNull(message = "Rating is required.")
        @Min(value = 1, message = "Rating must be between 1 and 5.")
        @Max(value = 5, message = "Rating must be between 1 and 5.")
        Integer rating
    ) {
    }

    /** Validated study result returned without scheduling or review-history persistence. */
    public record ReviewResponse(
        String flashcardId,
        int rating
    ) {
    }
}
