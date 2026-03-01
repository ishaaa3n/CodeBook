package com.example.app.execution;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/executions")
public class ExecutionController {


    private final ExecutionService executionService;



    // POST /api/executions/{noteId}   
    // → trigger execution for a note

    @PostMapping("{noteId}")
    public ExecutionResponse triggerExecution(@PathVariable Long noteId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return executionService.createExecution(userId, noteId);
    }

    // GET  /api/executions/{id}            
    // → get single execution status
    @GetMapping("/{id}")
    public ExecutionResponse getExecution(@PathVariable Long id, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return executionService.getExecution(id, userId);
    }
    

//     GET  /api/executions                
// → get all executions for current user

    @GetMapping
    public List<ExecutionResponse> getExecutions(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        
        return executionService.getUserExecutions(userId);
    }

// GET  /api/executions/note/{noteId}   → get all executions for a specific note
    @GetMapping("/note/{noteId}")
    public List<ExecutionResponse> getExecutionsForNote(@PathVariable Long noteId, Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return executionService.getExecutionsForNote(userId, noteId);
    }




}
