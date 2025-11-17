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

/**
 * Client for communicating with the Chatbot Service API.
 *
 * This client provides methods to interact with the chatbot service's REST endpoints,
 * specifically for translating natural language queries into MongoDB aggregation pipelines
 * and executing them. It uses Spring RestTemplate for synchronous HTTP communication.
 *
 * Connection pooling and timeouts are configured globally via {@link com.project.core_service.config.RestTemplateConfig}.
 * Default timeout is 30 seconds for both connection and socket operations to handle
 * potentially long-running LLM-based query generation.
 */
@Component
public class ChatbotServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    /**
     * Constructs a new ChatbotServiceClient with the specified base URL.
     *
     * The base URL is injected from application properties using the key
     * {@code services.chatbot-service.url}. This allows for easy configuration
     * across different environments (dev, staging, production).
     *
     * The RestTemplate is configured with connection pooling and appropriate timeouts
     * via the RestTemplateBuilder bean defined in {@link com.project.core_service.config.RestTemplateConfig}.
     * Timeouts can be configured via application properties:
     * - http.client.connection.timeout (default: 30000ms)
     * - http.client.socket.timeout (default: 30000ms)
     *
     * @param baseUrl the base URL of the chatbot service, injected from application properties
     * @param restTemplateBuilder Spring's RestTemplateBuilder pre-configured with connection pooling
     */
    public ChatbotServiceClient(
            @Value("${services.chatbot-service.url}") String baseUrl,
            RestTemplateBuilder restTemplateBuilder) {
        this.baseUrl = baseUrl;
        this.restTemplate = restTemplateBuilder.build();
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

        ChatbotTranslateRequestDTO request = ChatbotTranslateRequestDTO.builder()
                .question(question)
                .execute(execute)
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ChatbotTranslateRequestDTO> entity = new HttpEntity<>(request, headers);

        return restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                entity,
                ChatbotTranslateResponseDTO.class
        ).getBody();
    }
}