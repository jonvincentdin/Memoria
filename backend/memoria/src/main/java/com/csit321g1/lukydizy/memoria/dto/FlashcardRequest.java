package com.csit321g1.lukydizy.memoria.dto;

import jakarta.validation.constraints.NotBlank;

/** Body for creating or updating a flashcard; ownership and creation time are set by the server. */
public record FlashcardRequest(
    @NotBlank(message = "Reviewer ID is required.")
    String reviewerId,

    @NotBlank(message = "Front is required.")
    String front,

    @NotBlank(message = "Back is required.")
    String back
) {
}
