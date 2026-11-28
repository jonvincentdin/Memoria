package com.csit321g1.lukydizy.memoria.flashcards;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.csit321g1.lukydizy.memoria.flashcards.FlashcardDto.ProgressSummary;
import com.csit321g1.lukydizy.memoria.flashcards.FlashcardDto.Request;
import com.csit321g1.lukydizy.memoria.flashcards.FlashcardDto.Response;
import com.csit321g1.lukydizy.memoria.flashcards.FlashcardDto.ReviewHistoryItem;
import com.csit321g1.lukydizy.memoria.flashcards.FlashcardDto.ReviewRequest;
import com.csit321g1.lukydizy.memoria.flashcards.FlashcardDto.ReviewResponse;
import com.csit321g1.lukydizy.memoria.flashcards.SpacedRepetitionScheduler.ScheduleResult;
import com.csit321g1.lukydizy.memoria.reviewers.ReviewerEntity;
import com.csit321g1.lukydizy.memoria.reviewers.ReviewerRepository;
import com.csit321g1.lukydizy.memoria.shared.error.InvalidRequestException;
import com.csit321g1.lukydizy.memoria.shared.error.ResourceNotFoundException;
import com.csit321g1.lukydizy.memoria.shared.security.CurrentUser;
import com.csit321g1.lukydizy.memoria.users.UserRepository;

@Service
public class FlashcardService {

    private final FlashcardRepository flashcards;
    private final FlashcardReviewRepository reviews;
    private final ReviewerRepository reviewers;
    private final UserRepository users;

    /** Creates the service with its repositories. */
    public FlashcardService(
        FlashcardRepository flashcards,
        FlashcardReviewRepository reviews,
        ReviewerRepository reviewers,
        UserRepository users
    ) {
        this.flashcards = flashcards;
        this.reviews = reviews;
        this.reviewers = reviewers;
        this.users = users;
    }

    /** Creates a flashcard for the logged-in user and one of their reviewers. */
    @Transactional
    public Response create(Request request) {
        String userId = CurrentUser.id();
        ReviewerEntity reviewer = findOwnedReviewer(request.reviewerId(), userId);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        FlashcardEntity flashcard = flashcards.save(new FlashcardEntity(
            users.getReferenceById(userId),
            reviewer,
            request.front().trim(),
            request.back().trim(),
            now
        ));
        return toResponse(flashcard, now);
    }

    /**
     * Lists the logged-in user's cards, optionally filtered by one of their reviewers
     * and/or restricted to currently due cards.
     */
    @Transactional(readOnly = true)
    public List<Response> list(String reviewerId, boolean dueOnly) {
        String userId = CurrentUser.id();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        boolean hasReviewer = reviewerId != null && !reviewerId.isBlank();
        if (hasReviewer) {
            findOwnedReviewer(reviewerId, userId);
        }
        List<FlashcardEntity> found;
        if (dueOnly) {
            found = hasReviewer
                ? flashcards.findDueByReviewerIdAndUserId(reviewerId, userId, now)
                : flashcards.findDueByUserId(userId, now);
        } else {
            found = hasReviewer
                ? flashcards.findByReviewer_IdAndUser_IdOrderByCreatedAtDesc(reviewerId, userId)
                : flashcards.findByUser_IdOrderByCreatedAtDesc(userId);
        }
        return found.stream().map(card -> toResponse(card, now)).toList();
    }

    /** Returns one of the logged-in user's flashcards. */
    @Transactional(readOnly = true)
    public Response get(String id) {
        return toResponse(findOwned(id, CurrentUser.id()), LocalDateTime.now(ZoneOffset.UTC));
    }

    /** Replaces the text and reviewer of an owned flashcard while preserving scheduling state. */
    @Transactional
    public Response update(String id, Request request) {
        String userId = CurrentUser.id();
        FlashcardEntity flashcard = findOwned(id, userId);
        flashcard.setReviewer(findOwnedReviewer(request.reviewerId(), userId));
        flashcard.setFront(request.front().trim());
        flashcard.setBack(request.back().trim());
        return toResponse(flashcard, LocalDateTime.now(ZoneOffset.UTC));
    }

    /** Deletes one of the logged-in user's flashcards and its review history. */
    @Transactional
    public void delete(String id) {
        FlashcardEntity flashcard = findOwned(id, CurrentUser.id());
        reviews.deleteByFlashcard_Id(flashcard.getId());
        flashcards.delete(flashcard);
    }

    /**
     * Records a 1-5 study rating for an owned flashcard, updates its SM-2 schedule,
     * and persists a review-history row.
     */
    @Transactional
    public ReviewResponse review(String id, ReviewRequest request) {
        String userId = CurrentUser.id();
        FlashcardEntity flashcard = findOwned(id, userId);
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        ScheduleResult schedule = SpacedRepetitionScheduler.nextSchedule(
            flashcard.getRepetitionCount(),
            flashcard.getIntervalDays(),
            flashcard.getEaseFactor(),
            request.rating(),
            now
        );

        flashcard.applyReviewSchedule(
            schedule.repetitionCount(),
            schedule.intervalDays(),
            schedule.easeFactor(),
            request.rating(),
            now,
            schedule.nextReviewAt()
        );

        FlashcardReviewEntity savedReview = reviews.save(new FlashcardReviewEntity(
            flashcard,
            users.getReferenceById(userId),
            request.rating(),
            schedule.intervalDays(),
            schedule.easeFactor(),
            now,
            schedule.nextReviewAt()
        ));

        String status = SpacedRepetitionScheduler.masteryStatus(
            schedule.repetitionCount(),
            request.rating(),
            now
        );

        return new ReviewResponse(
            savedReview.getId(),
            flashcard.getId(),
            request.rating(),
            schedule.repetitionCount(),
            schedule.intervalDays(),
            schedule.easeFactor(),
            now,
            schedule.nextReviewAt(),
            status
        );
    }

    /** Lists the logged-in user's flashcard review history, newest first. */
    @Transactional(readOnly = true)
    public List<ReviewHistoryItem> listReviews(String reviewerId, String flashcardId) {
        String userId = CurrentUser.id();
        boolean hasReviewer = reviewerId != null && !reviewerId.isBlank();
        boolean hasFlashcard = flashcardId != null && !flashcardId.isBlank();

        if (hasReviewer) {
            findOwnedReviewer(reviewerId, userId);
        }
        if (hasFlashcard) {
            FlashcardEntity card = findOwned(flashcardId, userId);
            if (hasReviewer && !card.getReviewer().getId().equals(reviewerId)) {
                return List.of();
            }
            return reviews.findByUserIdAndFlashcardIdWithDetails(userId, flashcardId)
                .stream()
                .map(this::toReviewHistoryItem)
                .toList();
        }
        if (hasReviewer) {
            return reviews.findByUserIdAndReviewerIdWithDetails(userId, reviewerId)
                .stream()
                .map(this::toReviewHistoryItem)
                .toList();
        }
        return reviews.findByUserIdWithDetails(userId)
            .stream()
            .map(this::toReviewHistoryItem)
            .toList();
    }

    /** Computes flashcard progress summary across all reviewers or a single owned reviewer. */
    @Transactional(readOnly = true)
    public ProgressSummary progress(String reviewerId) {
        String userId = CurrentUser.id();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime startOfToday = now.toLocalDate().atStartOfDay();
        boolean hasReviewer = reviewerId != null && !reviewerId.isBlank();

        List<FlashcardEntity> cards;
        List<FlashcardReviewEntity> reviewList;
        if (hasReviewer) {
            findOwnedReviewer(reviewerId, userId);
            cards = flashcards.findByReviewer_IdAndUser_IdOrderByCreatedAtDesc(reviewerId, userId);
            reviewList = reviews.findByUserIdAndReviewerIdWithDetails(userId, reviewerId);
        } else {
            cards = flashcards.findByUser_IdOrderByCreatedAtDesc(userId);
            reviewList = reviews.findByUserIdWithDetails(userId);
        }

        long dueCards = 0;
        long newCards = 0;
        long learningCards = 0;
        long masteredCards = 0;
        LocalDateTime nextDueAt = null;

        for (FlashcardEntity card : cards) {
            if (SpacedRepetitionScheduler.isDue(card.getNextReviewAt(), now)) {
                dueCards++;
            } else if (card.getNextReviewAt() != null
                && (nextDueAt == null || card.getNextReviewAt().isBefore(nextDueAt))) {
                nextDueAt = card.getNextReviewAt();
            }

            String status = SpacedRepetitionScheduler.masteryStatus(
                card.getRepetitionCount(),
                card.getLastRating(),
                card.getLastReviewedAt()
            );
            switch (status) {
                case SpacedRepetitionScheduler.STATUS_NEW -> newCards++;
                case SpacedRepetitionScheduler.STATUS_MASTERED -> masteredCards++;
                default -> learningCards++;
            }
        }

        long totalReviews = reviewList.size();
        long reviewsToday = 0;
        long ratingSum = 0;
        for (FlashcardReviewEntity entry : reviewList) {
            ratingSum += entry.getRating();
            if (!entry.getReviewedAt().isBefore(startOfToday)) {
                reviewsToday++;
            }
        }

        Double averageRating = totalReviews == 0
            ? null
            : Math.round((ratingSum * 100.0) / totalReviews) / 100.0;
        LocalDateTime lastReviewedAt = reviewList.isEmpty() ? null : reviewList.get(0).getReviewedAt();

        return new ProgressSummary(
            cards.size(),
            dueCards,
            newCards,
            learningCards,
            masteredCards,
            totalReviews,
            reviewsToday,
            averageRating,
            lastReviewedAt,
            nextDueAt
        );
    }

    /** Finds a reviewer owned by the user; missing and foreign reviewers are treated alike. */
    private ReviewerEntity findOwnedReviewer(String reviewerId, String userId) {
        return reviewers.findByIdAndUser_Id(reviewerId, userId)
            .orElseThrow(() -> new InvalidRequestException("Reviewer not found or does not belong to this user."));
    }

    /** Finds a flashcard owned by the user; another user's card is treated as missing. */
    private FlashcardEntity findOwned(String id, String userId) {
        return flashcards.findByIdAndUser_Id(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found."));
    }

    /** Converts the entity into the API response shape with normalized scheduling defaults. */
    private Response toResponse(FlashcardEntity flashcard, LocalDateTime now) {
        int repetitions = flashcard.getRepetitionCount() == null ? 0 : flashcard.getRepetitionCount();
        int intervalDays = flashcard.getIntervalDays() == null ? 0 : flashcard.getIntervalDays();
        double easeFactor = flashcard.getEaseFactor() == null
            ? SpacedRepetitionScheduler.DEFAULT_EASE_FACTOR
            : flashcard.getEaseFactor();
        boolean due = SpacedRepetitionScheduler.isDue(flashcard.getNextReviewAt(), now);
        String status = SpacedRepetitionScheduler.masteryStatus(
            flashcard.getRepetitionCount(),
            flashcard.getLastRating(),
            flashcard.getLastReviewedAt()
        );
        return new Response(
            flashcard.getId(),
            flashcard.getReviewer().getId(),
            flashcard.getFront(),
            flashcard.getBack(),
            flashcard.getCreatedAt(),
            repetitions,
            intervalDays,
            easeFactor,
            flashcard.getLastRating(),
            flashcard.getLastReviewedAt(),
            flashcard.getNextReviewAt(),
            due,
            status
        );
    }

    private ReviewHistoryItem toReviewHistoryItem(FlashcardReviewEntity entry) {
        FlashcardEntity card = entry.getFlashcard();
        ReviewerEntity reviewer = card.getReviewer();
        return new ReviewHistoryItem(
            entry.getId(),
            card.getId(),
            reviewer.getId(),
            reviewer.getTitle(),
            card.getFront(),
            entry.getRating(),
            entry.getIntervalDays(),
            entry.getEaseFactor(),
            entry.getReviewedAt(),
            entry.getNextReviewAt()
        );
    }
}
