package com.csit321g1.lukydizy.memoria.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * One multiple-choice question, stored inside QuizEntity.questions as JSON.
 * A single correctIndex (0-3) means there is always exactly one correct choice.
 */
public record QuizQuestion(
    @NotBlank(message = "Question text is required.")
    @Size(max = 1000, message = "Question text must be 1,000 characters or fewer.")
    String text,

    // Exactly 4 non-empty choices, each up to 300 characters.
    @NotNull(message = "Choices are required.")
    @Size(min = 4, max = 4, message = "Each question needs exactly 4 choices.")
    List<@NotBlank(message = "Choices cannot be empty.")
         @Size(max = 300, message = "Choices must be 300 characters or fewer.") String> choices,

    // Position of the correct answer in choices.
    @NotNull(message = "Pick the correct choice.")
    @Min(value = 0, message = "Correct choice must be between 0 and 3.")
    @Max(value = 3, message = "Correct choice must be between 0 and 3.")
    Integer correctIndex
) {
}
