package com.csit321g1.lukydizy.memoria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.csit321g1.lukydizy.memoria.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, String> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
