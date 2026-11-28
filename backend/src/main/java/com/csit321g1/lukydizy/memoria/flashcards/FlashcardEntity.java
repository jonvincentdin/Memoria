package com.csit321g1.lukydizy.memoria.flashcards;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.csit321g1.lukydizy.memoria.reviewers.ReviewerEntity;
import com.csit321g1.lukydizy.memoria.users.UserEntity;

/**
 * Flashcard table from the ERD (FlashcardID, UserID, ReviewerID, Front, Back, CreatedAt).
 * A flashcard belongs to its student and to one of that student's reviewers.
 */
@Entity
@Table(name = "flashcards")
public class FlashcardEntity {

    // Primary key (FlashcardID), generated as a UUID.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "flashcard_id", nullable = false, updatable = false)
    private String id;

    // FK to users (UserID): deleting the student deletes their flashcards.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    // FK to reviewers (ReviewerID): deleting the reviewer deletes its flashcards.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ReviewerEntity reviewer;

    @Column(name = "front", nullable = false, columnDefinition = "TEXT")
    private String front;

    @Column(name = "back", nullable = false, columnDefinition = "TEXT")
    private String back;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Spaced-repetition scheduling state (nullable for backward compatibility with existing rows).
    @Column(name = "repetition_count")
    private Integer repetitionCount;

    @Column(name = "interval_days")
    private Integer intervalDays;

    @Column(name = "ease_factor")
    private Double easeFactor;

    @Column(name = "last_rating")
    private Integer lastRating;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt;

    /** Creates an empty flashcard for persistence. */
    public FlashcardEntity() {
    }

    /** Creates a flashcard owned by the given user and linked to their reviewer. */
    public FlashcardEntity(
        UserEntity user,
        ReviewerEntity reviewer,
        String front,
        String back,
        LocalDateTime createdAt
    ) {
        this.user = user;
        this.reviewer = reviewer;
        this.front = front;
        this.back = back;
        this.createdAt = createdAt;
        this.repetitionCount = 0;
        this.intervalDays = 0;
        this.easeFactor = 2.5;
    }

    public String getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public ReviewerEntity getReviewer() {
        return reviewer;
    }

    /** Replaces the linked reviewer. */
    public void setReviewer(ReviewerEntity reviewer) {
        this.reviewer = reviewer;
    }

    public String getFront() {
        return front;
    }

    /** Replaces the front of the card. */
    public void setFront(String front) {
        this.front = front;
    }

    public String getBack() {
        return back;
    }

    /** Replaces the back of the card. */
    public void setBack(String back) {
        this.back = back;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Integer getRepetitionCount() {
        return repetitionCount;
    }

    public Integer getIntervalDays() {
        return intervalDays;
    }

    public Double getEaseFactor() {
        return easeFactor;
    }

    public Integer getLastRating() {
        return lastRating;
    }

    public LocalDateTime getLastReviewedAt() {
        return lastReviewedAt;
    }

    public LocalDateTime getNextReviewAt() {
        return nextReviewAt;
    }

    /** Updates spaced-repetition scheduling fields after a student rates this card. */
    public void applyReviewSchedule(
        int repetitionCount,
        int intervalDays,
        double easeFactor,
        int lastRating,
        LocalDateTime lastReviewedAt,
        LocalDateTime nextReviewAt
    ) {
        this.repetitionCount = repetitionCount;
        this.intervalDays = intervalDays;
        this.easeFactor = easeFactor;
        this.lastRating = lastRating;
        this.lastReviewedAt = lastReviewedAt;
        this.nextReviewAt = nextReviewAt;
    }
}
