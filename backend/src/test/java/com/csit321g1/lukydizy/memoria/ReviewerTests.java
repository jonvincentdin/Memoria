package com.csit321g1.lukydizy.memoria;

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
import java.util.Set;
import java.util.TreeSet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.csit321g1.lukydizy.memoria.entity.NoteEntity;
import com.csit321g1.lukydizy.memoria.entity.QuizEntity;
import com.csit321g1.lukydizy.memoria.entity.QuizReviewerEntity;
import com.csit321g1.lukydizy.memoria.entity.UserEntity;
import com.csit321g1.lukydizy.memoria.repository.NoteRepository;
import com.csit321g1.lukydizy.memoria.repository.QuizRepository;
import com.csit321g1.lukydizy.memoria.repository.QuizReviewerRepository;
import com.csit321g1.lukydizy.memoria.repository.ReviewerRepository;
import com.csit321g1.lukydizy.memoria.repository.UserRepository;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReviewerTests {

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
    private QuizRepository quizzes;

    @Autowired
    private QuizReviewerRepository quizReviewers;

    private String ownerId;
    private String otherId;
    private String noteA;
    private String noteB;
    private String noteC;
    private String otherNote;

    /** Creates two students; the owner has three notes and the other student has one. */
    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM quiz_reviewers");
        jdbc.update("DELETE FROM reviewer_notes");
        jdbc.update("DELETE FROM reviewers");
        jdbc.update("DELETE FROM notes");
        jdbc.update("DELETE FROM users");
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        UserEntity owner = users.save(new UserEntity("Owner", "owner@example.com", "hash", now));
        UserEntity other = users.save(new UserEntity("Other", "other@example.com", "hash", now));
        ownerId = owner.getId();
        otherId = other.getId();
        noteA = notes.save(new NoteEntity(owner, "Cell Biology", "Cells...", "MANUAL", now)).getId();
        noteB = notes.save(new NoteEntity(owner, "Genetics", "DNA...", "MANUAL", now)).getId();
        noteC = notes.save(new NoteEntity(owner, "Ecology", "Ecosystems...", "MANUAL", now)).getId();
        otherNote = notes.save(new NoteEntity(other, "Private", "Not yours", "MANUAL", now)).getId();
    }

    /** Covers create, detail, list, update (add and remove notes), and delete. */
    @Test
    void ownerCanCreateReadListUpdateAndDeleteReviewers() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/reviewers")
                .with(user(ownerId)).with(csrf())
                .contentType("application/json")
                .content(body("Biology Review", "Summary", "Key points...", noteA, noteB)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Biology Review"))
            .andExpect(jsonPath("$.style").value("Summary"))
            .andExpect(jsonPath("$.notes.length()").value(2))
            .andReturn();
        String id = readJson(created).get("id").asText();

        MvcResult detail = mockMvc.perform(get("/api/reviewers/{id}", id).with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Key points..."))
            .andReturn();
        assertEquals(new TreeSet<>(Set.of(noteA, noteB)), noteIds(readJson(detail)));

        mockMvc.perform(get("/api/reviewers").with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(id));

        MvcResult updated = mockMvc.perform(put("/api/reviewers/{id}", id)
                .with(user(ownerId)).with(csrf())
                .contentType("application/json")
                .content(body("Biology Outline", "Outline", "1. Cells", noteA, noteC)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Biology Outline"))
            .andExpect(jsonPath("$.style").value("Outline"))
            .andReturn();
        assertEquals(new TreeSet<>(Set.of(noteA, noteC)), noteIds(readJson(updated)));

        mockMvc.perform(delete("/api/reviewers/{id}", id).with(user(ownerId)).with(csrf()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/reviewers/{id}", id).with(user(ownerId)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Reviewer not found."));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM reviewer_notes", Integer.class));
    }

    /** One note can be linked to several reviewers (many-to-many). */
    @Test
    void oneNoteCanBeLinkedToManyReviewers() throws Exception {
        for (String title : new String[] {"Review 1", "Review 2"}) {
            mockMvc.perform(post("/api/reviewers")
                    .with(user(ownerId)).with(csrf())
                    .contentType("application/json")
                    .content(body(title, "Q&A", "Q: ...", noteA)))
                .andExpect(status().isCreated());
        }
        assertEquals(2, jdbc.queryForObject(
            "SELECT COUNT(*) FROM reviewer_notes WHERE note_id = ?", Integer.class, noteA));

        mockMvc.perform(get("/api/reviewers").param("noteId", noteA).with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/reviewers").param("noteId", noteB).with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/reviewers").param("noteId", noteA).with(user(otherId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    /** Notes that are missing or belong to another student are rejected with a clear message. */
    @Test
    void rejectsMissingOrForeignNotes() throws Exception {
        mockMvc.perform(post("/api/reviewers")
                .with(user(ownerId)).with(csrf())
                .contentType("application/json")
                .content(body("Bad", "Summary", "x", noteA, otherNote)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("These notes do not exist or are not yours: " + otherNote));

        mockMvc.perform(post("/api/reviewers")
                .with(user(ownerId)).with(csrf())
                .contentType("application/json")
                .content(body("Bad", "Summary", "x", "missing-id")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("These notes do not exist or are not yours: missing-id"));

        mockMvc.perform(post("/api/reviewers")
                .with(user(ownerId)).with(csrf())
                .contentType("application/json")
                .content(body("Bad", "Summary", "x")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Select at least one note."));

        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM reviewers", Integer.class));
    }

    /** Another student's reviewer behaves as if it does not exist. */
    @Test
    void anotherStudentsReviewerReturns404() throws Exception {
        String id = createReviewer(noteA);

        mockMvc.perform(get("/api/reviewers/{id}", id).with(user(otherId)))
            .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/reviewers/{id}", id)
                .with(user(otherId)).with(csrf())
                .contentType("application/json")
                .content(body("Hijack", "Summary", "x", otherNote)))
            .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/reviewers/{id}", id).with(user(otherId)).with(csrf()))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/reviewers").with(user(otherId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    /** A reviewer used by a quiz cannot be deleted. */
    @Test
    void deletingAReviewerUsedByAQuizIsBlocked() throws Exception {
        String id = createReviewer(noteA);
        UserEntity owner = users.findById(ownerId).orElseThrow();
        QuizEntity quiz = quizzes.save(new QuizEntity(
            owner, "Biology Quiz", "MULTIPLE_CHOICE", "[]", LocalDateTime.now(ZoneOffset.UTC)));
        quizReviewers.save(new QuizReviewerEntity(quiz, reviewers.findById(id).orElseThrow()));

        mockMvc.perform(delete("/api/reviewers/{id}", id).with(user(ownerId)).with(csrf()))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value(
                "This reviewer is used by 1 quiz. Remove it from those quizzes before deleting it."));

        mockMvc.perform(get("/api/reviewers/{id}", id).with(user(ownerId)))
            .andExpect(status().isOk());
    }

    /** Reviewers require a logged-in session. */
    @Test
    void reviewersRequireLogin() throws Exception {
        mockMvc.perform(get("/api/reviewers"))
            .andExpect(status().isUnauthorized());
    }

    private String createReviewer(String... noteIds) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/reviewers")
                .with(user(ownerId)).with(csrf())
                .contentType("application/json")
                .content(body("Biology Review", "Summary", "Key points...", noteIds)))
            .andExpect(status().isCreated())
            .andReturn();
        return readJson(result).get("id").asText();
    }

    private String body(String title, String style, String content, String... noteIds) {
        StringBuilder ids = new StringBuilder();
        for (int i = 0; i < noteIds.length; i++) {
            ids.append(i == 0 ? "" : ",").append('"').append(noteIds[i]).append('"');
        }
        return "{\"title\":\"" + title + "\",\"style\":\"" + style + "\",\"content\":\"" + content
            + "\",\"noteIds\":[" + ids + "]}";
    }

    private JsonNode readJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private Set<String> noteIds(JsonNode reviewer) {
        Set<String> ids = new TreeSet<>();
        reviewer.get("notes").forEach(note -> ids.add(note.get("id").asText()));
        return ids;
    }
}
