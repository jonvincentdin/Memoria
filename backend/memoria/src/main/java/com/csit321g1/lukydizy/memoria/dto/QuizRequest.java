package com.csit321g1.lukydizy.memoria.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Body for creating or updating a quiz. */
public record QuizRequest(
    @NotBlank(message = "Title is required.")
    @Size(max = 150, message = "Title must be 150 characters or fewer.")
    String title,

    // Only practice (multiple choice) quizzes exist for now.
    @NotBlank(message = "Mode is required.")
    @Pattern(regexp = "PRACTICE", message = "Mode must be PRACTICE.")
    String mode,

    // @Valid makes the rules inside QuizQuestion run for every question.
    @NotNull(message = "Questions are required.")
    @Size(min = 1, max = 20, message = "A quiz needs 1 to 20 questions.")
    List<@Valid @NotNull(message = "Questions cannot be empty.") QuizQuestion> questions,

    // At least one reviewer; ownership is checked in QuizService.
    @NotEmpty(message = "Select at least one reviewer.")
    List<@NotBlank(message = "Reviewer IDs cannot be blank.") String> reviewerIds
) {
}
