package com.csit321g1.lukydizy.memoria.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * ReviewerNote join table from the ERD: links one reviewer to one note.
 * A reviewer can have many notes and a note can be used by many reviewers.
 */
@Entity
@Table(name = "reviewer_notes")
@IdClass(ReviewerNoteId.class)
public class ReviewerNoteEntity {

    // Part 1 of the composite key: FK to reviewers (ReviewerID). Deleting the reviewer deletes the link.
    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reviewer_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ReviewerEntity reviewer;

    // Part 2 of the composite key: FK to notes (NoteID). Deleting the note deletes the link.
    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private NoteEntity note;

    /** Creates an empty link for persistence. */
    public ReviewerNoteEntity() {
    }

    /** Links a reviewer to a note. */
    public ReviewerNoteEntity(ReviewerEntity reviewer, NoteEntity note) {
        this.reviewer = reviewer;
        this.note = note;
    }

    public ReviewerEntity getReviewer() {
        return reviewer;
    }

    public NoteEntity getNote() {
        return note;
    }
}
