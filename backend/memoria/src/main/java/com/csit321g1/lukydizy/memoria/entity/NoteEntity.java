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
 * A student's note with the ERD fields and a link to its owning user.
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

    @Column(nullable = false, length = 150)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // Notes entered in the app use MANUAL; imported files use IMPORT.
    @Column(name = "source_type", nullable = false, length = 30)
    private String sourceType;

    // Original filename for imported notes; null for manual notes.
    @Column(name = "source_filename", length = 255)
    private String sourceFilename;

    @Column(name = "archived", nullable = false, columnDefinition = "boolean not null default false")
    private boolean archived = false;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Creates an empty note for persistence. */
    public NoteEntity() {
    }

    /** Creates a note owned by the given user. */
    public NoteEntity(UserEntity user, String title, String content, String sourceType, LocalDateTime createdAt) {
        this(user, title, content, sourceType, null, createdAt);
    }

    /** Creates a note owned by the given user with an optional source filename. */
    public NoteEntity(
        UserEntity user,
        String title,
        String content,
        String sourceType,
        String sourceFilename,
        LocalDateTime createdAt
    ) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.sourceType = sourceType;
        this.sourceFilename = sourceFilename;
        this.archived = false;
        this.archivedAt = null;
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

    /** Replaces the note title. */
    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    /** Replaces the note content. */
    public void setContent(String content) {
        this.content = content;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getSourceFilename() {
        return sourceFilename;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(LocalDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
