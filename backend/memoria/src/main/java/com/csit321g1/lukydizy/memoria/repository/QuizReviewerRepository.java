package com.csit321g1.lukydizy.memoria.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.csit321g1.lukydizy.memoria.entity.QuizReviewerEntity;
import com.csit321g1.lukydizy.memoria.entity.QuizReviewerId;

public interface QuizReviewerRepository extends JpaRepository<QuizReviewerEntity, QuizReviewerId> {

    /** Counts how many quizzes use a reviewer. */
    long countByReviewer_Id(String reviewerId);
}
