package com.example.app.execution;

import java.time.LocalDateTime;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class ExecutionWorker {

    private final ExecutionRepository executionRepository;
    private final DockerExecutionService dockerExecutionService;

    @RabbitListener(queues = "${rabbitmq.queue.execution}")
    public void processExecution(ExecutionMessage message) {
        Long executionId = message.getExecutionId();

        log.info("Received execution request: executionId={}, language={}", 
                 executionId, message.getLanguage());

        Execution execution = executionRepository.findById(executionId)
                .orElseThrow(() -> new RuntimeException("Execution not found"));

        if (execution.getStatus() == ExecutionStatus.SUCCESS || 
            execution.getStatus() == ExecutionStatus.FAILED ||
            execution.getStatus() == ExecutionStatus.TIMEOUT) {
            log.info("Execution {} already completed, skipping", executionId);
            return;
        }

        execution.setStatus(ExecutionStatus.RUNNING);
        executionRepository.save(execution);

        log.info("Starting Docker execution for executionId={}", executionId);

        String image = getDockerImage(message.getLanguage());
        ExecutionResult result = dockerExecutionService.execute(
                image, message.getCode(), message.getInput(), executionId);

        log.info("Execution {} completed with status={} in {}ms", 
                 executionId, result.getStatus(), result.getExecutionTime());

        if (result.getStatus() == ExecutionStatus.FAILED || 
            result.getStatus() == ExecutionStatus.TIMEOUT) {
            log.warn("Execution {} did not succeed: {}", executionId, result.getError());
        }

        execution.setOutput(result.getOutput());
        execution.setError(result.getError());
        execution.setExecutionTime(result.getExecutionTime());
        execution.setStatus(result.getStatus());
        execution.setCompletedAt(LocalDateTime.now());
        executionRepository.save(execution);

        log.info("Execution {} saved to database successfully", executionId);
    }

    private String getDockerImage(String language) {
        return switch (language.toLowerCase()) {
            case "python" -> "python:3.11-slim";
            case "javascript" -> "node:18-slim";
            case "java" -> "eclipse-temurin:17-jdk-alpine";
            case "cpp", "c++" -> "gcc:latest";
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }
}