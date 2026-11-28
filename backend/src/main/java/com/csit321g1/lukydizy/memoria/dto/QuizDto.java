package com.csit321g1.lukydizy.memoria.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class QuizDto {

    private QuizDto() {
    }

    /**
     * One multiple-choice question, stored inside QuizEntity.questions as JSON.
     * A single correctIndex (0-3) means there is always exactly one correct choice.
     */
    public record QuizQuestion(
        @NotBlank(message = "Question text is required.")
        @Size(max = 1000, message = "Question text must be 1,000 characters or fewer.")
        String text,

        @NotNull(message = "Choices are required.")
        @Size(min = 4, max = 4, message = "Each question needs exactly 4 choices.")
        List<@NotBlank(message = "Choices cannot be empty.")
             @Size(max = 300, message = "Choices must be 300 characters or fewer.") String> choices,

        @NotNull(message = "Pick the correct choice.")
        @Min(value = 0, message = "Correct choice must be between 0 and 3.")
        @Max(value = 3, message = "Correct choice must be between 0 and 3.")
        Integer correctIndex
    ) {
    }

    /** Body for creating or updating a quiz. */
    public record QuizRequest(
        @NotBlank(message = "Title is required.")
        @Size(max = 150, message = "Title must be 150 characters or fewer.")
        String title,

        @NotBlank(message = "Mode is required.")
        @Pattern(regexp = "PRACTICE", message = "Mode must be PRACTICE.")
        String mode,

        @NotNull(message = "Questions are required.")
        @Size(min = 1, max = 20, message = "A quiz needs 1 to 20 questions.")
        List<@Valid @NotNull(message = "Questions cannot be empty.") QuizQuestion> questions,

        @NotEmpty(message = "Select at least one reviewer.")
        List<@NotBlank(message = "Reviewer IDs cannot be blank.") String> reviewerIds
    ) {
    }

    /** Quiz as the owner sees it (authoring view, includes the answers). */
    public record QuizResponse(
        String id,
        String title,
        String mode,
        List<QuizQuestion> questions,
        LocalDateTime createdAt,
        boolean hasAttempts,
        List<QuizReviewerDto.LinkedReviewer> reviewers
    ) {
    }

    /** Student-facing quiz view without correct-answer keys. */
    public record QuizTakeResponse(String title, List<Question> questions) {
        /** A question and its choices, with no correct-answer metadata. */
        public record Question(String text, List<String> choices) {
        }
    }
}
