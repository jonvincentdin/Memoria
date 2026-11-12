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
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.csit321g1.lukydizy.memoria.entity.AttemptStatus;
import com.csit321g1.lukydizy.memoria.entity.QuizAttemptEntity;
import com.csit321g1.lukydizy.memoria.entity.ReviewerEntity;
import com.csit321g1.lukydizy.memoria.entity.UserEntity;
import com.csit321g1.lukydizy.memoria.repository.QuizAttemptRepository;
import com.csit321g1.lukydizy.memoria.repository.QuizRepository;
import com.csit321g1.lukydizy.memoria.repository.ReviewerRepository;
import com.csit321g1.lukydizy.memoria.repository.UserRepository;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuizTests {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private UserRepository users;
    @Autowired private ReviewerRepository reviewers;
    @Autowired private QuizRepository quizzes;
    @Autowired private QuizAttemptRepository attempts;

    private UserEntity owner;
    private String ownerId;
    private String otherId;
    private String reviewerA;
    private String reviewerB;
    private String otherReviewer;

    /** Two students: the owner has two reviewers, the other student has one. */
    @BeforeEach
    void setUp() {
        // Children before parents, so foreign keys don't block the cleanup.
        jdbc.update("DELETE FROM quiz_attempts");
        jdbc.update("DELETE FROM quiz_reviewers");
        jdbc.update("DELETE FROM quizzes");
        jdbc.update("DELETE FROM reviewer_notes");
        jdbc.update("DELETE FROM reviewers");
        jdbc.update("DELETE FROM notes");
        jdbc.update("DELETE FROM users");
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        owner = users.save(new UserEntity("Owner", "owner@example.com", "hash", now));
        UserEntity other = users.save(new UserEntity("Other", "other@example.com", "hash", now));
        ownerId = owner.getId();
        otherId = other.getId();
        reviewerA = reviewers.save(new ReviewerEntity(owner, "Cells", "Summary", "...", now)).getId();
        reviewerB = reviewers.save(new ReviewerEntity(owner, "Genetics", "Summary", "...", now)).getId();
        otherReviewer = reviewers.save(new ReviewerEntity(other, "Private", "Summary", "...", now)).getId();
    }

    /** Criterion 1 and 2: full CRUD, linked to two reviewers. */
    @Test
    void ownerCanCreateReadListUpdateAndDeleteQuiz() throws Exception {
        String id = create(body("Biology Quiz", 2, 4, 1, reviewerA, reviewerB));

        mockMvc.perform(get("/api/quizzes/{id}", id).with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.questions.length()").value(2))
            .andExpect(jsonPath("$.questions[0].correctIndex").value(1))
            .andExpect(jsonPath("$.reviewers.length()").value(2))
            .andExpect(jsonPath("$.hasAttempts").value(false));

        mockMvc.perform(get("/api/quizzes").with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));

        // Drop reviewerB, keep reviewerA, rename.
        mockMvc.perform(put("/api/quizzes/{id}", id).with(user(ownerId)).with(csrf())
                .contentType("application/json").content(body("Renamed", 2, 4, 1, reviewerA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Renamed"))
            .andExpect(jsonPath("$.reviewers.length()").value(1));

        mockMvc.perform(delete("/api/quizzes/{id}", id).with(user(ownerId)).with(csrf()))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/quizzes/{id}", id).with(user(ownerId)))
            .andExpect(status().isNotFound());
    }

    /** Criterion 4: bad question data is rejected and nothing is saved. */
    @Test
    void invalidQuestionsAreRejected() throws Exception {
        for (String bad : List.of(
            body("Three choices", 1, 3, 0, reviewerA),
            body("Too many", 21, 4, 0, reviewerA),
            body("Bad answer index", 1, 4, 4, reviewerA)
        )) {
            mockMvc.perform(post("/api/quizzes").with(user(ownerId)).with(csrf())
                    .contentType("application/json").content(bad))
                .andExpect(status().isBadRequest());
        }
        assertEquals(0, quizzes.count());
    }

    /** Criterion 2 and 3: another student's reviewer is rejected and the transaction saves nothing. */
    @Test
    void foreignReviewerIsRejectedAndNothingIsSaved() throws Exception {
        mockMvc.perform(post("/api/quizzes").with(user(ownerId)).with(csrf())
                .contentType("application/json").content(body("Quiz", 1, 4, 0, reviewerA, otherReviewer)))
            .andExpect(status().isBadRequest());
        assertEquals(0, quizzes.count());
    }

    /** Criterion 4: another student's quiz is a 404 for read, update, and delete. */
    @Test
    void otherStudentsQuizReturns404() throws Exception {
        String id = create(body("Mine", 1, 4, 0, reviewerA));
        mockMvc.perform(get("/api/quizzes/{id}", id).with(user(otherId)))
            .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/quizzes/{id}", id).with(user(otherId)).with(csrf())
                .contentType("application/json").content(body("Hijack", 1, 4, 0, otherReviewer)))
            .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/quizzes/{id}", id).with(user(otherId)).with(csrf()))
            .andExpect(status().isNotFound());
    }

    /** Criterion 5: after an attempt, questions are locked, the title isn't, and delete is blocked. */
    @Test
    void attemptsLockQuestionsAndBlockDelete() throws Exception {
        String id = create(body("Quiz", 1, 4, 0, reviewerA));
        attempts.save(new QuizAttemptEntity(owner, quizzes.findById(id).orElseThrow(), 1, 1,
            AttemptStatus.COMPLETED, LocalDateTime.now(ZoneOffset.UTC)));

        // Changed answer -> questions changed -> 409.
        mockMvc.perform(put("/api/quizzes/{id}", id).with(user(ownerId)).with(csrf())
                .contentType("application/json").content(body("Quiz", 1, 4, 2, reviewerA)))
            .andExpect(status().isConflict());

        // Same questions, new title -> allowed.
        mockMvc.perform(put("/api/quizzes/{id}", id).with(user(ownerId)).with(csrf())
                .contentType("application/json").content(body("New title", 1, 4, 0, reviewerA)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hasAttempts").value(true));

        mockMvc.perform(delete("/api/quizzes/{id}", id).with(user(ownerId)).with(csrf()))
            .andExpect(status().isConflict());
    }

    /** Creates a quiz as the owner and returns its id. */
    private String create(String json) throws Exception {
        String response = mockMvc.perform(post("/api/quizzes").with(user(ownerId)).with(csrf())
                .contentType("application/json").content(json))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(response).get("id").asText();
    }

    /** Builds a request with `questionCount` questions, each with `choiceCount` choices. */
    private String body(String title, int questionCount, int choiceCount, int correctIndex, String... reviewerIds) {
        List<Map<String, Object>> questions = IntStream.range(0, questionCount)
            .mapToObj(q -> Map.<String, Object>of(
                "text", "Question " + q,
                "choices", IntStream.range(0, choiceCount).mapToObj(c -> "Choice " + c).toList(),
                "correctIndex", correctIndex))
            .toList();
        return objectMapper.writeValueAsString(Map.of(
            "title", title, "mode", "PRACTICE", "questions", questions, "reviewerIds", List.of(reviewerIds)));
    }
}
