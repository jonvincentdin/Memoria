package com.csit321g1.lukydizy.memoria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.csit321g1.lukydizy.memoria.entity.QuizEntity;

public interface QuizRepository extends JpaRepository<QuizEntity, String> {

    /** Finds a quiz only if it belongs to the given user. */
    Optional<QuizEntity> findByIdAndUser_Id(String id, String userId);
}
