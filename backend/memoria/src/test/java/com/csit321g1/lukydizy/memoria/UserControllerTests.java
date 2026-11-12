package com.csit321g1.lukydizy.memoria;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    /** Clears users before each test. */
    @BeforeEach
    void clearUsers() {
        jdbc.update("DELETE FROM users");
    }

    /** Exercises public create, read, update, and delete operations. */
    @Test
    void usersCanBeCreatedReadUpdatedAndDeleted() throws Exception {
        LocalDateTime beforeCreate = LocalDateTime.now(ZoneOffset.UTC);
        MvcResult createResult = mockMvc.perform(post("/api/users")
                .contentType("application/json")
                .content("""
                    {
                      "name":"Mina Student",
                      "email":"mina@example.com",
                      "passwordHash":"opaque-hash",
                      "createdAt":"2000-01-01T00:00:00"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Mina Student"))
            .andExpect(jsonPath("$.passwordHash").doesNotExist())
            .andReturn();
        LocalDateTime afterCreate = LocalDateTime.now(ZoneOffset.UTC);

        JsonNode createdUser = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String id = createdUser.get("id").asText();
        LocalDateTime createdAt = LocalDateTime.parse(createdUser.get("createdAt").asText());
        assertFalse(createdAt.isBefore(beforeCreate));
        assertFalse(createdAt.isAfter(afterCreate));

        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].email").value("mina@example.com"));

        mockMvc.perform(get("/api/users/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id))
            .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(put("/api/users/{id}", id)
                .contentType("application/json")
                .content("""
                    {"name":"Mina Updated","email":"updated@example.com","passwordHash":"new-hash"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Mina Updated"))
            .andExpect(jsonPath("$.email").value("updated@example.com"))
            .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(delete("/api/users/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value("Successfully deleted user: " + id));

        mockMvc.perform(get("/api/users/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("User not found with id: " + id));
    }
}
