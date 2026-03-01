package com.example.app.execution;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DockerExecutionService {

    private static final int TIMEOUT_SECONDS = 10;

    public ExecutionResult execute(String image, String code, 
                                   String input, Long executionId) {
        Path tempDir = null;
        try {
            // Step 1 - create temp directory
            tempDir = Files.createTempDirectory("exec_" + executionId);

            // Step 2 - write code to file
            String fileName = getFileName(image);
            Path codeFile = tempDir.resolve(fileName);
            Files.writeString(codeFile, code);

            // Step 3 - write input to file
            Path inputFile = tempDir.resolve("input.txt");
            Files.writeString(inputFile, input != null ? input : "");

            // Step 4 - build docker command
            String[] command = {
                "docker", "run", "--rm",
                "--memory=256m",
                "--cpus=0.5",
                "--network=none",
                "--pids-limit=50",
                "-v", tempDir.toAbsolutePath() + ":/code",
                image,
                "sh", "-c", getRunCommand(image, fileName)
            };

            // Step 5 - run with timeout
            long startTime = System.currentTimeMillis();
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectInput(inputFile.toFile());
            Process process = pb.start();

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

            // Step 6 - capture output
            String output = new String(process.getInputStream().readAllBytes());
            String error = new String(process.getErrorStream().readAllBytes());
            int exitCode = process.exitValue();

            // Step 7 - determine status
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
        } finally {
            // Step 8 - cleanup temp files
            if (tempDir != null) {
                try {
                    Files.walk(tempDir)
                         .sorted(Comparator.reverseOrder())
                         .map(Path::toFile)
                         .forEach(File::delete);
                } catch (Exception ignored) {}
            }
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
    
    // pull in background thread
    new Thread(() -> {
        images.forEach(image -> {
            try {
                System.out.println("Pulling image: " + image);
                new ProcessBuilder("docker", "pull", image)
                    .inheritIO()
                    .start()
                    .waitFor();
                System.out.println("Pulled: " + image);
            } catch (Exception e) {
                System.err.println("Failed to pull: " + image);
            }
        });
    }).start();
}

    private String getFileName(String image) {
        if (image.contains("python")) return "solution.py";
        if (image.contains("node")) return "solution.js";
        if (image.contains("openjdk")) return "Solution.java";
        if (image.contains("gcc")) return "solution.cpp";
        return "solution.sh";
    }

    private String getRunCommand(String image, String fileName) {
        if (image.contains("python")) return "python /code/" + fileName;
        if (image.contains("node")) return "node /code/" + fileName;
        if(image.contains("gcc")) 
            return "cd /code && g++ " + fileName + " -o solution && ./solution";
        if (image.contains("openjdk")) 
            return "cd /code && javac " + fileName + " && java Solution";
        return "sh /code/" + fileName;
    }
}

    
        
