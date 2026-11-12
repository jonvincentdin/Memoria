package com.csit321g1.lukydizy.memoria.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.csit321g1.lukydizy.memoria.dto.NoteRequest;
import com.csit321g1.lukydizy.memoria.dto.NoteResponse;
import com.csit321g1.lukydizy.memoria.entity.NoteEntity;
import com.csit321g1.lukydizy.memoria.exception.ResourceInUseException;
import com.csit321g1.lukydizy.memoria.exception.ResourceNotFoundException;
import com.csit321g1.lukydizy.memoria.repository.NoteRepository;
import com.csit321g1.lukydizy.memoria.repository.ReviewerNoteRepository;
import com.csit321g1.lukydizy.memoria.repository.UserRepository;

@Service
public class NoteService {

    private final NoteRepository notes;
    private final ReviewerNoteRepository reviewerNotes;
    private final UserRepository users;

    /** Creates the service with its repositories. */
    public NoteService(NoteRepository notes, ReviewerNoteRepository reviewerNotes, UserRepository users) {
        this.notes = notes;
        this.reviewerNotes = reviewerNotes;
        this.users = users;
    }

    /** Creates a note for the logged-in user; clients cannot choose its owner or source type. */
    @Transactional
    public NoteResponse create(NoteRequest request) {
        String userId = CurrentUser.id();
        NoteEntity note = notes.save(new NoteEntity(
            users.getReferenceById(userId),
            request.title().trim(),
            request.content(),
            "MANUAL",
            LocalDateTime.now(ZoneOffset.UTC)
        ));
        return toResponse(note);
    }

    /** Lists only the logged-in user's notes, newest first. */
    @Transactional(readOnly = true)
    public List<NoteResponse> list() {
        String userId = CurrentUser.id();
        return notes.findByUser_IdOrderByCreatedAtDesc(userId).stream()
            .map(this::toResponse)
            .toList();
    }

    /** Returns an owned note; another user's note is indistinguishable from a missing note. */
    @Transactional(readOnly = true)
    public NoteResponse get(String id) {
        return toResponse(findOwned(id, CurrentUser.id()));
    }

    /** Replaces the title and content of an owned note. */
    @Transactional
    public NoteResponse update(String id, NoteRequest request) {
        NoteEntity note = findOwned(id, CurrentUser.id());
        note.setTitle(request.title().trim());
        note.setContent(request.content());
        return toResponse(note);
    }

    /** Deletes an owned note unless a reviewer still links to it. */
    @Transactional
    public void delete(String id) {
        NoteEntity note = findOwned(id, CurrentUser.id());
        long reviewerCount = reviewerNotes.countByNote_Id(note.getId());
        if (reviewerCount > 0) {
            throw new ResourceInUseException(
                "This note is linked to " + reviewerCount + (reviewerCount == 1 ? " reviewer" : " reviewers")
                    + ". Remove it from those reviewers before deleting it."
            );
        }
        notes.delete(note);
    }

    /** Finds a note owned by the user; missing and foreign notes both return 404. */
    private NoteEntity findOwned(String id, String userId) {
        return notes.findByIdAndUser_Id(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Note not found."));
    }

    /** Converts the entity into the API response shape. */
    private NoteResponse toResponse(NoteEntity note) {
        return new NoteResponse(
            note.getId(),
            note.getTitle(),
            note.getContent(),
            note.getSourceType(),
            note.getCreatedAt()
        );
    }
}
