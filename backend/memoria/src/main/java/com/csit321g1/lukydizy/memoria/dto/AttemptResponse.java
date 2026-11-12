package com.csit321g1.lukydizy.memoria.dto;

import java.time.LocalDateTime;

import com.csit321g1.lukydizy.memoria.entity.AttemptStatus;
import com.csit321g1.lukydizy.memoria.entity.QuizAttemptEntity;

/** Attempt result returned to the frontend. */
public record AttemptResponse(
    String id,
    String quizId,
    String quizTitle,
    int score,
    int totalQuestions,
    AttemptStatus status,
    LocalDateTime completedAt
) {

    /** Converts a saved attempt into a response. */
    public static AttemptResponse from(QuizAttemptEntity attempt) {
        // Copy only what the frontend needs; the user record is never exposed.
        return new AttemptResponse(
            attempt.getId(),
            attempt.getQuiz().getId(),
            attempt.getQuiz().getTitle(),
            attempt.getScore(),
            attempt.getTotalQuestions(),
            attempt.getStatus(),
            attempt.getCompletedAt()
        );
    }
}
