package com.example.app.execution;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionResponse {
    private Long id;
    private ExecutionStatus status;
    private String output;
    private String error;
    private Long executionTime;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Long noteId;
    private String noteTitle;
    private Long userId;
    private String userName;
}