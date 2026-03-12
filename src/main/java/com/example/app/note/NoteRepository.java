package com.example.app.note;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserId(Long userId);
    Optional<Note> findByIdAndUserId(Long id, Long userId);
    int countByFolderIdAndUserId(Long folderId, Long userId);
    List<Note> findByFolderIdAndUserId(Long folderId, Long userId);
    List<Note> findByFolderIsNullAndUserId(Long userId);
}
