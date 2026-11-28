package com.csit321g1.lukydizy.memoria.dto;

public final class ReviewerNoteDto {

    private ReviewerNoteDto() {
    }

    /** A note linked to a reviewer through ReviewerNote. */
    public record LinkedNote(String id, String title) {
    }
}
