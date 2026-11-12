package com.csit321g1.lukydizy.memoria.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.csit321g1.lukydizy.memoria.entity.ReviewerEntity;

public interface ReviewerRepository extends JpaRepository<ReviewerEntity, String> {

    /** Finds a reviewer only if it belongs to the given user. */
    Optional<ReviewerEntity> findByIdAndUser_Id(String id, String userId);

    /** Lists a user's reviewers, newest first. */
    List<ReviewerEntity> findByUser_IdOrderByCreatedAtDesc(String userId);
}
