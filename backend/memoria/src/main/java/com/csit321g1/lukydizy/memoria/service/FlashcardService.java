package com.csit321g1.lukydizy.memoria.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.csit321g1.lukydizy.memoria.dto.FlashcardRequest;
import com.csit321g1.lukydizy.memoria.dto.FlashcardResponse;
import com.csit321g1.lukydizy.memoria.dto.FlashcardReviewRequest;
import com.csit321g1.lukydizy.memoria.dto.FlashcardReviewResponse;
import com.csit321g1.lukydizy.memoria.entity.FlashcardEntity;
import com.csit321g1.lukydizy.memoria.entity.ReviewerEntity;
import com.csit321g1.lukydizy.memoria.exception.InvalidRequestException;
import com.csit321g1.lukydizy.memoria.exception.ResourceNotFoundException;
import com.csit321g1.lukydizy.memoria.repository.FlashcardRepository;
import com.csit321g1.lukydizy.memoria.repository.ReviewerRepository;
import com.csit321g1.lukydizy.memoria.repository.UserRepository;

@Service
public class FlashcardService {

    private final FlashcardRepository flashcards;
    private final ReviewerRepository reviewers;
    private final UserRepository users;

    /** Creates the service with its repositories. */
    public FlashcardService(
        FlashcardRepository flashcards,
        ReviewerRepository reviewers,
        UserRepository users
    ) {
        this.flashcards = flashcards;
        this.reviewers = reviewers;
        this.users = users;
    }

    /** Creates a flashcard for the logged-in user and one of their reviewers. */
    @Transactional
    public FlashcardResponse create(FlashcardRequest request) {
        String userId = CurrentUser.id();
        ReviewerEntity reviewer = findOwnedReviewer(request.reviewerId(), userId);
        FlashcardEntity flashcard = flashcards.save(new FlashcardEntity(
            users.getReferenceById(userId),
            reviewer,
            request.front().trim(),
            request.back().trim(),
            LocalDateTime.now(ZoneOffset.UTC)
        ));
        return toResponse(flashcard);
    }

    /** Lists the logged-in user's cards, optionally filtered by one of their reviewers. */
    @Transactional(readOnly = true)
    public List<FlashcardResponse> list(String reviewerId) {
        String userId = CurrentUser.id();
        List<FlashcardEntity> found;
        if (reviewerId == null || reviewerId.isBlank()) {
            found = flashcards.findByUser_IdOrderByCreatedAtDesc(userId);
        } else {
            findOwnedReviewer(reviewerId, userId);
            found = flashcards.findByReviewer_IdAndUser_IdOrderByCreatedAtDesc(reviewerId, userId);
        }
        return found.stream().map(this::toResponse).toList();
    }

    /** Returns one of the logged-in user's flashcards. */
    @Transactional(readOnly = true)
    public FlashcardResponse get(String id) {
        return toResponse(findOwned(id, CurrentUser.id()));
    }

    /** Replaces the text and reviewer of an owned flashcard. */
    @Transactional
    public FlashcardResponse update(String id, FlashcardRequest request) {
        String userId = CurrentUser.id();
        FlashcardEntity flashcard = findOwned(id, userId);
        flashcard.setReviewer(findOwnedReviewer(request.reviewerId(), userId));
        flashcard.setFront(request.front().trim());
        flashcard.setBack(request.back().trim());
        return toResponse(flashcard);
    }

    /** Deletes one of the logged-in user's flashcards. */
    @Transactional
    public void delete(String id) {
        flashcards.delete(findOwned(id, CurrentUser.id()));
    }

    /**
     * Validates a study rating for an owned card and returns the outcome.
     * The ERD has no review-history fields, so this operation does not persist a rating or schedule.
     */
    @Transactional(readOnly = true)
    public FlashcardReviewResponse review(String id, FlashcardReviewRequest request) {
        FlashcardEntity flashcard = findOwned(id, CurrentUser.id());
        return new FlashcardReviewResponse(flashcard.getId(), request.rating());
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

    /** Converts the entity into the API response shape. */
    private FlashcardResponse toResponse(FlashcardEntity flashcard) {
        return new FlashcardResponse(
            flashcard.getId(),
            flashcard.getReviewer().getId(),
            flashcard.getFront(),
            flashcard.getBack(),
            flashcard.getCreatedAt()
        );
    }
}
