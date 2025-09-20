package com.project.core_service.controllers;

import com.project.core_service.commands.LifecycleTransitionCommand;
import com.project.core_service.services.SolutionReviewLifecycleService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/lifecycle")
public class LifecycleController {

    private final SolutionReviewLifecycleService lifecycleService;

    @Autowired
    public LifecycleController(SolutionReviewLifecycleService lifecycleService) {
        this.lifecycleService = lifecycleService;
    }

    @PostMapping("/transition")
    public ResponseEntity<String> transition(@Valid @RequestBody LifecycleTransitionCommand command) {
        lifecycleService.executeTransition(command);
        return ResponseEntity.ok("Transition successful");
    }
}
