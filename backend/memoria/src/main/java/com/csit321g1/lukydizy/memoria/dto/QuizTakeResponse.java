package com.csit321g1.lukydizy.memoria.dto;

import java.util.List;

/** Student-facing quiz view without correct-answer keys. */
public record QuizTakeResponse(String title, List<Question> questions) {

    /** A question and its choices, with no correct-answer metadata. */
    public record Question(String text, List<String> choices) {
    }
}
