package com.csit321g1.lukydizy.memoria.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Quiz table from the ERD (QuizID, UserID, Title, Mode, Questions, CreatedAt).
 * Minimal version so BE-03 and BE-05 compile; BE-04 extends this class instead of creating a second one.
 */
@Entity
@Table(name = "quizzes")
public class QuizEntity {

    // Primary key (QuizID), generated as a UUID.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "quiz_id", nullable = false, updatable = false)
    private String id;

    // FK to users (UserID): the quiz owner. Deleting the user deletes their quizzes.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 50)
    private String mode;

    // Questions stored as text; BE-04 decides the exact format.
    @Column(columnDefinition = "TEXT")
    private String questions;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Creates an empty quiz for persistence. */
    public QuizEntity() {
    }

    /** Creates a quiz owned by the given user. */
    public QuizEntity(UserEntity user, String title, String mode, String questions, LocalDateTime createdAt) {
        this.user = user;
        this.title = title;
        this.mode = mode;
        this.questions = questions;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public String getMode() {
        return mode;
    }

    public String getQuestions() {
        return questions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
