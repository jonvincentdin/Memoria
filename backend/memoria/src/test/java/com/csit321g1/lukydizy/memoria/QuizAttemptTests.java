package com.csit321g1.lukydizy.memoria;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.csit321g1.lukydizy.memoria.dto.AttemptResponse;
import com.csit321g1.lukydizy.memoria.entity.QuizEntity;
import com.csit321g1.lukydizy.memoria.entity.UserEntity;
import com.csit321g1.lukydizy.memoria.exception.InvalidRequestException;
import com.csit321g1.lukydizy.memoria.exception.ResourceNotFoundException;
import com.csit321g1.lukydizy.memoria.repository.QuizRepository;
import com.csit321g1.lukydizy.memoria.repository.UserRepository;
import com.csit321g1.lukydizy.memoria.service.QuizAttemptService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class QuizAttemptTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private UserRepository users;

    @Autowired
    private QuizRepository quizzes;

    @Autowired
    private QuizAttemptService attemptService;

    private String ownerId;
    private String otherId;
    private String ownerQuizId;
    private String otherQuizId;

    /** Creates two students, each with one quiz. */
    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM quiz_attempts");
        jdbc.update("DELETE FROM quizzes");
        jdbc.update("DELETE FROM users");
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        UserEntity owner = users.save(new UserEntity("Owner", "owner@example.com", "hash", now));
        UserEntity other = users.save(new UserEntity("Other", "other@example.com", "hash", now));
        ownerId = owner.getId();
        otherId = other.getId();
        ownerQuizId = quizzes.save(new QuizEntity(owner, "Biology Quiz", "MULTIPLE_CHOICE", "[]", now)).getId();
        otherQuizId = quizzes.save(new QuizEntity(other, "History Quiz", "MULTIPLE_CHOICE", "[]", now)).getId();
    }

    @AfterEach
    void clearSession() {
        SecurityContextHolder.clearContext();
    }

    /** Covers create (service), list newest first, filter, read, delete, and the summary fields. */
    @Test
    void ownerCanCreateReadListAndDeleteAttempts() throws Exception {
        loginAs(ownerId);
        AttemptResponse first = attemptService.create(ownerQuizId, 7, 10);
        Thread.sleep(10);
        AttemptResponse second = attemptService.create(ownerQuizId, 9, 10);
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/attempts").with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(second.id()))
            .andExpect(jsonPath("$[1].id").value(first.id()));

        mockMvc.perform(get("/api/attempts").param("quizId", ownerQuizId).with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/attempts/{id}", first.id()).with(user(ownerId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quizId").value(ownerQuizId))
            .andExpect(jsonPath("$.quizTitle").value("Biology Quiz"))
            .andExpect(jsonPath("$.score").value(7))
            .andExpect(jsonPath("$.totalQuestions").value(10))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
            .andExpect(jsonPath("$.completedAt").exists());

        mockMvc.perform(delete("/api/attempts/{id}", first.id()).with(user(ownerId)).with(csrf()))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/attempts/{id}", first.id()).with(user(ownerId)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Attempt not found."));
    }

    /** Another student's attempt behaves as if it does not exist. */
    @Test
    void anotherStudentsAttemptReturns404() throws Exception {
        loginAs(ownerId);
        AttemptResponse attempt = attemptService.create(ownerQuizId, 5, 5);
        SecurityContextHolder.clearContext();

        mockMvc.perform(get("/api/attempts/{id}", attempt.id()).with(user(otherId)))
            .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/attempts/{id}", attempt.id()).with(user(otherId)).with(csrf()))
            .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/attempts").with(user(otherId)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/attempts/{id}", attempt.id()).with(user(ownerId)))
            .andExpect(status().isOk());
    }

    /** An attempt can only be saved for the user's own quiz and with a valid score. */
    @Test
    void createRejectsOtherUsersQuizAndInvalidScores() {
        loginAs(ownerId);
        assertThrows(ResourceNotFoundException.class, () -> attemptService.create(otherQuizId, 1, 5));
        assertThrows(InvalidRequestException.class, () -> attemptService.create(ownerQuizId, 6, 5));
        assertThrows(InvalidRequestException.class, () -> attemptService.create(ownerQuizId, -1, 5));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM quiz_attempts", Integer.class));
    }

    /** History requires a logged-in session. */
    @Test
    void attemptsRequireLogin() throws Exception {
        mockMvc.perform(get("/api/attempts"))
            .andExpect(status().isUnauthorized());
    }

    /** ERD decision: no AttemptAnswer table in Increment 1. */
    @Test
    void noAttemptAnswerTableExists() {
        Integer tables = jdbc.queryForObject(
            "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE UPPER(TABLE_NAME) LIKE '%ANSWER%'",
            Integer.class
        );
        assertEquals(0, tables);
    }

    /** Puts a user into the security context, like a logged-in session does. */
    private void loginAs(String userId) {
        SecurityContextHolder.getContext().setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(userId, null, List.of())
        );
    }
}
