package com.example.app.note;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;

    @GetMapping
        public List<NoteResponse> getAllNotes(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return noteService.getAllNotes(userId);
    }
    @GetMapping("/{id}")
    public NoteResponse getNoteByIdNote(Authentication authentication , @PathVariable Long id){
        Long userId = (Long)authentication.getPrincipal();
        return noteService.getNoteById(id, userId);
    }
    @PostMapping
    public NoteResponse createNote(Authentication authentication, @RequestBody NoteRequest request){
        Long userId = (Long)authentication.getPrincipal();
        return noteService.createNote(userId, request);
    }
    @PutMapping("/{id}")
    public NoteResponse updateNote(@PathVariable Long id, Authentication authentication, @RequestBody NoteRequest request){
        Long userId = (Long)authentication.getPrincipal();
        return noteService.updateNote(id, userId, request);
    }
    @DeleteMapping("/{id}")
    public void deleteNoteById(@PathVariable Long id, Authentication authentication){   
        Long userId = (Long)authentication.getPrincipal();
        noteService.deleteNoteById(id, userId);
    }



    
}
