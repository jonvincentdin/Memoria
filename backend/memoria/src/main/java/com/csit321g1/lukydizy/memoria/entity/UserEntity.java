package com.csit321g1.lukydizy.memoria.entity;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "uk_users_email", columnNames = "email")
})
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", nullable = false, updatable = false)
    private String id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /** Creates an empty user for persistence. */
    public UserEntity() {
    }

    /** Creates a user with the supplied profile and creation time. */
    public UserEntity(String name, String email, String passwordHash, LocalDateTime createdAt) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt;
    }

    /** Sets the user ID. */
    public void setId(String id) {
        this.id = id;
    }

    /** Sets the creation time. */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /** Sets the user's name. */
    public void setName(String name) {
        this.name = name;
    }

    /** Sets the user's email address. */
    public void setEmail(String email) {
        this.email = email;
    }

    /** Sets the stored password hash. */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /** Returns the user ID. */
    public String getId() {
        return id;
    }

    /** Returns the creation time. */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /** Returns the user's name. */
    public String getName() {
        return name;
    }

    /** Returns the user's email address. */
    public String getEmail() {
        return email;
    }

    /** Defines the write-only password hash property. */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String getPasswordHash() {
        return passwordHash;
    }
}
