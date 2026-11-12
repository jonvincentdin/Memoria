package com.csit321g1.lukydizy.memoria.dto;

import java.time.LocalDateTime;
import java.util.List;

/** Reviewer returned to the frontend, including its linked notes. */
public record ReviewerResponse(
    String id,
    String title,
    String style,
    String content,
    LocalDateTime createdAt,
    List<LinkedNote> notes
) {

    /** A note linked to the reviewer through ReviewerNote. */
    public record LinkedNote(String id, String title) {
    }
}
