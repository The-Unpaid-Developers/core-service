package com.project.core_service.client;

import com.project.core_service.dto.ChatbotTranslateRequestDTO;
import com.project.core_service.dto.ChatbotTranslateResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Client for communicating with the Chatbot Service API.
 *
 * This client provides methods to interact with the chatbot service's REST endpoints,
 * specifically for translating natural language queries into MongoDB aggregation pipelines
 * and executing them. It uses Spring RestTemplate for synchronous HTTP communication.
 * 
 * Configured with a 30-second timeout for both connection and read operations to handle
 * potentially long-running LLM-based query generation.
 */
@Component
public class ChatbotServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    /**
     * Constructs a new ChatbotServiceClient with the specified base URL and timeout configuration.
     *
     * The base URL is injected from application properties using the key
     * {@code services.chatbot-service.url}. This allows for easy configuration
     * across different environments (dev, staging, production).
     * 
     * A 30-second timeout is configured for both connection and read operations to accommodate
     * LLM processing time while preventing indefinite hangs.
     *
     * @param baseUrl the base URL of the chatbot service, injected from application properties
     * @param restTemplateBuilder Spring's RestTemplateBuilder for creating configured RestTemplate instances
     */
    public ChatbotServiceClient(
            @Value("${services.chatbot-service.url}") String baseUrl,
            RestTemplateBuilder restTemplateBuilder) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(30))
                .setReadTimeout(Duration.ofSeconds(30))
                .build();
    }

    /**
     * Translates a natural language query into a MongoDB aggregation pipeline
     * and optionally executes it against the chatbot service's database.
     *
     * This method makes an HTTP POST request to the chatbot service's /translate endpoint
     * with the natural language question and an execute flag. When execute is true,
     * the service returns both the generated MongoDB query and the results of executing it.
     * 
     * The request will timeout after 30 seconds if no response is received.
     *
     * @param question the natural language search query
     * @param execute whether to execute the generated query and return results
     * @return a {@link ChatbotTranslateResponseDTO} containing the MongoDB query and optionally results
     * @throws org.springframework.web.client.RestClientException if the HTTP request fails
     * @throws org.springframework.web.client.ResourceAccessException if the request times out after 30 seconds
     */
    public ChatbotTranslateResponseDTO translate(String question, boolean execute) {
        String url = baseUrl;

        ChatbotTranslateRequestDTO request = ChatbotTranslateRequestDTO.builder()
                .question(question)
                .execute(execute)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ChatbotTranslateRequestDTO> entity = new HttpEntity<>(request, headers);

        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                ChatbotTranslateResponseDTO.class
        ).getBody();
    }
}