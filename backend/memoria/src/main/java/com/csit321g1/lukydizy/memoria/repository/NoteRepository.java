package com.csit321g1.lukydizy.memoria.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.csit321g1.lukydizy.memoria.entity.NoteEntity;

public interface NoteRepository extends JpaRepository<NoteEntity, String> {

    /** Finds a note only if it belongs to the given user. */
    Optional<NoteEntity> findByIdAndUser_Id(String id, String userId);

    /** Lists a user's notes, newest first. */
    List<NoteEntity> findByUser_IdOrderByCreatedAtDesc(String userId);

    /** Returns only the notes from the given IDs that belong to the given user. */
    List<NoteEntity> findByIdInAndUser_Id(Collection<String> ids, String userId);
}
