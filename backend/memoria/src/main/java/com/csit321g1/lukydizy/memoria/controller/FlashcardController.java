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

import com.csit321g1.lukydizy.memoria.dto.FlashcardRequest;
import com.csit321g1.lukydizy.memoria.dto.FlashcardResponse;
import com.csit321g1.lukydizy.memoria.dto.FlashcardReviewRequest;
import com.csit321g1.lukydizy.memoria.dto.FlashcardReviewResponse;
import com.csit321g1.lukydizy.memoria.service.FlashcardService;

@RestController
@RequestMapping("/api/flashcards")
public class FlashcardController {

    // Business logic lives in the service; it is injected through the constructor.
    private final FlashcardService service;

    /** Creates the controller with its flashcard service. */
    public FlashcardController(FlashcardService service) {
        this.service = service;
    }

    /** Creates a flashcard for one of the logged-in user's reviewers. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FlashcardResponse create(@Valid @RequestBody FlashcardRequest request) {
        return service.create(request);
    }

    /** Lists the current user's flashcards; add ?reviewerId= to filter by reviewer. */
    @GetMapping
    public List<FlashcardResponse> list(
        @RequestParam(name = "reviewerId", required = false) String reviewerId
    ) {
        return service.list(reviewerId);
    }

    /** Returns one flashcard owned by the current user. */
    @GetMapping("/{id}")
    public FlashcardResponse get(@PathVariable("id") String id) {
        return service.get(id);
    }

    /** Updates an owned flashcard and its reviewer link. */
    @PutMapping("/{id}")
    public FlashcardResponse update(
        @PathVariable("id") String id,
        @Valid @RequestBody FlashcardRequest request
    ) {
        return service.update(id, request);
    }

    /** Validates and returns the study rating for an owned flashcard. */
    @PostMapping("/{id}/review")
    public FlashcardReviewResponse review(
        @PathVariable("id") String id,
        @Valid @RequestBody FlashcardReviewRequest request
    ) {
        return service.review(id, request);
    }

    /** Deletes a flashcard owned by the current user. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) {
        service.delete(id);
    }
}
