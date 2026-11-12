package com.csit321g1.lukydizy.memoria.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.csit321g1.lukydizy.memoria.dto.NoteRequest;
import com.csit321g1.lukydizy.memoria.dto.NoteResponse;
import com.csit321g1.lukydizy.memoria.service.NoteService;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService service;

    /** Creates the controller with its note service. */
    public NoteController(NoteService service) {
        this.service = service;
    }

    /** Creates a note for the authenticated user. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse create(@Valid @RequestBody NoteRequest request) {
        return service.create(request);
    }

    /** Lists the authenticated user's notes, newest first. */
    @GetMapping
    public List<NoteResponse> list() {
        return service.list();
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

    /** Deletes an owned note unless a reviewer still uses it. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) {
        service.delete(id);
    }
}
