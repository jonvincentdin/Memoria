package com.csit321g1.lukydizy.memoria.notes;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.csit321g1.lukydizy.memoria.notes.NoteDto.NoteRequest;
import com.csit321g1.lukydizy.memoria.notes.NoteDto.NoteResponse;
import com.csit321g1.lukydizy.memoria.notes.NoteFileExtractor.ExtractedNote;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService service;
    private final NoteFileExtractor extractor;

    /** Creates the controller with its note service and file extractor. */
    public NoteController(NoteService service, NoteFileExtractor extractor) {
        this.service = service;
        this.extractor = extractor;
    }

    /** Creates a note for the authenticated user. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse create(@Valid @RequestBody NoteRequest request) {
        return service.create(request);
    }

    /** Imports a .md, .txt, .pdf, .docx, or .pptx file as a note for the authenticated user. */
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse importNote(
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "title", required = false) String title
    ) {
        ExtractedNote extracted = extractor.extract(file, title);
        return service.createImported(extracted.title(), extracted.content(), extracted.sourceFilename());
    }

    /** Lists the authenticated user's active or archived notes, newest first. */
    @GetMapping
    public List<NoteResponse> list(@RequestParam(value = "archived", defaultValue = "false") boolean archived) {
        return service.list(archived);
    }

    /** Returns one of the authenticated user's notes. */
    @GetMapping("/{id}")
    public NoteResponse get(@PathVariable("id") String id) {
        return service.get(id);
    }

    /** Updates the title and content of an owned note. */
    @PutMapping("/{id}")
    public NoteResponse update(@PathVariable("id") String id, @Valid @RequestBody NoteRequest request) {
        return service.update(id, request);
    }

    /** Archives an owned note. */
    @PostMapping("/{id}/archive")
    public NoteResponse archive(@PathVariable("id") String id) {
        return service.archive(id);
    }

    /** Restores an archived owned note back to active notes. */
    @PostMapping("/{id}/restore")
    public NoteResponse restore(@PathVariable("id") String id) {
        return service.restore(id);
    }

    /** Deletes an owned note unless a reviewer still uses it. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) {
        service.delete(id);
    }
}
