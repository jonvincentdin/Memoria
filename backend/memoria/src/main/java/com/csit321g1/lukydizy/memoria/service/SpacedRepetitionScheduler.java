package com.csit321g1.lukydizy.memoria.service;

import java.time.LocalDateTime;

/**
 * SM-2 spaced-repetition scheduler adapted for Memoria's 1-5 recall rating scale.
 * Ratings 1-2 reset repetitions to 0 and schedule a 1-day interval.
 * Ratings 3-5 increment repetitions and schedule 1 day, 6 days, then round(prevInterval * easeFactor).
 */
public final class SpacedRepetitionScheduler {

    public static final double DEFAULT_EASE_FACTOR = 2.5;
    public static final double MIN_EASE_FACTOR = 1.3;
    public static final int PASSING_RATING = 3;
    public static final int MASTERED_REPETITIONS = 3;

    public static final String STATUS_NEW = "NEW";
    public static final String STATUS_LEARNING = "LEARNING";
    public static final String STATUS_MASTERED = "MASTERED";

    private SpacedRepetitionScheduler() {
    }

    /** Computed scheduling output from a single flashcard review. */
    public record ScheduleResult(
        int repetitionCount,
        int intervalDays,
        double easeFactor,
        LocalDateTime nextReviewAt
    ) {
    }

    /** Computes the next spaced-repetition state for a 1-5 rating at the given UTC timestamp. */
    public static ScheduleResult nextSchedule(
        Integer currentRepetitions,
        Integer currentIntervalDays,
        Double currentEaseFactor,
        int rating,
        LocalDateTime reviewedAt
    ) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        int repetitions = currentRepetitions == null ? 0 : Math.max(0, currentRepetitions);
        int previousInterval = currentIntervalDays == null ? 0 : Math.max(0, currentIntervalDays);
        double ease = currentEaseFactor == null ? DEFAULT_EASE_FACTOR : currentEaseFactor;

        int deficit = 5 - rating;
        double rawEase = ease + (0.1 - deficit * (0.08 + deficit * 0.02));
        double nextEase = Math.max(MIN_EASE_FACTOR, Math.round(rawEase * 100.0) / 100.0);

        int nextRepetitions;
        int nextInterval;
        if (rating < PASSING_RATING) {
            nextRepetitions = 0;
            nextInterval = 1;
        } else {
            nextRepetitions = repetitions + 1;
            if (nextRepetitions == 1) {
                nextInterval = 1;
            } else if (nextRepetitions == 2) {
                nextInterval = 6;
            } else {
                int baseInterval = previousInterval > 0 ? previousInterval : 6;
                nextInterval = Math.max(1, (int) Math.round(baseInterval * nextEase));
            }
        }

        return new ScheduleResult(
            nextRepetitions,
            nextInterval,
            nextEase,
            reviewedAt.plusDays(nextInterval)
        );
    }

    /** Returns true when a card has never been scheduled or its due timestamp has arrived. */
    public static boolean isDue(LocalDateTime nextReviewAt, LocalDateTime now) {
        return nextReviewAt == null || !nextReviewAt.isAfter(now);
    }

    /** Maps current review state to NEW, LEARNING, or MASTERED. */
    public static String masteryStatus(
        Integer repetitionCount,
        Integer lastRating,
        LocalDateTime lastReviewedAt
    ) {
        if (lastReviewedAt == null) {
            return STATUS_NEW;
        }
        int reps = repetitionCount == null ? 0 : repetitionCount;
        int rating = lastRating == null ? 0 : lastRating;
        if (reps >= MASTERED_REPETITIONS && rating >= PASSING_RATING) {
            return STATUS_MASTERED;
        }
        return STATUS_LEARNING;
    }
}
