package com.example.app.execution;

import java.time.LocalDateTime;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ExecutionWorker {

    private final ExecutionRepository executionRepository;
    private final DockerExecutionService dockerExecutionService;

    @RabbitListener(queues = "${rabbitmq.queue.execution}")
public void processExecution(ExecutionMessage message) {
    Long executionId = message.getExecutionId();
    
    Execution execution = executionRepository.findById(executionId)
            .orElseThrow(() -> new RuntimeException("Execution not found"));
    
    if (execution.getStatus() == ExecutionStatus.SUCCESS || 
        execution.getStatus() == ExecutionStatus.FAILED ||
        execution.getStatus() == ExecutionStatus.TIMEOUT) {
        System.out.println("Execution " + executionId + " already completed, skipping.");
        return;
    }

    execution.setStatus(ExecutionStatus.RUNNING);
    executionRepository.save(execution);

    // ADD THESE LINES HERE INSIDE THE METHOD
    String image = getDockerImage(message.getLanguage());
    ExecutionResult result = dockerExecutionService.execute(
            image, message.getCode(), message.getInput(), executionId);
    
    execution.setOutput(result.getOutput());
    execution.setError(result.getError());
    execution.setExecutionTime(result.getExecutionTime());
    execution.setStatus(result.getStatus());
    execution.setCompletedAt(LocalDateTime.now());
    executionRepository.save(execution);
}

    private String getDockerImage(String language) {
        return switch (language.toLowerCase()) {
            case "python" -> "python:3.9-alpine";
            case "javascript" -> "node:14-alpine";
            case "java" -> "eclipse-temurin:17-jdk-alpine";
            case "cpp", "c++" -> "gcc:latest";
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }
}