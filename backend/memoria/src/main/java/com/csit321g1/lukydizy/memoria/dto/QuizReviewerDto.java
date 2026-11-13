package com.csit321g1.lukydizy.memoria.dto;

public final class QuizReviewerDto {

    private QuizReviewerDto() {
    }

    /** A reviewer linked to a quiz. */
    public record LinkedReviewer(String id, String title) {
    }
}
