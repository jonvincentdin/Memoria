package com.csit321g1.lukydizy.memoria.notes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.csit321g1.lukydizy.memoria.notes.NoteDto.NoteRequest;
import com.csit321g1.lukydizy.memoria.reviewers.ReviewerEntity;
import com.csit321g1.lukydizy.memoria.reviewers.ReviewerNoteEntity;
import com.csit321g1.lukydizy.memoria.reviewers.ReviewerNoteRepository;
import com.csit321g1.lukydizy.memoria.reviewers.ReviewerRepository;
import com.csit321g1.lukydizy.memoria.users.UserEntity;
import com.csit321g1.lukydizy.memoria.users.UserRepository;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NoteTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UserRepository users;

    @Autowired
    private NoteRepository notes;

    @Autowired
    private ReviewerRepository reviewers;

    @Autowired
    private ReviewerNoteRepository reviewerNotes;

    private String ownerId;
    private String otherId;

    /** Creates two isolated users before each note API test. */
    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM quiz_reviewers");
        jdbc.update("DELETE FROM reviewer_notes");
        jdbc.update("DELETE FROM quiz_attempts");
        jdbc.update("DELETE FROM quizzes");
        jdbc.update("DELETE FROM reviewers");
        jdbc.update("DELETE FROM notes");
        jdbc.update("DELETE FROM users");
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        ownerId = users.save(new UserEntity("Owner", "owner@example.com", "hash", now)).getId();
        otherId = users.save(new UserEntity("Other", "other@example.com", "hash", now)).getId();
    }

    /** Covers create, read, update, persistence, and delete for an owned note. */
    @Test
    void ownerCanCreateReadUpdateAndDeleteNote() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/notes")
                .with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "title", "Biology",
                    "content", "Cell structure",
                    "userId", otherId,
                    "sourceType", "AI"
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Biology"))
            .andExpect(jsonPath("$.content").value("Cell structure"))
            .andExpect(jsonPath("$.sourceType").value("MANUAL"))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.userId").doesNotExist())
            .andReturn();
        String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        assertEquals(ownerId, notes.findById(id).orElseThrow().getUser().getId());
        mockMvc.perform(get("/api/notes/{id}", id).with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Cell structure"));

        mockMvc.perform(put("/api/notes/{id}", id)
                .with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("Biology outline", "Cells contain organelles.")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Biology outline"))
            .andExpect(jsonPath("$.content").value("Cells contain organelles."));

        mockMvc.perform(get("/api/notes/{id}", id).with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Biology outline"))
            .andExpect(jsonPath("$.content").value("Cells contain organelles."));

        mockMvc.perform(delete("/api/notes/{id}", id).with(user(ownerId)).with(csrf()))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/notes/{id}", id).with(user(ownerId)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Note not found."));
        mockMvc.perform(get("/api/notes").with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    /** Returns only owned notes and orders them by creation time, newest first. */
    @Test
    void listIsOwnerScopedAndNewestFirst() throws Exception {
        UserEntity owner = users.findById(ownerId).orElseThrow();
        UserEntity other = users.findById(otherId).orElseThrow();
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        String oldestId = saveNote(owner, "Oldest", "A", now.minusDays(2)).getId();
        String newestId = saveNote(owner, "Newest", "B", now).getId();
        saveNote(other, "Private", "C", now.plusDays(1));

        mockMvc.perform(get("/api/notes").with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(newestId))
            .andExpect(jsonPath("$[1].id").value(oldestId));
        mockMvc.perform(get("/api/notes").with(user(otherId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].title").value("Private"));
    }

    /** Treats another student's note as missing for read, update, and delete. */
    @Test
    void anotherStudentsNoteReturns404() throws Exception {
        NoteEntity note = saveNote(users.findById(ownerId).orElseThrow(), "Private", "Keep out",
            LocalDateTime.now(ZoneOffset.UTC));

        mockMvc.perform(get("/api/notes/{id}", note.getId()).with(user(otherId)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Note not found."));
        mockMvc.perform(put("/api/notes/{id}", note.getId())
                .with(user(otherId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body("Changed", "Not allowed")))
            .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/notes/{id}", note.getId()).with(user(otherId)).with(csrf()))
            .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/notes/{id}", note.getId()).with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Private"));
    }

    /** Rejects blank and oversized values while allowing both exact maximum lengths. */
    @Test
    void validatesRequiredAndMaximumFieldLengths() throws Exception {
        mockMvc.perform(post("/api/notes").with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body(" ", "valid")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Title is required."));
        mockMvc.perform(post("/api/notes").with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body("valid", "  ")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Content is required."));
        mockMvc.perform(post("/api/notes").with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body("T".repeat(151), "valid")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Title must be 150 characters or fewer."));
        mockMvc.perform(post("/api/notes").with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body("valid", "C".repeat(20001))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Content must be 20000 characters or fewer."));
        mockMvc.perform(post("/api/notes").with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body("T".repeat(150), "C".repeat(20000))))
            .andExpect(status().isCreated());
    }

    /** Prevents deleting a note that remains linked to a reviewer. */
    @Test
    void cannotDeleteNoteLinkedToReviewer() throws Exception {
        UserEntity owner = users.findById(ownerId).orElseThrow();
        NoteEntity note = saveNote(owner, "Linked", "Reviewer source", LocalDateTime.now(ZoneOffset.UTC));
        ReviewerEntity reviewer = reviewers.save(new ReviewerEntity(
            owner, "Review", "Summary", "Reviewed content", LocalDateTime.now(ZoneOffset.UTC)));
        reviewerNotes.save(new ReviewerNoteEntity(reviewer, note));

        mockMvc.perform(delete("/api/notes/{id}", note.getId()).with(user(ownerId)).with(csrf()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value(
                "This note is linked to 1 reviewer. Remove it from those reviewers before deleting it."));
        mockMvc.perform(get("/api/notes/{id}", note.getId()).with(user(ownerId)))
            .andExpect(status().isOk());
    }

    /** Notes routes require an authenticated session. */
    @Test
    void notesRequireLogin() throws Exception {
        mockMvc.perform(get("/api/notes"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/notes/any-id/archive").with(csrf()))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/api/notes/any-id/restore").with(csrf()))
            .andExpect(status().isUnauthorized());
    }

    /** Archives and restores an owned note, separating archived notes from active notes. */
    @Test
    void ownerCanArchiveAndRestoreNote() throws Exception {
        UserEntity owner = users.findById(ownerId).orElseThrow();
        NoteEntity active = saveNote(owner, "Active Note", "Body A", LocalDateTime.now(ZoneOffset.UTC).minusHours(1));
        NoteEntity toArchive = saveNote(owner, "Archive Me", "Body B", LocalDateTime.now(ZoneOffset.UTC));

        mockMvc.perform(post("/api/notes/{id}/archive", toArchive.getId()).with(user(ownerId)).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.archived").value(true))
            .andExpect(jsonPath("$.archivedAt").isNotEmpty());

        mockMvc.perform(get("/api/notes").with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(active.getId()));

        mockMvc.perform(get("/api/notes").param("archived", "true").with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(toArchive.getId()))
            .andExpect(jsonPath("$[0].archived").value(true));

        mockMvc.perform(post("/api/notes/{id}/restore", toArchive.getId()).with(user(ownerId)).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.archived").value(false))
            .andExpect(jsonPath("$.archivedAt").doesNotExist());

        mockMvc.perform(get("/api/notes").with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/notes").param("archived", "true").with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    /** Another student cannot archive or restore a note they do not own. */
    @Test
    void anotherStudentCannotArchiveOrRestoreNote() throws Exception {
        NoteEntity note = saveNote(users.findById(ownerId).orElseThrow(), "Private", "Content",
            LocalDateTime.now(ZoneOffset.UTC));

        mockMvc.perform(post("/api/notes/{id}/archive", note.getId()).with(user(otherId)).with(csrf()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Note not found."));
        mockMvc.perform(post("/api/notes/{id}/restore", note.getId()).with(user(otherId)).with(csrf()))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Note not found."));
    }

    /** Deleting an archived note linked to a reviewer is still blocked with 409. */
    @Test
    void cannotDeleteArchivedNoteLinkedToReviewer() throws Exception {
        UserEntity owner = users.findById(ownerId).orElseThrow();
        NoteEntity note = saveNote(owner, "Linked", "Reviewer source", LocalDateTime.now(ZoneOffset.UTC));
        ReviewerEntity reviewer = reviewers.save(new ReviewerEntity(
            owner, "Review", "Summary", "Reviewed content", LocalDateTime.now(ZoneOffset.UTC)));
        reviewerNotes.save(new ReviewerNoteEntity(reviewer, note));

        mockMvc.perform(post("/api/notes/{id}/archive", note.getId()).with(user(ownerId)).with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.archived").value(true));

        mockMvc.perform(delete("/api/notes/{id}", note.getId()).with(user(ownerId)).with(csrf()))
            .andExpect(status().isConflict());
    }

    /** Saves an entity for setup cases that need explicit ownership or creation times. */
    private NoteEntity saveNote(UserEntity owner, String title, String content, LocalDateTime createdAt) {
        return notes.save(new NoteEntity(owner, title, content, "MANUAL", createdAt));
    }

    /** Serializes a public request body without exposing owner or persistence fields. */
    private String body(String title, String content) throws Exception {
        return objectMapper.writeValueAsString(new NoteRequest(title, content));
    }
}
