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
 * Reviewer table from the ERD (ReviewerID, UserID, Title, Style, Content, CreatedAt).
 * Linked notes are stored in the ReviewerNote join table, not in a column here.
 */
@Entity
@Table(name = "reviewers")
public class ReviewerEntity {

    // Primary key (ReviewerID), generated as a UUID.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "reviewer_id", nullable = false, updatable = false)
    private String id;

    // FK to users (UserID): the reviewer owner. Notes are linked through ReviewerNote, not here.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserEntity user;

    @Column(nullable = false, length = 200)
    private String title;

    // Short label such as Summary, Outline, or Q&A.
    @Column(nullable = false, length = 50)
    private String style;

    // Reviewer text typed or pasted by the student (no AI).
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Creates an empty reviewer for persistence. */
    public ReviewerEntity() {
    }

    /** Creates a reviewer owned by the given user. */
    public ReviewerEntity(UserEntity user, String title, String style, String content, LocalDateTime createdAt) {
        this.user = user;
        this.title = title;
        this.style = style;
        this.content = content;
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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
