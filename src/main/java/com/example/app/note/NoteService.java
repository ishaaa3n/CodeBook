package com.example.app.note;

import java.time.LocalDateTime;
import java.util.List;
import com.example.app.config.ValidationException;
import com.example.app.execution.ExecutionRepository;
import com.example.app.folder.Folder;
import com.example.app.folder.FolderRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.example.app.user.User;
import com.example.app.user.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final FolderRepository folderRepository;
    private final ExecutionRepository executionRepository;

    private static final List<String> SUPPORTED_LANGUAGES = 
    List.of("python", "javascript", "java", "cpp");

    private void validateLanguage(String language) {
    if (!SUPPORTED_LANGUAGES.contains(language.toLowerCase())) {
        throw new ValidationException("Unsupported language: " + language + 
            ". Supported: " + SUPPORTED_LANGUAGES);
    }
}




    private NoteResponse toResponse(Note note) {
    return NoteResponse.builder()
            .id(note.getId())
            .title(note.getTitle())
            .content(note.getContent())
            .language(note.getLanguage())
            .input(note.getInput())
            .createdAt(note.getCreatedAt())
            .updatedAt(note.getUpdatedAt())
            .userId(note.getUser().getId())
            .userName(note.getUser().getName())
            .folderId(note.getFolder() != null ? note.getFolder().getId() : null)
            .folderName(note.getFolder() != null ? note.getFolder().getName() : null)
            .build();
}

    // @Cacheable(value = "notes", key = "#userId")
    public List<NoteResponse> getAllNotes(Long userId) {
        return noteRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }

    // @Cacheable(value = "note", key = "#id + '-' + #userId")
    public NoteResponse getNoteById(Long id, Long userId) {
        Note note = noteRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        return toResponse(note);
    }

    @CacheEvict(value = "notes", key = "#userId")
    public NoteResponse createNote(Long userId, NoteRequest request) {
        validateLanguage(request.getLanguage());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Note note = Note.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .language(request.getLanguage())
                .input(request.getInput())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .user(user)
                .build();
        Note savedNote = noteRepository.save(note);
        return toResponse(savedNote);
    }

    @Caching(evict = {
        @CacheEvict(value = "notes", key = "#userId"),
        @CacheEvict(value = "note", key = "#id + '-' + #userId")
    })
    public NoteResponse updateNote(Long id, Long userId, NoteRequest request) {
        validateLanguage(request.getLanguage());
        Note existingNote = noteRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        existingNote.setTitle(request.getTitle());
        existingNote.setContent(request.getContent());
        existingNote.setLanguage(request.getLanguage());
        existingNote.setInput(request.getInput());
        existingNote.setUpdatedAt(LocalDateTime.now());
        Note updatedNote = noteRepository.save(existingNote);
        return toResponse(updatedNote);
    }
    @Caching(evict = {
    @CacheEvict(value = "notes", key = "#userId"),
    @CacheEvict(value = "note", key = "#noteId + '-' + #userId")
})
public NoteResponse moveNote(Long noteId, Long folderId, Long userId) {
    Note note = noteRepository.findByIdAndUserId(noteId, userId)
            .orElseThrow(() -> new RuntimeException("Note not found"));

    if (folderId != null) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));
        note.setFolder(folder);
    } else {
        note.setFolder(null);
    }

    note.setUpdatedAt(LocalDateTime.now());
    Note saved = noteRepository.save(note);
    return toResponse(saved);
}
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "notes", key = "#userId"),
        @CacheEvict(value = "note", key = "#id + '-' + #userId")
    })
    public void deleteNoteById(Long id, Long userId) {
    Note existingNote = noteRepository.findByIdAndUserId(id, userId)
            .orElseThrow(() -> new RuntimeException("Note not found"));
    executionRepository.deleteByNoteId(id);  // delete executions first
    noteRepository.delete(existingNote);
}


}