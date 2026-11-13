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
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.csit321g1.lukydizy.memoria.dto.FlashcardRequest;
import com.csit321g1.lukydizy.memoria.dto.FlashcardReviewRequest;
import com.csit321g1.lukydizy.memoria.entity.FlashcardEntity;
import com.csit321g1.lukydizy.memoria.entity.ReviewerEntity;
import com.csit321g1.lukydizy.memoria.entity.UserEntity;
import com.csit321g1.lukydizy.memoria.repository.FlashcardRepository;
import com.csit321g1.lukydizy.memoria.repository.ReviewerRepository;
import com.csit321g1.lukydizy.memoria.repository.UserRepository;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FlashcardTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UserRepository users;

    @Autowired
    private ReviewerRepository reviewers;

    @Autowired
    private FlashcardRepository flashcards;

    private String ownerId;
    private String otherId;
    private String reviewerA;
    private String reviewerB;
    private String otherReviewer;

    /** Creates two students and reviewers for ownership and filtering cases. */
    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM quiz_reviewers");
        jdbc.update("DELETE FROM reviewer_notes");
        jdbc.update("DELETE FROM quiz_attempts");
        jdbc.update("DELETE FROM quizzes");
        jdbc.update("DELETE FROM flashcards");
        jdbc.update("DELETE FROM reviewers");
        jdbc.update("DELETE FROM notes");
        jdbc.update("DELETE FROM users");
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        UserEntity owner = users.save(new UserEntity("Owner", "owner@example.com", "hash", now));
        UserEntity other = users.save(new UserEntity("Other", "other@example.com", "hash", now));
        ownerId = owner.getId();
        otherId = other.getId();
        reviewerA = saveReviewer(owner, "Biology").getId();
        reviewerB = saveReviewer(owner, "Chemistry").getId();
        otherReviewer = saveReviewer(other, "Private").getId();
    }

    /** Covers create, read, update, persistence, and delete for an owned flashcard. */
    @Test
    void ownerCanCreateReadUpdateAndDeleteFlashcard() throws Exception {
        MvcResult created = mockMvc.perform(post("/api/flashcards")
                .with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                    "reviewerId", reviewerA,
                    "front", "  What is a cell?  ",
                    "back", "  The basic unit of life.  ",
                    "userId", otherId
                ))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.reviewerId").value(reviewerA))
            .andExpect(jsonPath("$.front").value("What is a cell?"))
            .andExpect(jsonPath("$.back").value("The basic unit of life."))
            .andExpect(jsonPath("$.createdAt").exists())
            .andExpect(jsonPath("$.userId").doesNotExist())
            .andReturn();
        String id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asText();

        FlashcardEntity saved = flashcards.findById(id).orElseThrow();
        assertEquals(ownerId, saved.getUser().getId());
        assertEquals(reviewerA, saved.getReviewer().getId());

        mockMvc.perform(get("/api/flashcards/{id}", id).with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.front").value("What is a cell?"));

        mockMvc.perform(put("/api/flashcards/{id}", id)
                .with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(reviewerB, "What is H2O?", "Water")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reviewerId").value(reviewerB))
            .andExpect(jsonPath("$.front").value("What is H2O?"))
            .andExpect(jsonPath("$.back").value("Water"));

        mockMvc.perform(get("/api/flashcards/{id}", id).with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reviewerId").value(reviewerB));

        mockMvc.perform(delete("/api/flashcards/{id}", id).with(user(ownerId)).with(csrf()))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/flashcards/{id}", id).with(user(ownerId)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Flashcard not found."));
    }

    /** Lists only owned cards and supports filtering by an owned reviewer. */
    @Test
    void listIsOwnerScopedAndCanFilterByReviewer() throws Exception {
        UserEntity owner = users.findById(ownerId).orElseThrow();
        UserEntity other = users.findById(otherId).orElseThrow();
        FlashcardEntity biology = saveFlashcard(owner, reviewers.findById(reviewerA).orElseThrow(),
            "Cell?", "Basic unit");
        saveFlashcard(owner, reviewers.findById(reviewerB).orElseThrow(), "H2O?", "Water");
        saveFlashcard(other, reviewers.findById(otherReviewer).orElseThrow(), "Private?", "Secret");

        mockMvc.perform(get("/api/flashcards").with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/flashcards").param("reviewerId", reviewerA).with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(biology.getId()));
        mockMvc.perform(get("/api/flashcards").param("reviewerId", otherReviewer).with(user(ownerId)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Reviewer not found or does not belong to this user."));
    }

    /** Treats another student's card as missing for all individual operations. */
    @Test
    void anotherStudentsFlashcardReturns404() throws Exception {
        UserEntity other = users.findById(otherId).orElseThrow();
        FlashcardEntity card = saveFlashcard(other, reviewers.findById(otherReviewer).orElseThrow(),
            "Private", "Keep out");

        mockMvc.perform(get("/api/flashcards/{id}", card.getId()).with(user(ownerId)))
            .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/flashcards/{id}", card.getId())
                .with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(reviewerA, "Changed", "Not allowed")))
            .andExpect(status().isNotFound());
        mockMvc.perform(post("/api/flashcards/{id}/review", card.getId())
                .with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewBody(3)))
            .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/flashcards/{id}", card.getId()).with(user(ownerId)).with(csrf()))
            .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/flashcards/{id}", card.getId()).with(user(otherId)))
            .andExpect(status().isOk());
    }

    /** Rejects reviewers that are missing or belong to another student. */
    @Test
    void rejectsMissingOrForeignReviewerOnCreateAndUpdate() throws Exception {
        mockMvc.perform(post("/api/flashcards")
                .with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(otherReviewer, "Question", "Answer")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Reviewer not found or does not belong to this user."));

        FlashcardEntity card = saveFlashcard(users.findById(ownerId).orElseThrow(),
            reviewers.findById(reviewerA).orElseThrow(), "Question", "Answer");
        mockMvc.perform(put("/api/flashcards/{id}", card.getId())
                .with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(otherReviewer, "Changed", "Not allowed")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Reviewer not found or does not belong to this user."));
    }

    /** Rejects blank front and back values with clear validation messages. */
    @Test
    void validatesRequiredFlashcardFields() throws Exception {
        mockMvc.perform(post("/api/flashcards").with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(reviewerA, "  ", "Answer")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Front is required."));
        mockMvc.perform(post("/api/flashcards").with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body(reviewerA, "Question", " \t ")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Back is required."));
        mockMvc.perform(post("/api/flashcards").with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("front", "Question", "back", "Answer"))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Reviewer ID is required."));
    }

    /** Accepts ratings 1–5 and rejects out-of-range or missing values without scheduling. */
    @Test
    void validatesAndReturnsReviewRating() throws Exception {
        FlashcardEntity card = saveFlashcard(users.findById(ownerId).orElseThrow(),
            reviewers.findById(reviewerA).orElseThrow(), "Question", "Answer");

        for (int rating : new int[] {1, 5}) {
            mockMvc.perform(post("/api/flashcards/{id}/review", card.getId())
                    .with(user(ownerId)).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reviewBody(rating)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flashcardId").value(card.getId()))
                .andExpect(jsonPath("$.rating").value(rating));
        }

        for (int rating : new int[] {0, 6}) {
            mockMvc.perform(post("/api/flashcards/{id}/review", card.getId())
                    .with(user(ownerId)).with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(reviewBody(rating)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Rating must be between 1 and 5."));
        }
        mockMvc.perform(post("/api/flashcards/{id}/review", card.getId())
                .with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of())))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Rating is required."));
    }

    /** Flashcard routes require an authenticated session. */
    @Test
    void flashcardsRequireLogin() throws Exception {
        mockMvc.perform(get("/api/flashcards"))
            .andExpect(status().isUnauthorized());
    }

    private ReviewerEntity saveReviewer(UserEntity owner, String title) {
        return reviewers.save(new ReviewerEntity(
            owner,
            title,
            "Summary",
            "Reviewer content",
            LocalDateTime.now(ZoneOffset.UTC)
        ));
    }

    private FlashcardEntity saveFlashcard(UserEntity owner, ReviewerEntity reviewer, String front, String back) {
        return flashcards.save(new FlashcardEntity(
            owner,
            reviewer,
            front,
            back,
            LocalDateTime.now(ZoneOffset.UTC)
        ));
    }

    private String body(String reviewerId, String front, String back) throws Exception {
        return objectMapper.writeValueAsString(new FlashcardRequest(reviewerId, front, back));
    }

    private String reviewBody(int rating) throws Exception {
        return objectMapper.writeValueAsString(new FlashcardReviewRequest(rating));
    }
}
