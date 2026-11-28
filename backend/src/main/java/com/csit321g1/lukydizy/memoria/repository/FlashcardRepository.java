package com.csit321g1.lukydizy.memoria.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.csit321g1.lukydizy.memoria.entity.FlashcardEntity;

public interface FlashcardRepository extends JpaRepository<FlashcardEntity, String> {

    /** Finds a flashcard only if it belongs to the given user. */
    Optional<FlashcardEntity> findByIdAndUser_Id(String id, String userId);

    /** Lists a user's flashcards, newest first. */
    List<FlashcardEntity> findByUser_IdOrderByCreatedAtDesc(String userId);

    /** Lists a user's flashcards for one reviewer, newest first. */
    List<FlashcardEntity> findByReviewer_IdAndUser_IdOrderByCreatedAtDesc(String reviewerId, String userId);

    /** Lists a user's due flashcards (overdue first, then never-reviewed oldest first). */
    @Query("""
        SELECT f FROM FlashcardEntity f
        WHERE f.user.id = :userId
          AND (f.nextReviewAt IS NULL OR f.nextReviewAt <= :now)
        ORDER BY CASE WHEN f.nextReviewAt IS NULL THEN 1 ELSE 0 END,
                 f.nextReviewAt ASC,
                 f.createdAt ASC
        """)
    List<FlashcardEntity> findDueByUserId(@Param("userId") String userId, @Param("now") LocalDateTime now);

    /** Lists a user's due flashcards for one reviewer (overdue first, then never-reviewed oldest first). */
    @Query("""
        SELECT f FROM FlashcardEntity f
        WHERE f.reviewer.id = :reviewerId
          AND f.user.id = :userId
          AND (f.nextReviewAt IS NULL OR f.nextReviewAt <= :now)
        ORDER BY CASE WHEN f.nextReviewAt IS NULL THEN 1 ELSE 0 END,
                 f.nextReviewAt ASC,
                 f.createdAt ASC
        """)
    List<FlashcardEntity> findDueByReviewerIdAndUserId(
        @Param("reviewerId") String reviewerId,
        @Param("userId") String userId,
        @Param("now") LocalDateTime now
    );
}
