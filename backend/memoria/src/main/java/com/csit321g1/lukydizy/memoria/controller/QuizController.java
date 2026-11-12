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

import com.csit321g1.lukydizy.memoria.dto.QuizRequest;
import com.csit321g1.lukydizy.memoria.dto.QuizResponse;
import com.csit321g1.lukydizy.memoria.service.QuizService;

/** Quiz authoring endpoints for the owner. Login and CSRF are enforced by SecurityConfig. */
@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService service;

    public QuizController(QuizService service) {
        this.service = service;
    }

    /** POST /api/quizzes: create a quiz with its questions and reviewer links. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResponse create(@Valid @RequestBody QuizRequest request) {
        return service.create(request);
    }

    /** GET /api/quizzes: list the student's quizzes. */
    @GetMapping
    public List<QuizResponse> list() {
        return service.list();
    }

    /** GET /api/quizzes/{id}: one quiz (404 if not yours). */
    @GetMapping("/{id}")
    public QuizResponse get(@PathVariable("id") String id) {
        return service.get(id);
    }

    /** PUT /api/quizzes/{id}: update title, questions, and reviewers. */
    @PutMapping("/{id}")
    public QuizResponse update(@PathVariable("id") String id, @Valid @RequestBody QuizRequest request) {
        return service.update(id, request);
    }

    /** DELETE /api/quizzes/{id}: delete a quiz without attempts. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) {
        service.delete(id);
    }
}
