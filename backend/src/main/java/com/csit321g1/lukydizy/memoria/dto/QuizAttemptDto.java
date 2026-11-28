package com.csit321g1.lukydizy.memoria.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.csit321g1.lukydizy.memoria.entity.QuizAttemptEntity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public final class QuizAttemptDto {

    private QuizAttemptDto() {
    }

    /** Attempt result returned to the frontend. */
    public record AttemptResponse(
        String id,
        String quizId,
        String quizTitle,
        int score,
        int totalQuestions,
        double percentage,
        QuizAttemptEntity.Status status,
        LocalDateTime completedAt
    ) {
        /** Converts a saved attempt into a response. */
        public static AttemptResponse from(QuizAttemptEntity attempt) {
            return new AttemptResponse(
                attempt.getId(),
                attempt.getQuiz().getId(),
                attempt.getQuiz().getTitle(),
                attempt.getScore(),
                attempt.getTotalQuestions(),
                100.0 * attempt.getScore() / attempt.getTotalQuestions(),
                attempt.getStatus(),
                attempt.getCompletedAt()
            );
        }
    }

    /** Selected choices for a quiz; omitted questions are treated as unanswered. */
    public record AttemptSubmissionRequest(
        @NotNull(message = "Answers are required. Use an empty list for an unanswered quiz.")
        List<@Valid @NotNull(message = "Answer selections cannot be empty.") AnswerSelection> answers
    ) {
        /** Zero-based question and choice positions, matching the quiz's question data. */
        public record AnswerSelection(
            @NotNull(message = "Question index is required.")
            @Min(value = 0, message = "Question index cannot be negative.")
            Integer questionIndex,

            @NotNull(message = "Choice index is required.")
            @Min(value = 0, message = "Choice index cannot be negative.")
            Integer choiceIndex
        ) {
        }
    }
}
