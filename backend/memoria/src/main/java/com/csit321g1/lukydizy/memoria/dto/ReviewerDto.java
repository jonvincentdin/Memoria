package com.csit321g1.lukydizy.memoria.dto;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public final class ReviewerDto {

    private ReviewerDto() {
    }

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

        @NotEmpty(message = "Select at least one note.")
        List<@NotBlank(message = "Note IDs cannot be blank.") String> noteIds
    ) {
    }

    /** Reviewer returned to the frontend, including its linked notes. */
    public record ReviewerResponse(
        String id,
        String title,
        String style,
        String content,
        LocalDateTime createdAt,
        List<ReviewerNoteDto.LinkedNote> notes
    ) {
    }
}
