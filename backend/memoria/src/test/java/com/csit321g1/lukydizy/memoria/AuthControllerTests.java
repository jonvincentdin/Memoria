package com.csit321g1.lukydizy.memoria;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void clearUsers() {
        jdbc.update("DELETE FROM users");
    }

    @Test
    void registrationValidatesInputNormalizesEmailAndNeverReturnsPasswordHash() throws Exception {
        CsrfState csrf = getCsrf(null);
        mockMvc.perform(post("/api/auth/register")
                .session(csrf.session())
                .header(csrf.headerName(), csrf.token())
                .contentType("application/json")
                .content("""
                    {"name":"Mina Student","email":"mina@example.com","password":"short"}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Password must be at least 8 characters."));

        mockMvc.perform(post("/api/auth/register")
                .session(csrf.session())
                .header(csrf.headerName(), csrf.token())
                .contentType("application/json")
                .content("""
                    {"name":"Mina Student","email":"MINA@Example.com","password":"a-safe-password"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("mina@example.com"))
            .andExpect(jsonPath("$.name").value("Mina Student"))
            .andExpect(jsonPath("$.passwordHash").doesNotExist());

        String storedHash = jdbc.queryForObject(
            "SELECT password_hash FROM users WHERE email = ?",
            String.class,
            "mina@example.com"
        );
        org.junit.jupiter.api.Assertions.assertNotEquals("a-safe-password", storedHash);
        org.junit.jupiter.api.Assertions.assertTrue(storedHash.startsWith("$2"));

        CsrfState refreshedCsrf = getCsrf(csrf.session());
        mockMvc.perform(post("/api/auth/register")
                .session(refreshedCsrf.session())
                .header(refreshedCsrf.headerName(), refreshedCsrf.token())
                .contentType("application/json")
                .content("""
                    {"name":"Another Student","email":"mina@example.com","password":"another-password"}
                    """))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("An account with this email already exists."));

        org.junit.jupiter.api.Assertions.assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class));
    }

    @Test
    void sessionsProtectMeAndLogoutInvalidatesTheSession() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Unauthorized"));

        CsrfState registrationCsrf = getCsrf(null);
        mockMvc.perform(post("/api/auth/register")
                .session(registrationCsrf.session())
                .header(registrationCsrf.headerName(), registrationCsrf.token())
                .contentType("application/json")
                .content("""
                    {"name":"Mina Student","email":"mina@example.com","password":"a-safe-password"}
                    """))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/auth/me").session(registrationCsrf.session()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("mina@example.com"))
            .andExpect(jsonPath("$.passwordHash").doesNotExist());

        CsrfState logoutCsrf = getCsrf(registrationCsrf.session());
        mockMvc.perform(post("/api/auth/logout")
                .session(logoutCsrf.session())
                .header(logoutCsrf.headerName(), logoutCsrf.token()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Logged out"));

        mockMvc.perform(get("/api/auth/me").session(registrationCsrf.session()))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void loginUsesCaseInsensitiveEmailAndKeepsBadCredentialsGeneric() throws Exception {
        CsrfState registrationCsrf = getCsrf(null);
        mockMvc.perform(post("/api/auth/register")
                .session(registrationCsrf.session())
                .header(registrationCsrf.headerName(), registrationCsrf.token())
                .contentType("application/json")
                .content("""
                    {"name":"Mina Student","email":"mina@example.com","password":"a-safe-password"}
                    """))
            .andExpect(status().isCreated());

        CsrfState loginCsrf = getCsrf(null);
        mockMvc.perform(post("/api/auth/login")
                .session(loginCsrf.session())
                .header(loginCsrf.headerName(), loginCsrf.token())
                .contentType("application/json")
                .content("""
                    {"email":"MINA@EXAMPLE.COM","password":"a-safe-password"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("mina@example.com"));

        CsrfState badLoginCsrf = getCsrf(null);
        mockMvc.perform(post("/api/auth/login")
                .session(badLoginCsrf.session())
                .header(badLoginCsrf.headerName(), badLoginCsrf.token())
                .contentType("application/json")
                .content("""
                    {"email":"mina@example.com","password":"wrong-password"}
                    """))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                .contentType("application/json")
                .content("""
                    {"email":"mina@example.com","password":"wrong-password"}
                    """))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    private CsrfState getCsrf(MockHttpSession session) throws Exception {
        MvcResult result = mockMvc.perform(session == null
                ? get("/api/auth/csrf")
                : get("/api/auth/csrf").session(session))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        MockHttpSession csrfSession = (MockHttpSession) result.getRequest().getSession(false);
        return new CsrfState(csrfSession, response.get("headerName").asText(), response.get("token").asText());
    }

    private record CsrfState(MockHttpSession session, String headerName, String token) {
    }
}
