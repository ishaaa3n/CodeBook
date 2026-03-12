package com.example.app.folder;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FolderResponse {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private int noteCount;
}