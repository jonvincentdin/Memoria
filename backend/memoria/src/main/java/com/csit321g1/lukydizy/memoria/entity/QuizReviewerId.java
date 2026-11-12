package com.csit321g1.lukydizy.memoria.entity;

import java.io.Serializable;
import java.util.Objects;

/** Composite primary key of QuizReviewer: (QuizID, ReviewerID). */
public class QuizReviewerId implements Serializable {

    private String quiz;
    private String reviewer;

    public QuizReviewerId() {
    }

    public QuizReviewerId(String quiz, String reviewer) {
        this.quiz = quiz;
        this.reviewer = reviewer;
    }

    // JPA compares composite keys with equals/hashCode, so both fields are included.
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof QuizReviewerId that)) {
            return false;
        }
        return Objects.equals(quiz, that.quiz) && Objects.equals(reviewer, that.reviewer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(quiz, reviewer);
    }
}
