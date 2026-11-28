package com.csit321g1.lukydizy.memoria.flashcards;

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

import com.csit321g1.lukydizy.memoria.flashcards.FlashcardDto.Request;
import com.csit321g1.lukydizy.memoria.flashcards.FlashcardDto.ReviewRequest;
import com.csit321g1.lukydizy.memoria.reviewers.ReviewerEntity;
import com.csit321g1.lukydizy.memoria.reviewers.ReviewerRepository;
import com.csit321g1.lukydizy.memoria.users.UserEntity;
import com.csit321g1.lukydizy.memoria.users.UserRepository;

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
        jdbc.update("DELETE FROM flashcard_reviews");
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
        mockMvc.perform(get("/api/flashcards/reviews"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/flashcards/progress"))
            .andExpect(status().isUnauthorized());
    }

    /** Submitting reviews persists history, updates SM-2 schedule, and supports due-only queries. */
    @Test
    void reviewPersistsHistoryUpdatesScheduleAndFiltersDueCards() throws Exception {
        UserEntity owner = users.findById(ownerId).orElseThrow();
        ReviewerEntity revA = reviewers.findById(reviewerA).orElseThrow();
        ReviewerEntity revB = reviewers.findById(reviewerB).orElseThrow();

        FlashcardEntity overdue = saveFlashcard(owner, revA, "Overdue card", "Answer 1");
        overdue.applyReviewSchedule(
            1, 1, 2.5, 4,
            LocalDateTime.now(ZoneOffset.UTC).minusDays(3),
            LocalDateTime.now(ZoneOffset.UTC).minusHours(2)
        );
        flashcards.save(overdue);

        FlashcardEntity fresh = saveFlashcard(owner, revA, "New card", "Answer 2");
        FlashcardEntity otherRevCard = saveFlashcard(owner, revB, "Chem card", "Answer 3");

        // Due query returns overdue card before never-reviewed card.
        mockMvc.perform(get("/api/flashcards")
                .param("reviewerId", reviewerA)
                .param("dueOnly", "true")
                .with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(overdue.getId()))
            .andExpect(jsonPath("$[0].due").value(true))
            .andExpect(jsonPath("$[1].id").value(fresh.getId()))
            .andExpect(jsonPath("$[1].status").value("NEW"));

        // Review overdue card (second consecutive passing rating -> 6 day interval).
        mockMvc.perform(post("/api/flashcards/{id}/review", overdue.getId())
                .with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewBody(4)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.reviewId").exists())
            .andExpect(jsonPath("$.flashcardId").value(overdue.getId()))
            .andExpect(jsonPath("$.rating").value(4))
            .andExpect(jsonPath("$.repetitionCount").value(2))
            .andExpect(jsonPath("$.intervalDays").value(6))
            .andExpect(jsonPath("$.easeFactor").value(2.5))
            .andExpect(jsonPath("$.nextReviewAt").exists())
            .andExpect(jsonPath("$.status").value("LEARNING"));

        // Third review pushes overdue card to MASTERED (15 day interval).
        mockMvc.perform(post("/api/flashcards/{id}/review", overdue.getId())
                .with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewBody(4)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.repetitionCount").value(3))
            .andExpect(jsonPath("$.intervalDays").value(15))
            .andExpect(jsonPath("$.status").value("MASTERED"));

        // Now only fresh is due in reviewerA, while overdue + fresh + otherRevCard are all returned when dueOnly=false.
        mockMvc.perform(get("/api/flashcards")
                .param("reviewerId", reviewerA)
                .param("dueOnly", "true")
                .with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(fresh.getId()));

        mockMvc.perform(get("/api/flashcards")
                .param("dueOnly", "true")
                .with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        // Review fresh with failing rating 1 -> resets repetitions and schedules 1 day out.
        mockMvc.perform(post("/api/flashcards/{id}/review", fresh.getId())
                .with(user(ownerId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewBody(1)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.repetitionCount").value(0))
            .andExpect(jsonPath("$.intervalDays").value(1))
            .andExpect(jsonPath("$.status").value("LEARNING"));

        // Review history lists 3 reviews for reviewerA (newest first) and supports flashcardId filter.
        mockMvc.perform(get("/api/flashcards/reviews")
                .param("reviewerId", reviewerA)
                .with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].flashcardId").value(fresh.getId()))
            .andExpect(jsonPath("$[0].rating").value(1))
            .andExpect(jsonPath("$[0].reviewerTitle").value("Biology"));

        mockMvc.perform(get("/api/flashcards/reviews")
                .param("flashcardId", overdue.getId())
                .with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        // Progress summary for reviewerA: 2 cards, 0 due, 0 new, 1 learning, 1 mastered, 3 reviews, avg rating 3.0.
        mockMvc.perform(get("/api/flashcards/progress")
                .param("reviewerId", reviewerA)
                .with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCards").value(2))
            .andExpect(jsonPath("$.dueCards").value(0))
            .andExpect(jsonPath("$.newCards").value(0))
            .andExpect(jsonPath("$.learningCards").value(1))
            .andExpect(jsonPath("$.masteredCards").value(1))
            .andExpect(jsonPath("$.totalReviews").value(3))
            .andExpect(jsonPath("$.reviewsToday").value(3))
            .andExpect(jsonPath("$.averageRating").value(3.0))
            .andExpect(jsonPath("$.lastReviewedAt").exists())
            .andExpect(jsonPath("$.nextDueAt").exists());

        // Global progress includes otherRevCard (1 due, 1 new).
        mockMvc.perform(get("/api/flashcards/progress").with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCards").value(3))
            .andExpect(jsonPath("$.dueCards").value(1))
            .andExpect(jsonPath("$.newCards").value(1));

        // Deleting a reviewed card cleanly deletes its review history too.
        mockMvc.perform(delete("/api/flashcards/{id}", overdue.getId()).with(user(ownerId)).with(csrf()))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/flashcards/reviews").param("reviewerId", reviewerA).with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].flashcardId").value(fresh.getId()));

        // Unused variable guard
        assertEquals(reviewerB, otherRevCard.getReviewer().getId());
    }

    /** Review history and progress endpoints isolate data per authenticated student. */
    @Test
    void reviewHistoryAndProgressEnforceOwnership() throws Exception {
        UserEntity other = users.findById(otherId).orElseThrow();
        FlashcardEntity otherCard = saveFlashcard(other, reviewers.findById(otherReviewer).orElseThrow(),
            "Secret", "Answer");

        mockMvc.perform(post("/api/flashcards/{id}/review", otherCard.getId())
                .with(user(otherId)).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(reviewBody(5)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/flashcards/reviews").with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/flashcards/reviews")
                .param("reviewerId", otherReviewer)
                .with(user(ownerId)))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/flashcards/reviews")
                .param("flashcardId", otherCard.getId())
                .with(user(ownerId)))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/flashcards/progress")
                .param("reviewerId", otherReviewer)
                .with(user(ownerId)))
            .andExpect(status().isBadRequest());

        mockMvc.perform(get("/api/flashcards/progress").with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCards").value(0))
            .andExpect(jsonPath("$.totalReviews").value(0));
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
        return objectMapper.writeValueAsString(new Request(reviewerId, front, back));
    }

    private String reviewBody(int rating) throws Exception {
        return objectMapper.writeValueAsString(new ReviewRequest(rating));
    }
}
