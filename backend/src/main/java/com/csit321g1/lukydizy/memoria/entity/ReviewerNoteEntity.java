package com.csit321g1.lukydizy.memoria.entity;

import java.io.Serializable;
import java.util.Objects;

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
@IdClass(ReviewerNoteEntity.CompositeId.class)
public class ReviewerNoteEntity {

    /** Composite primary key for the reviewer-note link. */
    public static class CompositeId implements Serializable {

        private String reviewer;
        private String note;

        public CompositeId() {
        }

        public CompositeId(String reviewer, String note) {
            this.reviewer = reviewer;
            this.note = note;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof CompositeId that)) {
                return false;
            }
            return Objects.equals(reviewer, that.reviewer) && Objects.equals(note, that.note);
        }

        @Override
        public int hashCode() {
            return Objects.hash(reviewer, note);
        }
    }

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
