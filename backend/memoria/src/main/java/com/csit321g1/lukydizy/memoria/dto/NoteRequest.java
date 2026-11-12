package com.csit321g1.lukydizy.memoria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Body for creating or updating a note; owner and source type are set by the server. */
public record NoteRequest(
    @NotBlank(message = "Title is required.")
    @Size(max = 150, message = "Title must be 150 characters or fewer.")
    String title,

    @NotBlank(message = "Content is required.")
    @Size(max = 20000, message = "Content must be 20000 characters or fewer.")
    String content
) {
}
