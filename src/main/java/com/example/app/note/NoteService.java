package com.example.app.note;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.app.user.User;
import com.example.app.user.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    
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
            .build();
}

    public List<NoteResponse>getAllNotes(Long userId){
        return noteRepository.findByUserId(userId).stream().map(this::toResponse).toList();
    }
    public NoteResponse getNoteById(Long id, Long userId) {
        Note note = noteRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        return toResponse(note);
    }
    // createNote(Note note) → creates a new note
    //service methods to return NoteResponse
    public NoteResponse createNote(Long userId,NoteRequest request){
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
    // updateNote(Note note) → updates an existing note
    public NoteResponse updateNote(Long id, Long userId, NoteRequest request){
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
    // deleteNoteById(Long id) → deletes a note by its ID
    public void deleteNoteById(Long id, Long userId){
        Note existingNote = noteRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Note not found"));
        noteRepository.delete(existingNote);
    }


}
