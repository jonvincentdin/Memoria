package com.csit321g1.lukydizy.memoria.flashcards;

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

    /** Public flashcard fields including spaced-repetition state, returned without persistence relationships. */
    public record Response(
        String id,
        String reviewerId,
        String front,
        String back,
        LocalDateTime createdAt,
        int repetitionCount,
        int intervalDays,
        double easeFactor,
        Integer lastRating,
        LocalDateTime lastReviewedAt,
        LocalDateTime nextReviewAt,
        boolean due,
        String status
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

    /** Persisted study review result including the updated spaced-repetition schedule. */
    public record ReviewResponse(
        String reviewId,
        String flashcardId,
        int rating,
        int repetitionCount,
        int intervalDays,
        double easeFactor,
        LocalDateTime reviewedAt,
        LocalDateTime nextReviewAt,
        String status
    ) {
    }

    /** One recorded flashcard review entry returned in newest-first history lists. */
    public record ReviewHistoryItem(
        String id,
        String flashcardId,
        String reviewerId,
        String reviewerTitle,
        String front,
        int rating,
        int intervalDays,
        double easeFactor,
        LocalDateTime reviewedAt,
        LocalDateTime nextReviewAt
    ) {
    }

    /** Aggregated flashcard study progress across all reviewers or a single reviewer. */
    public record ProgressSummary(
        long totalCards,
        long dueCards,
        long newCards,
        long learningCards,
        long masteredCards,
        long totalReviews,
        long reviewsToday,
        Double averageRating,
        LocalDateTime lastReviewedAt,
        LocalDateTime nextDueAt
    ) {
    }
}
