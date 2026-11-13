package com.csit321g1.lukydizy.memoria.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.csit321g1.lukydizy.memoria.entity.NoteEntity;
import com.csit321g1.lukydizy.memoria.entity.ReviewerEntity;
import com.csit321g1.lukydizy.memoria.dto.ReviewerDto.ReviewerRequest;
import com.csit321g1.lukydizy.memoria.dto.ReviewerDto.ReviewerResponse;
import com.csit321g1.lukydizy.memoria.dto.ReviewerNoteDto.LinkedNote;
import com.csit321g1.lukydizy.memoria.entity.ReviewerNoteEntity;
import com.csit321g1.lukydizy.memoria.exception.InvalidRequestException;
import com.csit321g1.lukydizy.memoria.exception.ResourceInUseException;
import com.csit321g1.lukydizy.memoria.exception.ResourceNotFoundException;
import com.csit321g1.lukydizy.memoria.repository.NoteRepository;
import com.csit321g1.lukydizy.memoria.repository.QuizReviewerRepository;
import com.csit321g1.lukydizy.memoria.repository.ReviewerNoteRepository;
import com.csit321g1.lukydizy.memoria.repository.ReviewerRepository;
import com.csit321g1.lukydizy.memoria.repository.UserRepository;

@Service
public class ReviewerService {

    private final ReviewerRepository reviewers;
    private final ReviewerNoteRepository reviewerNotes;
    private final NoteRepository notes;
    private final QuizReviewerRepository quizReviewers;
    private final UserRepository users;

    /** Creates the service with its repositories. */
    public ReviewerService(
        ReviewerRepository reviewers,
        ReviewerNoteRepository reviewerNotes,
        NoteRepository notes,
        QuizReviewerRepository quizReviewers,
        UserRepository users
    ) {
        this.reviewers = reviewers;
        this.reviewerNotes = reviewerNotes;
        this.notes = notes;
        this.quizReviewers = quizReviewers;
        this.users = users;
    }

    /** Creates a reviewer for the logged-in user and links it to the selected notes. */
    @Transactional
    public ReviewerResponse create(ReviewerRequest request) {
        // Owner comes from the logged-in session, never from the request body.
        String userId = CurrentUser.id();
        // Every selected note must exist and belong to this student; otherwise 400.
        List<NoteEntity> selectedNotes = findOwnedNotes(request.noteIds(), userId);

        // INSERT the reviewer row first so it has an ID for the note links.
        ReviewerEntity reviewer = reviewers.save(new ReviewerEntity(
            users.getReferenceById(userId),
            request.title().trim(),
            request.style().trim(),
            request.content(),
            LocalDateTime.now(ZoneOffset.UTC)
        ));
        // INSERT one reviewer_notes row per selected note.
        addNotes(reviewer, selectedNotes);
        return toResponse(reviewer, userId);
    }

    /** Lists the logged-in user's reviewers, newest first. With a noteId, lists only reviewers made from that note. */
    @Transactional(readOnly = true)
    public List<ReviewerResponse> list(String noteId) {
        String userId = CurrentUser.id();
        // No noteId: all of the user's reviewers. With noteId: only reviewers linked to that note.
        List<ReviewerEntity> found = (noteId == null || noteId.isBlank())
            ? reviewers.findByUser_IdOrderByCreatedAtDesc(userId)
            : reviewerNotes.findByNote_IdAndReviewer_User_Id(noteId, userId).stream()
                .map(ReviewerNoteEntity::getReviewer)
                .sorted(Comparator.comparing(ReviewerEntity::getCreatedAt).reversed())
                .toList();
        return found.stream().map(reviewer -> toResponse(reviewer, userId)).toList();
    }

    /** Returns one of the logged-in user's reviewers with its linked notes. */
    @Transactional(readOnly = true)
    public ReviewerResponse get(String id) {
        String userId = CurrentUser.id();
        return toResponse(findOwned(id, userId), userId);
    }

    /**
     * Updates a reviewer's fields and replaces its linked notes with the submitted list.
     * Notes missing from the list are unlinked; new notes are linked.
     */
    @Transactional
    public ReviewerResponse update(String id, ReviewerRequest request) {
        String userId = CurrentUser.id();
        // 404 if the reviewer is missing or belongs to another student.
        ReviewerEntity reviewer = findOwned(id, userId);
        List<NoteEntity> selectedNotes = findOwnedNotes(request.noteIds(), userId);

        // Changes to a loaded entity are saved automatically when the transaction commits.
        reviewer.setTitle(request.title().trim());
        reviewer.setStyle(request.style().trim());
        reviewer.setContent(request.content());

        // Compare the current links with the submitted list: keep, remove, or add.
        Set<String> selectedIds = new LinkedHashSet<>(request.noteIds());
        List<ReviewerNoteEntity> currentLinks = reviewerNotes.findByReviewer_Id(reviewer.getId());
        Set<String> linkedIds = new LinkedHashSet<>();
        List<ReviewerNoteEntity> linksToRemove = new ArrayList<>();
        for (ReviewerNoteEntity link : currentLinks) {
            String noteId = link.getNote().getId();
            if (selectedIds.contains(noteId)) {
                linkedIds.add(noteId);
            } else {
                linksToRemove.add(link);
            }
        }
        // DELETE links for notes that were unselected.
        removeNotes(linksToRemove);
        // INSERT links only for newly selected notes.
        addNotes(reviewer, selectedNotes.stream().filter(note -> !linkedIds.contains(note.getId())).toList());
        return toResponse(reviewer, userId);
    }

    /** Deletes a reviewer and its note links, unless a quiz still uses it. */
    @Transactional
    public void delete(String id) {
        String userId = CurrentUser.id();
        ReviewerEntity reviewer = findOwned(id, userId);
        // Block the delete if any quiz still uses this reviewer (QuizReviewer rows).
        long quizCount = quizReviewers.countByReviewer_Id(reviewer.getId());
        if (quizCount > 0) {
            throw new ResourceInUseException(
                "This reviewer is used by " + quizCount + (quizCount == 1 ? " quiz" : " quizzes")
                    + ". Remove it from those quizzes before deleting it."
            );
        }
        // Remove the reviewer's note links first, then the reviewer itself.
        removeNotes(reviewerNotes.findByReviewer_Id(reviewer.getId()));
        reviewers.delete(reviewer);
    }

    /** Links each given note to the reviewer. */
    private void addNotes(ReviewerEntity reviewer, List<NoteEntity> notesToAdd) {
        for (NoteEntity note : notesToAdd) {
            reviewerNotes.save(new ReviewerNoteEntity(reviewer, note));
        }
    }

    /** Removes the given reviewer-note links. */
    private void removeNotes(List<ReviewerNoteEntity> linksToRemove) {
        reviewerNotes.deleteAll(linksToRemove);
    }

    /** Finds a reviewer owned by the user; another user's reviewer is treated as missing. */
    private ReviewerEntity findOwned(String id, String userId) {
        return reviewers.findByIdAndUser_Id(id, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found."));
    }

    /** Loads the selected notes and fails if any are missing or belong to another user. */
    private List<NoteEntity> findOwnedNotes(List<String> noteIds, String userId) {
        Set<String> uniqueIds = new LinkedHashSet<>(noteIds);
        // Only this user's notes come back, so a smaller count means a missing or foreign note ID.
        List<NoteEntity> found = notes.findByIdInAndUser_Id(uniqueIds, userId);
        if (found.size() != uniqueIds.size()) {
            Set<String> foundIds = new LinkedHashSet<>();
            found.forEach(note -> foundIds.add(note.getId()));
            List<String> invalidIds = uniqueIds.stream().filter(noteId -> !foundIds.contains(noteId)).toList();
            throw new InvalidRequestException(
                "These notes do not exist or are not yours: " + String.join(", ", invalidIds)
            );
        }
        return found;
    }

    /** Converts a reviewer and its linked notes (owned by the same user) into a response. */
    private ReviewerResponse toResponse(ReviewerEntity reviewer, String userId) {
        // Load the linked notes through the ReviewerNote join table.
        List<LinkedNote> linkedNotes = reviewerNotes
            .findByReviewer_IdAndNote_User_Id(reviewer.getId(), userId).stream()
            .map(link -> new LinkedNote(link.getNote().getId(), link.getNote().getTitle()))
            .toList();
        return new ReviewerResponse(
            reviewer.getId(),
            reviewer.getTitle(),
            reviewer.getStyle(),
            reviewer.getContent(),
            reviewer.getCreatedAt(),
            linkedNotes
        );
    }
}
