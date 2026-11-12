package com.csit321g1.lukydizy.memoria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.csit321g1.lukydizy.memoria.entity.UserEntity;

// JpaRepository gives save, findAll, findById and delete; Spring generates the SQL from the method names.
public interface UserRepository extends JpaRepository<UserEntity, String> {

    // SELECT ... WHERE email = ?; used by login.
    Optional<UserEntity> findByEmail(String email);

    // Checks for a duplicate email before registering.
    boolean existsByEmail(String email);
}
