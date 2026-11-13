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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.csit321g1.lukydizy.memoria.dto.ReviewerDto.ReviewerRequest;
import com.csit321g1.lukydizy.memoria.dto.ReviewerDto.ReviewerResponse;
import com.csit321g1.lukydizy.memoria.service.ReviewerService;

@RestController
@RequestMapping("/api/reviewers")
public class ReviewerController {

    // Business logic lives in the service; it is injected through the constructor.
    private final ReviewerService service;

    /** Creates the controller with its reviewer service. */
    public ReviewerController(ReviewerService service) {
        this.service = service;
    }

    /** Creates a reviewer linked to one or more of the user's notes. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewerResponse create(@Valid @RequestBody ReviewerRequest request) {
        return service.create(request);
    }

    /** Lists the current user's reviewers, newest first. Add ?noteId= to list only reviewers made from that note. */
    @GetMapping
    public List<ReviewerResponse> list(@RequestParam(name = "noteId", required = false) String noteId) {
        return service.list(noteId);
    }

    /** Returns one reviewer with its linked notes. */
    @GetMapping("/{id}")
    public ReviewerResponse get(@PathVariable("id") String id) {
        return service.get(id);
    }

    /** Updates a reviewer and replaces its linked notes with the submitted list. */
    @PutMapping("/{id}")
    public ReviewerResponse update(@PathVariable("id") String id, @Valid @RequestBody ReviewerRequest request) {
        return service.update(id, request);
    }

    /** Deletes a reviewer unless a quiz uses it. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) {
        service.delete(id);
    }
}
