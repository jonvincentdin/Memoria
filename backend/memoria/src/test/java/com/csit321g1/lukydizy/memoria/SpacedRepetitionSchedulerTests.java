package com.csit321g1.lukydizy.memoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.csit321g1.lukydizy.memoria.service.SpacedRepetitionScheduler;
import com.csit321g1.lukydizy.memoria.service.SpacedRepetitionScheduler.ScheduleResult;

class SpacedRepetitionSchedulerTests {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 25, 10, 0);

    @Test
    void passingRatingsProgressThroughOneSixAndScaledIntervals() {
        ScheduleResult first = SpacedRepetitionScheduler.nextSchedule(null, null, null, 4, NOW);
        assertEquals(1, first.repetitionCount());
        assertEquals(1, first.intervalDays());
        assertEquals(2.5, first.easeFactor());
        assertEquals(NOW.plusDays(1), first.nextReviewAt());

        ScheduleResult second = SpacedRepetitionScheduler.nextSchedule(
            first.repetitionCount(), first.intervalDays(), first.easeFactor(), 4, NOW.plusDays(1));
        assertEquals(2, second.repetitionCount());
        assertEquals(6, second.intervalDays());
        assertEquals(2.5, second.easeFactor());
        assertEquals(NOW.plusDays(7), second.nextReviewAt());

        ScheduleResult third = SpacedRepetitionScheduler.nextSchedule(
            second.repetitionCount(), second.intervalDays(), second.easeFactor(), 5, NOW.plusDays(7));
        assertEquals(3, third.repetitionCount());
        assertEquals(2.6, third.easeFactor());
        assertEquals(16, third.intervalDays()); // round(6 * 2.6) = 16
        assertEquals(NOW.plusDays(23), third.nextReviewAt());
    }

    @Test
    void failedRatingsResetRepetitionsAndClampEaseFactor() {
        ScheduleResult failed = SpacedRepetitionScheduler.nextSchedule(3, 15, 2.5, 2, NOW);
        assertEquals(0, failed.repetitionCount());
        assertEquals(1, failed.intervalDays());
        assertEquals(2.18, failed.easeFactor());
        assertEquals(NOW.plusDays(1), failed.nextReviewAt());

        double ease = 1.5;
        ScheduleResult clamped = SpacedRepetitionScheduler.nextSchedule(2, 6, ease, 1, NOW);
        assertEquals(0, clamped.repetitionCount());
        assertEquals(1, clamped.intervalDays());
        assertEquals(SpacedRepetitionScheduler.MIN_EASE_FACTOR, clamped.easeFactor());
    }

    @Test
    void rejectsOutOfRangeRating() {
        assertThrows(IllegalArgumentException.class,
            () -> SpacedRepetitionScheduler.nextSchedule(0, 0, 2.5, 0, NOW));
        assertThrows(IllegalArgumentException.class,
            () -> SpacedRepetitionScheduler.nextSchedule(0, 0, 2.5, 6, NOW));
    }

    @Test
    void computesDueAndMasteryStatusAccurately() {
        assertTrue(SpacedRepetitionScheduler.isDue(null, NOW));
        assertTrue(SpacedRepetitionScheduler.isDue(NOW.minusMinutes(1), NOW));
        assertTrue(SpacedRepetitionScheduler.isDue(NOW, NOW));
        assertFalse(SpacedRepetitionScheduler.isDue(NOW.plusSeconds(1), NOW));

        assertEquals("NEW", SpacedRepetitionScheduler.masteryStatus(0, null, null));
        assertEquals("LEARNING", SpacedRepetitionScheduler.masteryStatus(2, 4, NOW));
        assertEquals("LEARNING", SpacedRepetitionScheduler.masteryStatus(3, 2, NOW));
        assertEquals("MASTERED", SpacedRepetitionScheduler.masteryStatus(3, 3, NOW));
    }
}
