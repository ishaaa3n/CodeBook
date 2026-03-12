package com.example.app.folder;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.app.note.MoveNoteRequest;
import com.example.app.note.NoteResponse;
import com.example.app.note.NoteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;
    private final NoteService noteService;

    @GetMapping
    public List<FolderResponse> getAllFolders(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return folderService.getAllFolders(userId);
    }

    @PostMapping
    public FolderResponse createFolder(Authentication authentication, @Valid @RequestBody FolderRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return folderService.createFolder(userId, request);
    }

    @DeleteMapping("/{id}")
    public void deleteFolder(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        folderService.deleteFolder(id, userId);
    }

    @PutMapping("/{id}/move")
public NoteResponse moveNote(@PathVariable Long id, 
                              @RequestBody MoveNoteRequest request,
                              Authentication authentication) {
    Long userId = (Long) authentication.getPrincipal();
    return noteService.moveNote(id, request.getFolderId(), userId);
}
}