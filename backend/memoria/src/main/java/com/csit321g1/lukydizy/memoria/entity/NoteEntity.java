package com.csit321g1.lukydizy.memoria.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Note table from the ERD (NoteID, UserID, Title, Content, SourceType, CreatedAt).
 * Minimal version so BE-03 compiles; BE-02 extends this class instead of creating a second one.
 */
@Entity
@Table(name = "notes")
public class NoteEntity {

    // Primary key (NoteID), generated as a UUID.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "note_id", nullable = false, updatable = false)
    private String id;

    // FK to users (UserID): the note owner. Deleting the user deletes their notes.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    // Where the note came from, e.g. MANUAL.
    @Column(name = "source_type", length = 30)
    private String sourceType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Creates an empty note for persistence. */
    public NoteEntity() {
    }

    /** Creates a note owned by the given user. */
    public NoteEntity(UserEntity user, String title, String content, String sourceType, LocalDateTime createdAt) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.sourceType = sourceType;
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getSourceType() {
        return sourceType;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
