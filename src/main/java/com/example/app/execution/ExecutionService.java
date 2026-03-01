package com.example.app.execution;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.app.note.Note;
import com.example.app.note.NoteRepository;
import com.example.app.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExecutionService {
    
    private final ExecutionRepository executionRepository;
    private final ExecutionPublisher executionPublisher;
    private final NoteRepository noteRepository;

    private ExecutionResponse toResponse(Execution execution) {
        return ExecutionResponse.builder()
            .id(execution.getId())
            .output(execution.getOutput())
            .error(execution.getError())
            .executionTime(execution.getExecutionTime())
            .status(execution.getStatus())
            .createdAt(execution.getCreatedAt())
            .completedAt(execution.getCompletedAt())
            .noteId(execution.getNote().getId())
            .noteTitle(execution.getNote().getTitle())
            .userId(execution.getUser().getId())
            .userName(execution.getUser().getName())
            .build();
    }

    public ExecutionResponse createExecution(Long userId, Long noteId) {
        Note note = noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        User user = note.getUser();
        
        Execution execution = Execution.builder()
                .user(user)
                .note(note)
                .status(ExecutionStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        Execution saved = executionRepository.save(execution);

        ExecutionMessage message = ExecutionMessage.builder()
                .executionId(saved.getId())
                .language(note.getLanguage())
                .code(note.getContent())
                .input(note.getInput())
                .build();
        executionPublisher.publish(message);
        
        return toResponse(saved);
    }

    public ExecutionResponse getExecution(Long id, Long userId) {
        Execution execution = executionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Execution not found"));
        return toResponse(execution);
    }

    public List<ExecutionResponse> getUserExecutions(Long userId) {
        return executionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ExecutionResponse> getExecutionsForNote(Long userId, Long noteId) {
        return executionRepository.findByNoteIdAndUserIdOrderByCreatedAtDesc(noteId, userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }
}