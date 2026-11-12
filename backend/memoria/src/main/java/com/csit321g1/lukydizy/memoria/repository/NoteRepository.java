package com.csit321g1.lukydizy.memoria.repository;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.csit321g1.lukydizy.memoria.entity.NoteEntity;

public interface NoteRepository extends JpaRepository<NoteEntity, String> {

    /** Returns only the notes from the given IDs that belong to the given user. */
    List<NoteEntity> findByIdInAndUser_Id(Collection<String> ids, String userId);
}
