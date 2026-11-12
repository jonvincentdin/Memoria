package com.csit321g1.lukydizy.memoria.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

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
