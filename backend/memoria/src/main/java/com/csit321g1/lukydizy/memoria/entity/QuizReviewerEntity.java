package com.csit321g1.lukydizy.memoria.entity;

import java.io.Serializable;
import java.util.Objects;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * QuizReviewer join table from the ERD: links one quiz to one reviewer.
 * Minimal version so BE-03 can block deleting a reviewer that a quiz uses; BE-04 reuses this class.
 */
@Entity
@Table(name = "quiz_reviewers")
@IdClass(QuizReviewerEntity.CompositeId.class)
public class QuizReviewerEntity {

    /** Composite primary key for the quiz-reviewer link. */
    public static class CompositeId implements Serializable {

        private String quiz;
        private String reviewer;

        public CompositeId() {
        }

        public CompositeId(String quiz, String reviewer) {
            this.quiz = quiz;
            this.reviewer = reviewer;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CompositeId that)) {
                return false;
            }
            return Objects.equals(quiz, that.quiz) && Objects.equals(reviewer, that.reviewer);
        }

        @Override
        public int hashCode() {
            return Objects.hash(quiz, reviewer);
        }
    }

    // Part 1 of the composite key: FK to quizzes (QuizID).
    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private QuizEntity quiz;

    // Part 2 of the composite key: FK to reviewers (ReviewerID).
    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ReviewerEntity reviewer;

    /** Creates an empty link for persistence. */
    public QuizReviewerEntity() {
    }

    /** Links a quiz to a reviewer. */
    public QuizReviewerEntity(QuizEntity quiz, ReviewerEntity reviewer) {
        this.quiz = quiz;
        this.reviewer = reviewer;
    }

    public QuizEntity getQuiz() {
        return quiz;
    }

    public ReviewerEntity getReviewer() {
        return reviewer;
    }
}
