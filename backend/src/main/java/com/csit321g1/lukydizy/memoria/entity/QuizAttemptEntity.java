package com.csit321g1.lukydizy.memoria.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * QuizAttempt table from the ERD (AttemptID, UserID, QuizID, Score, TotalQuestions, Status, CompletedAt).
 * Stores only the result summary; there is no AttemptAnswer table in Increment 1.
 */
@Entity
@Table(name = "quiz_attempts")
public class QuizAttemptEntity {

    /** Status of a quiz attempt. A graded attempt is COMPLETED. */
    public enum Status {
        COMPLETED
    }

    // Primary key (AttemptID), generated as a UUID.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "attempt_id", nullable = false, updatable = false)
    private String id;

    // FK to users (UserID): the student who took the quiz. Deleting the user deletes their attempts.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    // FK to quizzes (QuizID). Deleting the quiz deletes its attempts.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private QuizEntity quiz;

    // Number of correct answers.
    @Column(nullable = false)
    private int score;

    // Number of questions in the quiz when it was taken.
    @Column(name = "total_questions", nullable = false)
    private int totalQuestions;

    // Saved as text, e.g. "COMPLETED".
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    // When the attempt was graded (UTC); used for newest-first sorting.
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    /** Creates an empty attempt for persistence. */
    public QuizAttemptEntity() {
    }

    /** Creates an attempt result for the given user and quiz. */
    public QuizAttemptEntity(
        UserEntity user,
        QuizEntity quiz,
        int score,
        int totalQuestions,
        Status status,
        LocalDateTime completedAt
    ) {
        this.user = user;
        this.quiz = quiz;
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.status = status;
        this.completedAt = completedAt;
    }

    public String getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public QuizEntity getQuiz() {
        return quiz;
    }

    public int getScore() {
        return score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
