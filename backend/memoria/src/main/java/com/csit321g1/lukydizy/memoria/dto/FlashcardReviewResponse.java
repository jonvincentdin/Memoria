package com.csit321g1.lukydizy.memoria.dto;

/** Validated study result returned without scheduling or review-history persistence. */
public record FlashcardReviewResponse(
    String flashcardId,
    int rating
) {
}
