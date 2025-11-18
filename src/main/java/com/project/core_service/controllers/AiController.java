package com.project.core_service.controllers;

import com.project.core_service.dto.GenerateQueryRequestDTO;
import com.project.core_service.services.OpenAiQueryGenerationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import jakarta.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1/ai")
@Validated
@Slf4j
public class AiController {

    private final OpenAiQueryGenerationService openAiQueryGenerationService;

    @Autowired
    public AiController(OpenAiQueryGenerationService openAiQueryGenerationService) {
        this.openAiQueryGenerationService = openAiQueryGenerationService;
    }

    /**
     * Generate MongoDB aggregation query using OpenAI with streaming response
     * 
     * @param request The query generation request containing user prompt, lookup
     *                name, and fields
     * @return SSE stream of the generated MongoDB query
     */
    @PostMapping(value = "/generate-query", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateQuery(@Valid @RequestBody GenerateQueryRequestDTO request) {
        log.info("Received query generation request for lookup: {}", request.getLookupName());

        // Create SSE emitter with 5 minute timeout
        SseEmitter emitter = new SseEmitter(300000L);

        // Handle completion and timeout
        emitter.onCompletion(() -> log.info("Query generation stream completed"));
        emitter.onTimeout(() -> {
            log.warn("Query generation stream timed out");
            emitter.complete();
        });
        emitter.onError(e -> log.error("Query generation stream error", e));

        // Generate query asynchronously
        new Thread(() -> {
            try {
                openAiQueryGenerationService.generateQueryStream(
                        request.getUserPrompt(),
                        request.getLookupName(),
                        request.getLookupFieldsUsed(),
                        emitter);
            } catch (Exception e) {
                log.error("Error in query generation thread", e);
                try {
                    emitter.send(SseEmitter.event()
                            .name("error")
                            .data("Error generating query: " + e.getMessage()));
                } catch (IOException ioException) {
                    log.error("Failed to send error event", ioException);
                }
                emitter.completeWithError(e);
            }
        }).start();

        return emitter;
    }
}
