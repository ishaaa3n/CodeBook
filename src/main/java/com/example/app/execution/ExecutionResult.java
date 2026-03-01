package com.example.app.execution;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExecutionResult {
    private String output;
    private String error;
    private Long executionTime;
    private ExecutionStatus status;
}
