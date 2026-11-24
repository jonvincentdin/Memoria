package com.csit321g1.lukydizy.memoria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.csit321g1.lukydizy.memoria.entity.FlashcardReviewEntity;

public interface FlashcardReviewRepository extends JpaRepository<FlashcardReviewEntity, String> {

    /** Lists all flashcard reviews for a user, newest first, with flashcard and reviewer fetched. */
    @Query("""
        SELECT r FROM FlashcardReviewEntity r
        JOIN FETCH r.flashcard f
        JOIN FETCH f.reviewer
        WHERE r.user.id = :userId
        ORDER BY r.reviewedAt DESC, r.id DESC
        """)
    List<FlashcardReviewEntity> findByUserIdWithDetails(@Param("userId") String userId);

    /** Lists flashcard reviews for a user within a single reviewer, newest first. */
    @Query("""
        SELECT r FROM FlashcardReviewEntity r
        JOIN FETCH r.flashcard f
        JOIN FETCH f.reviewer rev
        WHERE r.user.id = :userId AND rev.id = :reviewerId
        ORDER BY r.reviewedAt DESC, r.id DESC
        """)
    List<FlashcardReviewEntity> findByUserIdAndReviewerIdWithDetails(
        @Param("userId") String userId,
        @Param("reviewerId") String reviewerId
    );

    /** Lists flashcard reviews for a single owned flashcard, newest first. */
    @Query("""
        SELECT r FROM FlashcardReviewEntity r
        JOIN FETCH r.flashcard f
        JOIN FETCH f.reviewer
        WHERE r.user.id = :userId AND f.id = :flashcardId
        ORDER BY r.reviewedAt DESC, r.id DESC
        """)
    List<FlashcardReviewEntity> findByUserIdAndFlashcardIdWithDetails(
        @Param("userId") String userId,
        @Param("flashcardId") String flashcardId
    );

    /** Removes review history rows for a flashcard prior to deletion. */
    void deleteByFlashcard_Id(String flashcardId);
}
