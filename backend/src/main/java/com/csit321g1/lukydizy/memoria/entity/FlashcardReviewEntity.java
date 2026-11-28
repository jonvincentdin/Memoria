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
 * Immutable history of flashcard study reviews and resulting spaced-repetition intervals.
 */
@Entity
@Table(name = "flashcard_reviews")
public class FlashcardReviewEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "review_id", nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flashcard_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private FlashcardEntity flashcard;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    @Column(name = "rating", nullable = false)
    private int rating;

    @Column(name = "interval_days", nullable = false)
    private int intervalDays;

    @Column(name = "ease_factor", nullable = false)
    private double easeFactor;

    @Column(name = "reviewed_at", nullable = false, updatable = false)
    private LocalDateTime reviewedAt;

    @Column(name = "next_review_at", nullable = false, updatable = false)
    private LocalDateTime nextReviewAt;

    /** Creates an empty review record for persistence. */
    public FlashcardReviewEntity() {
    }

    /** Creates a persisted flashcard review entry. */
    public FlashcardReviewEntity(
        FlashcardEntity flashcard,
        UserEntity user,
        int rating,
        int intervalDays,
        double easeFactor,
        LocalDateTime reviewedAt,
        LocalDateTime nextReviewAt
    ) {
        this.flashcard = flashcard;
        this.user = user;
        this.rating = rating;
        this.intervalDays = intervalDays;
        this.easeFactor = easeFactor;
        this.reviewedAt = reviewedAt;
        this.nextReviewAt = nextReviewAt;
    }

    public String getId() {
        return id;
    }

    public FlashcardEntity getFlashcard() {
        return flashcard;
    }

    public UserEntity getUser() {
        return user;
    }

    public int getRating() {
        return rating;
    }

    public int getIntervalDays() {
        return intervalDays;
    }

    public double getEaseFactor() {
        return easeFactor;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public LocalDateTime getNextReviewAt() {
        return nextReviewAt;
    }
}
