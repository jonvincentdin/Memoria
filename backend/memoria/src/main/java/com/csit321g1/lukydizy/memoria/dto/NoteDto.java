package com.csit321g1.lukydizy.memoria.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class NoteDto {

    private NoteDto() {
    }

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

    /** Public note fields returned to the frontend, without persistence relationships. */
    public record NoteResponse(
        String id,
        String title,
        String content,
        String sourceType,
        LocalDateTime createdAt
    ) {
    }
}
