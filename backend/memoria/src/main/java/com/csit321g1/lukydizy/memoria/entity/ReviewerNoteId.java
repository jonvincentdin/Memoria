package com.csit321g1.lukydizy.memoria.entity;

import java.io.Serializable;
import java.util.Objects;

/** Composite primary key of ReviewerNote: (ReviewerID, NoteID). */
public class ReviewerNoteId implements Serializable {

    private String reviewer;
    private String note;

    public ReviewerNoteId() {
    }

    public ReviewerNoteId(String reviewer, String note) {
        this.reviewer = reviewer;
        this.note = note;
    }

    // JPA compares composite keys with equals/hashCode, so both fields are included.
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ReviewerNoteId that)) {
            return false;
        }
        return Objects.equals(reviewer, that.reviewer) && Objects.equals(note, that.note);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reviewer, note);
    }
}
