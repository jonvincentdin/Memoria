package com.csit321g1.lukydizy.memoria.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.csit321g1.lukydizy.memoria.dto.AttemptResponse;
import com.csit321g1.lukydizy.memoria.service.QuizAttemptService;

/** Attempt history. Attempts are created by the quiz submission endpoint (BE-06). */
@RestController
@RequestMapping("/api/attempts")
public class QuizAttemptController {

    // Business logic lives in the service; it is injected through the constructor.
    private final QuizAttemptService service;

    /** Creates the controller with its attempt service. */
    public QuizAttemptController(QuizAttemptService service) {
        this.service = service;
    }

    /** Lists the current user's attempts, newest first. Add ?quizId= to filter by quiz. */
    @GetMapping
    public List<AttemptResponse> list(@RequestParam(name = "quizId", required = false) String quizId) {
        return service.list(quizId);
    }

    /** Returns one of the current user's attempts. */
    @GetMapping("/{id}")
    public AttemptResponse get(@PathVariable("id") String id) {
        return service.get(id);
    }

    /** Deletes one of the current user's attempts. */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) {
        service.delete(id);
    }
}
