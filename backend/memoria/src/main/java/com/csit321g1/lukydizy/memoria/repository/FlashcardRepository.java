package com.csit321g1.lukydizy.memoria.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.csit321g1.lukydizy.memoria.entity.FlashcardEntity;

public interface FlashcardRepository extends JpaRepository<FlashcardEntity, String> {

    /** Finds a flashcard only if it belongs to the given user. */
    Optional<FlashcardEntity> findByIdAndUser_Id(String id, String userId);

    /** Lists a user's flashcards, newest first. */
    List<FlashcardEntity> findByUser_IdOrderByCreatedAtDesc(String userId);

    /** Lists a user's flashcards for one reviewer, newest first. */
    List<FlashcardEntity> findByReviewer_IdAndUser_IdOrderByCreatedAtDesc(String reviewerId, String userId);
}
