package com.csit321g1.lukydizy.memoria.notes;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.csit321g1.lukydizy.memoria.users.UserEntity;
import com.csit321g1.lukydizy.memoria.users.UserRepository;

import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NoteImportTests {

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

    private String ownerId;
    private String otherId;

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

    @Test
    void importsTxtAndPreservesFilenameAndOwnership() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "chapter-1.txt", "text/plain", "Cells are the basic unit of life.\r\nMitosis divides nuclei."
                .getBytes(StandardCharsets.UTF_8)
        );

        MvcResult result = mockMvc.perform(multipart("/api/notes/import").file(file)
                .with(user(ownerId)).with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("chapter-1"))
            .andExpect(jsonPath("$.content").value("Cells are the basic unit of life.\nMitosis divides nuclei."))
            .andExpect(jsonPath("$.sourceType").value("IMPORT"))
            .andExpect(jsonPath("$.sourceFilename").value("chapter-1.txt"))
            .andExpect(jsonPath("$.archived").value(false))
            .andReturn();

        String id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
        assertEquals(ownerId, notes.findById(id).orElseThrow().getUser().getId());

        mockMvc.perform(get("/api/notes/{id}", id).with(user(otherId)))
            .andExpect(status().isNotFound());
    }

    @Test
    void importsMarkdownWithCustomTitle() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "C:\\fake\\path\\lecture-notes.md", "text/markdown",
            "# Algorithms\n- Binary search is O(log n)".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/notes/import").file(file)
                .param("title", "Algorithm Summary")
                .with(user(ownerId)).with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Algorithm Summary"))
            .andExpect(jsonPath("$.sourceFilename").value("lecture-notes.md"))
            .andExpect(jsonPath("$.content").value("# Algorithms\n- Binary search is O(log n)"));
    }

    @Test
    void importsValidPdf() throws Exception {
        byte[] pdfBytes = createPdf("Photosynthesis converts light energy into chemical energy.");
        MockMultipartFile file = new MockMultipartFile("file", "biology.pdf", "application/pdf", pdfBytes);

        mockMvc.perform(multipart("/api/notes/import").file(file)
                .with(user(ownerId)).with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("biology"))
            .andExpect(jsonPath("$.sourceType").value("IMPORT"))
            .andExpect(jsonPath("$.sourceFilename").value("biology.pdf"))
            .andExpect(jsonPath("$.content", containsString("Photosynthesis converts light energy")));
    }

    @Test
    void importsValidDocx() throws Exception {
        byte[] docxBytes = createDocx("Third normal form eliminates transitive dependencies.");
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "database-review.docx",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            docxBytes
        );

        mockMvc.perform(multipart("/api/notes/import").file(file)
                .with(user(ownerId)).with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("database-review"))
            .andExpect(jsonPath("$.sourceFilename").value("database-review.docx"))
            .andExpect(jsonPath("$.content", containsString("Third normal form eliminates transitive dependencies.")));
    }

    @Test
    void importsValidPptx() throws Exception {
        byte[] pptxBytes = createPptx("Process Scheduling", "Round robin uses a fixed time quantum.");
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "os-slides.pptx",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            pptxBytes
        );

        mockMvc.perform(multipart("/api/notes/import").file(file)
                .with(user(ownerId)).with(csrf()))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("os-slides"))
            .andExpect(jsonPath("$.sourceFilename").value("os-slides.pptx"))
            .andExpect(jsonPath("$.content", containsString("Process Scheduling")))
            .andExpect(jsonPath("$.content", containsString("Round robin uses a fixed time quantum.")));
    }

    @Test
    void rejectsUnsupportedEmptyMalformedAndOversizedFiles() throws Exception {
        MockMultipartFile unsupported = new MockMultipartFile(
            "file", "image.png", "image/png", "not-a-note".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/notes/import").file(unsupported).with(user(ownerId)).with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("Unsupported file type")));

        MockMultipartFile empty = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);
        mockMvc.perform(multipart("/api/notes/import").file(empty).with(user(ownerId)).with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("non-empty")));

        MockMultipartFile blankText = new MockMultipartFile(
            "file", "blank.md", "text/markdown", "   \n\t  ".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/notes/import").file(blankText).with(user(ownerId)).with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("No readable text")));

        MockMultipartFile corruptPdf = new MockMultipartFile(
            "file", "broken.pdf", "application/pdf", "not actually a pdf".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/notes/import").file(corruptPdf).with(user(ownerId)).with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("Could not read the PDF")));

        MockMultipartFile corruptDocx = new MockMultipartFile(
            "file", "broken.docx", "application/octet-stream", "not a zip".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/notes/import").file(corruptDocx).with(user(ownerId)).with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("Could not read the DOCX")));

        MockMultipartFile corruptPptx = new MockMultipartFile(
            "file", "broken.pptx", "application/octet-stream", "not a zip".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/notes/import").file(corruptPptx).with(user(ownerId)).with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("Could not read the PPTX")));

        MockMultipartFile oversized = new MockMultipartFile(
            "file", "huge.txt", "text/plain", new byte[(5 * 1024 * 1024) + 1]);
        mockMvc.perform(multipart("/api/notes/import").file(oversized).with(user(ownerId)).with(csrf()))
            .andExpect(status().is(413))
            .andExpect(jsonPath("$.error", containsString("5 MB")));

        MockMultipartFile tooLongContent = new MockMultipartFile(
            "file", "long.txt", "text/plain", "A".repeat(20001).getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/notes/import").file(tooLongContent).with(user(ownerId)).with(csrf()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error", containsString("20000-character")));
    }

    @Test
    void importRequiresLogin() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file", "note.txt", "text/plain", "Hello".getBytes(StandardCharsets.UTF_8));
        mockMvc.perform(multipart("/api/notes/import").file(file).with(csrf()))
            .andExpect(status().isUnauthorized());
    }

    private byte[] createPdf(String text) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage();
            document.addPage(page);
            try (PDPageContentStream stream = new PDPageContentStream(document, page)) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(50, 700);
                stream.showText(text);
                stream.endText();
            }
            document.save(out);
            return out.toByteArray();
        }
    }

    private byte[] createDocx(String text) throws Exception {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.createParagraph().createRun().setText(text);
            document.write(out);
            return out.toByteArray();
        }
    }

    private byte[] createPptx(String heading, String body) throws Exception {
        try (XMLSlideShow presentation = new XMLSlideShow(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XSLFSlide slide = presentation.createSlide();
            XSLFTextBox box1 = slide.createTextBox();
            box1.setText(heading);
            XSLFTextBox box2 = slide.createTextBox();
            box2.setText(body);
            presentation.write(out);
            return out.toByteArray();
        }
    }
}
