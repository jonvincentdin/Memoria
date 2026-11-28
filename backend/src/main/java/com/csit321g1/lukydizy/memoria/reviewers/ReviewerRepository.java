package com.csit321g1.lukydizy.memoria.reviewers;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewerRepository extends JpaRepository<ReviewerEntity, String> {

    /** Finds a reviewer only if it belongs to the given user. */
    Optional<ReviewerEntity> findByIdAndUser_Id(String id, String userId);

    /** Lists a user's reviewers, newest first. */
    List<ReviewerEntity> findByUser_IdOrderByCreatedAtDesc(String userId);

    /** Finds the given reviewers that belong to the user; missing or foreign IDs are left out. */
    List<ReviewerEntity> findByIdInAndUser_Id(Collection<String> ids, String userId);

}
