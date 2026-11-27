package com.csit321g1.lukydizy.memoria.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFGroupShape;
import org.apache.poi.xslf.usermodel.XSLFNotes;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTableRow;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.csit321g1.lukydizy.memoria.exception.InvalidRequestException;

/**
 * Validates and extracts plain text from supported note import formats (.md, .txt, .pdf, .docx, .pptx).
 */
@Component
public class NoteFileExtractor {

    public static final long MAX_FILE_BYTES = 5L * 1024 * 1024;
    public static final int MAX_TITLE_LENGTH = 150;
    public static final int MAX_CONTENT_LENGTH = 20000;
    private static final int MAX_FILENAME_LENGTH = 255;

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("md", "txt", "pdf", "docx", "pptx");

    public record ExtractedNote(String title, String content, String sourceFilename) {
    }

    /** Validates the uploaded file, extracts its text, and resolves a safe title and filename. */
    public ExtractedNote extract(MultipartFile file, String requestedTitle) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("Please choose a non-empty file to import.");
        }
        if (file.getSize() > MAX_FILE_BYTES) {
            throw new MaxUploadSizeExceededException(MAX_FILE_BYTES);
        }

        String filename = sanitizeFilename(file.getOriginalFilename());
        String extension = extractExtension(filename);
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new InvalidRequestException(
                "Unsupported file type. Supported formats are .md, .txt, .pdf, .docx, and .pptx."
            );
        }

        byte[] bytes = readBytes(file);
        if (bytes.length == 0) {
            throw new InvalidRequestException("The uploaded file is empty.");
        }
        if (bytes.length > MAX_FILE_BYTES) {
            throw new MaxUploadSizeExceededException(MAX_FILE_BYTES);
        }

        String rawText = switch (extension) {
            case "md", "txt" -> extractUtf8Text(bytes);
            case "pdf" -> extractPdf(bytes);
            case "docx" -> extractDocx(bytes);
            case "pptx" -> extractPptx(bytes);
            default -> throw new InvalidRequestException(
                "Unsupported file type. Supported formats are .md, .txt, .pdf, .docx, and .pptx."
            );
        };

        String content = normalizeText(rawText);
        if (content.isBlank()) {
            throw new InvalidRequestException("No readable text could be extracted from this file.");
        }
        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new InvalidRequestException(
                "Extracted text exceeds the " + MAX_CONTENT_LENGTH + "-character note limit ("
                    + content.length() + " characters)."
            );
        }

        String title = resolveTitle(requestedTitle, filename);
        return new ExtractedNote(title, content, filename);
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException exception) {
            throw new InvalidRequestException("Could not read the uploaded file.");
        }
    }

    private String extractUtf8Text(byte[] bytes) {
        for (byte b : bytes) {
            if (b == 0) {
                throw new InvalidRequestException("The file does not appear to be valid plain text.");
            }
        }
        CharsetDecoder decoder = StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT);
        try {
            String decoded = decoder.decode(ByteBuffer.wrap(bytes)).toString();
            if (!decoded.isEmpty() && decoded.charAt(0) == '\uFEFF') {
                return decoded.substring(1);
            }
            return decoded;
        } catch (CharacterCodingException exception) {
            throw new InvalidRequestException("Text and Markdown files must be valid UTF-8.");
        }
    }

    private String extractPdf(byte[] bytes) {
        if (!hasPrefix(bytes, (byte) '%', (byte) 'P', (byte) 'D', (byte) 'F')) {
            throw new InvalidRequestException("Could not read the PDF file. Make sure it is a valid, uncorrupted PDF.");
        }
        try (PDDocument document = Loader.loadPDF(bytes)) {
            return new PDFTextStripper().getText(document);
        } catch (InvalidPasswordException exception) {
            throw new InvalidRequestException("Password-protected PDF files cannot be imported.");
        } catch (IOException | RuntimeException exception) {
            throw new InvalidRequestException("Could not read the PDF file. Make sure it is a valid, uncorrupted PDF.");
        }
    }

    private String extractDocx(byte[] bytes) {
        if (!hasZipSignature(bytes)) {
            throw new InvalidRequestException("Could not read the DOCX file. Make sure it is a valid Word document.");
        }
        try (
            XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes));
            XWPFWordExtractor extractor = new XWPFWordExtractor(document)
        ) {
            return extractor.getText();
        } catch (EncryptedDocumentException exception) {
            throw new InvalidRequestException("Password-protected DOCX files cannot be imported.");
        } catch (IOException | RuntimeException exception) {
            throw new InvalidRequestException("Could not read the DOCX file. Make sure it is a valid Word document.");
        }
    }

    private String extractPptx(byte[] bytes) {
        if (!hasZipSignature(bytes)) {
            throw new InvalidRequestException("Could not read the PPTX file. Make sure it is a valid PowerPoint file.");
        }
        try (XMLSlideShow presentation = new XMLSlideShow(new ByteArrayInputStream(bytes))) {
            List<String> slideBlocks = new ArrayList<>();
            for (XSLFSlide slide : presentation.getSlides()) {
                List<String> parts = new ArrayList<>();
                collectShapeText(slide.getShapes(), parts);
                XSLFNotes notes = slide.getNotes();
                if (notes != null) {
                    collectShapeText(notes.getShapes(), parts);
                }
                if (!parts.isEmpty()) {
                    slideBlocks.add(String.join("\n", parts));
                }
            }
            return String.join("\n\n", slideBlocks);
        } catch (EncryptedDocumentException exception) {
            throw new InvalidRequestException("Password-protected PPTX files cannot be imported.");
        } catch (IOException | RuntimeException exception) {
            throw new InvalidRequestException("Could not read the PPTX file. Make sure it is a valid PowerPoint file.");
        }
    }

    private void collectShapeText(List<XSLFShape> shapes, List<String> parts) {
        for (XSLFShape shape : shapes) {
            if (shape instanceof XSLFTextShape textShape) {
                String text = textShape.getText();
                if (text != null && !text.isBlank()) {
                    parts.add(text.trim());
                }
            } else if (shape instanceof XSLFTable table) {
                for (XSLFTableRow row : table.getRows()) {
                    List<String> cells = new ArrayList<>();
                    for (XSLFTableCell cell : row.getCells()) {
                        String text = cell.getText();
                        if (text != null && !text.isBlank()) {
                            cells.add(text.trim());
                        }
                    }
                    if (!cells.isEmpty()) {
                        parts.add(String.join(" | ", cells));
                    }
                }
            } else if (shape instanceof XSLFGroupShape group) {
                collectShapeText(group.getShapes(), parts);
            }
        }
    }

    private boolean hasZipSignature(byte[] bytes) {
        return hasPrefix(bytes, (byte) 'P', (byte) 'K', (byte) 0x03, (byte) 0x04);
    }

    private boolean hasPrefix(byte[] bytes, byte... prefix) {
        if (bytes.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (bytes[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private String sanitizeFilename(String original) {
        if (original == null || original.isBlank()) {
            throw new InvalidRequestException("Uploaded file must have a valid filename.");
        }
        String name = original.replace('\\', '/');
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }
        name = name.replaceAll("[\\p{Cntrl}]", "").trim();
        if (name.isEmpty()) {
            throw new InvalidRequestException("Uploaded file must have a valid filename.");
        }
        if (name.length() > MAX_FILENAME_LENGTH) {
            name = name.substring(name.length() - MAX_FILENAME_LENGTH);
        }
        return name;
    }

    private String extractExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot <= 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private String normalizeText(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("\r\n", "\n").replace('\r', '\n').trim();
    }

    private String resolveTitle(String requestedTitle, String filename) {
        if (requestedTitle != null && !requestedTitle.isBlank()) {
            String trimmed = requestedTitle.trim();
            if (trimmed.length() > MAX_TITLE_LENGTH) {
                throw new InvalidRequestException("Title must be " + MAX_TITLE_LENGTH + " characters or fewer.");
            }
            return trimmed;
        }
        int dot = filename.lastIndexOf('.');
        String stem = (dot > 0 ? filename.substring(0, dot) : filename).trim();
        if (stem.isEmpty()) {
            return "Imported note";
        }
        return stem.length() > MAX_TITLE_LENGTH ? stem.substring(0, MAX_TITLE_LENGTH) : stem;
    }
}
