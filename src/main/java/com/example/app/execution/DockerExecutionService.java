package com.example.app.execution;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class DockerExecutionService {

    private static final int TIMEOUT_SECONDS = 10;

    public ExecutionResult execute(String image, String code,
                                   String input, Long executionId) {
        try {
            // Build docker command — no volume mount, code passed inline
            String[] command = {
                "docker", "run", "--rm",
                "--memory=256m",
                "--cpus=0.5",
                "--network=none",
                "--pids-limit=50",
                image,
                "sh", "-c", getRunCommand(image, code)
            };

            long startTime = System.currentTimeMillis();
            ProcessBuilder pb = new ProcessBuilder(command);
            Process process = pb.start();

            // Write input to process stdin
            if (input != null && !input.isEmpty()) {
                process.getOutputStream().write(input.getBytes());
            }
            process.getOutputStream().close();

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            long executionTime = System.currentTimeMillis() - startTime;

            if (!finished) {
                process.destroyForcibly();
                return ExecutionResult.builder()
                        .status(ExecutionStatus.TIMEOUT)
                        .error("Execution timed out after " + TIMEOUT_SECONDS + " seconds")
                        .executionTime(executionTime)
                        .build();
            }

            String output = new String(process.getInputStream().readAllBytes());
            String error = new String(process.getErrorStream().readAllBytes());
            int exitCode = process.exitValue();

            ExecutionStatus status = exitCode == 0 ?
                ExecutionStatus.SUCCESS : ExecutionStatus.FAILED;

            return ExecutionResult.builder()
                    .output(output)
                    .error(error)
                    .status(status)
                    .executionTime(executionTime)
                    .build();

        } catch (Exception e) {
            return ExecutionResult.builder()
                    .status(ExecutionStatus.SYSTEM_ERROR)
                    .error(e.getMessage())
                    .build();
        }
    }

    @PostConstruct
    public void pullImages() {
        List<String> images = List.of(
            "python:3.11-slim",
            "node:18-slim",
            "eclipse-temurin:17-jdk-alpine",
            "gcc:latest"
        );

        new Thread(() -> {
            images.forEach(image -> {
                try {
                    log.info("Pulling image: {}", image);
                    new ProcessBuilder("docker", "pull", image)
                        .inheritIO()
                        .start()
                        .waitFor();
                    log.info("Pulled: {}", image);
                } catch (Exception e) {
                    log.error("Failed to pull image {}: {}", image, e.getMessage());
                }
            });
        }).start();
    }

    private String getRunCommand(String image, String code) {
        String escaped = code.replace("'", "'\"'\"'");
        if (image.contains("python"))
            return "python -c '" + escaped + "'";
        if (image.contains("node"))
            return "node -e '" + escaped + "'";
        if (image.contains("gcc"))
            return "echo '" + escaped + "' > /tmp/s.cpp && g++ /tmp/s.cpp -o /tmp/s && /tmp/s";
        if (image.contains("temurin") || image.contains("openjdk"))
            return "echo '" + escaped + "' > /tmp/Solution.java && cd /tmp && javac Solution.java && java Solution";
        return "sh -c '" + escaped + "'";
    }
}