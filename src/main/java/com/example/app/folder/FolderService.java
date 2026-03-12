package com.example.app.folder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.app.note.NoteRepository;
import com.example.app.user.User;
import com.example.app.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FolderService {

    private final FolderRepository folderRepository;
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;

    public List<FolderResponse> getAllFolders(Long userId) {
        return folderRepository.findByUserId(userId).stream()
                .map(f -> FolderResponse.builder()
                        .id(f.getId())
                        .name(f.getName())
                        .createdAt(f.getCreatedAt())
                        .noteCount(noteRepository.countByFolderIdAndUserId(f.getId(), userId))
                        .build())
                .collect(Collectors.toList());
    }

    public FolderResponse createFolder(Long userId, FolderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Folder folder = Folder.builder()
                .name(request.getName())
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        Folder saved = folderRepository.save(folder);

        return FolderResponse.builder()
                .id(saved.getId())
                .name(saved.getName())
                .createdAt(saved.getCreatedAt())
                .noteCount(0)
                .build();
    }

    public void deleteFolder(Long folderId, Long userId) {
        Folder folder = folderRepository.findById(folderId)
                .orElseThrow(() -> new RuntimeException("Folder not found"));
        if (!folder.getUser().getId().equals(userId))
            throw new RuntimeException("Unauthorized");
        folderRepository.delete(folder);
    }
}