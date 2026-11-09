package com.csit321g1.lukydizy.memoria.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.csit321g1.lukydizy.memoria.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, String> {

}
