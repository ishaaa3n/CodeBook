package com.example.app.execution;

import org.springframework.data.jpa.repository.JpaRepository;

import jakarta.transaction.Transactional;

import java.util.List;

public interface ExecutionRepository extends JpaRepository<Execution,Long> {
    List<Execution> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Execution> findByNoteIdAndUserIdOrderByCreatedAtDesc(Long noteId, Long userId);
    @Transactional
    void deleteByNoteId(Long noteId);
}