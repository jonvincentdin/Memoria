package com.csit321g1.lukydizy.memoria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.csit321g1.lukydizy.memoria.entity.ReviewerNoteEntity;
import com.csit321g1.lukydizy.memoria.entity.ReviewerNoteId;

/** Add links with save/saveAll and remove them with delete/deleteAll. */
public interface ReviewerNoteRepository extends JpaRepository<ReviewerNoteEntity, ReviewerNoteId> {

    /** Lists every note link of a reviewer. */
    List<ReviewerNoteEntity> findByReviewer_Id(String reviewerId);

    /** Lists a reviewer's links whose note belongs to the given user. */
    List<ReviewerNoteEntity> findByReviewer_IdAndNote_User_Id(String reviewerId, String userId);

    /** Lists the links of one note whose reviewer belongs to the given user. */
    List<ReviewerNoteEntity> findByNote_IdAndReviewer_User_Id(String noteId, String userId);

    /** Counts reviewer links so a linked note can be protected from deletion. */
    long countByNote_Id(String noteId);
}
