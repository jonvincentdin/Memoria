package com.csit321g1.lukydizy.memoria.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Quiz as the owner sees it (authoring view, includes the answers).
 * The student-facing view that hides answers comes in BE-06.
 */
public record QuizResponse(
    String id,
    String title,
    String mode,
    List<QuizQuestion> questions,
    LocalDateTime createdAt,
    // True once the quiz has attempts: the frontend should show the questions as read-only.
    boolean hasAttempts,
    List<LinkedReviewer> reviewers
) {
    /** A reviewer linked to the quiz. */
    public record LinkedReviewer(String id, String title) {
    }
}
