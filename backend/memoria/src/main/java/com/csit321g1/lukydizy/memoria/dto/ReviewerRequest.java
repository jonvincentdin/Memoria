package com.csit321g1.lukydizy.memoria.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

/** Body for creating or updating a reviewer. noteIds is the full list of linked notes. */
public record ReviewerRequest(
    @NotBlank(message = "Title is required.")
    @Size(max = 200, message = "Title must be 200 characters or fewer.")
    String title,

    @NotBlank(message = "Style is required.")
    @Size(max = 50, message = "Style must be 50 characters or fewer.")
    String style,

    @NotBlank(message = "Content is required.")
    String content,

    // A reviewer must be linked to at least one note.
    @NotEmpty(message = "Select at least one note.")
    List<@NotBlank(message = "Note IDs cannot be blank.") String> noteIds
) {
}
