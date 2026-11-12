package com.csit321g1.lukydizy.memoria.entity;

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
@IdClass(QuizReviewerId.class)
public class QuizReviewerEntity {

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
