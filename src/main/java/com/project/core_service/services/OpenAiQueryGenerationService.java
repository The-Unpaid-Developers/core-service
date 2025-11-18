package com.project.core_service.services;

import com.theokanning.openai.completion.chat.ChatCompletionChunk;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.completion.chat.ChatMessageRole;
import com.theokanning.openai.service.OpenAiService;
import com.project.core_service.models.lookup.Lookup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OpenAiQueryGenerationService {

    private final OpenAiService openAiService;
    private final LookupService lookupService;
    private final String modelName;
    private String schemaContent;

    @Autowired
    public OpenAiQueryGenerationService(OpenAiService openAiService,
            LookupService lookupService,
            @Value("${openai.model.name}") String modelName) {
        this.openAiService = openAiService;
        this.lookupService = lookupService;
        this.modelName = modelName;
        this.schemaContent = loadSchemaFromResources();
    }

    /**
     * Load the schema.txt file from resources at service initialization.
     * Note: try-with-resources ensures the BufferedReader (and underlying
     * InputStream) is properly closed.
     */
    private String loadSchemaFromResources() {
        try {
            ClassPathResource resource = new ClassPathResource("schema.txt");
            // BufferedReader's close() will automatically close the underlying
            // InputStreamReader and InputStream
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (IOException e) {
            log.error("Failed to load schema.txt from resources", e);
            return "Schema file not available";
        }
    }

    /**
     * Generate MongoDB aggregation query using OpenAI with streaming
     * 
     * @param userPrompt       The user's natural language query
     * @param lookupName       Name of the lookup to reference
     * @param lookupFieldsUsed Specific fields to filter from the lookup
     * @param emitter          SSE emitter for streaming the response
     */
    public void generateQueryStream(String userPrompt, String lookupName, List<String> lookupFieldsUsed,
            SseEmitter emitter) {
        try {
            Lookup lookup = lookupService.findLookupByName(lookupName);

            String formattedLookupData = filterAndFormatLookupData(lookup, lookupFieldsUsed);

            String systemPrompt = buildSystemPrompt();
            String userMessage = buildUserMessage(userPrompt, formattedLookupData);

            List<ChatMessage> messages = new ArrayList<>();
            messages.add(new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt));
            messages.add(new ChatMessage(ChatMessageRole.USER.value(), userMessage));

            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model(modelName)
                    .messages(messages)
                    .temperature(0.1) // Low temperature for more deterministic output
                    .stream(true)
                    .build();

            log.info("Sending request to OpenAI API for query generation");

            openAiService.streamChatCompletion(chatCompletionRequest)
                    .doOnError(throwable -> {
                        log.error("Error during OpenAI streaming", throwable);
                        emitter.completeWithError(throwable);
                    })
                    .blockingForEach(chunk -> {
                        String content = extractContentFromChunk(chunk);
                        if (content != null && !content.isEmpty()) {
                            try {
                                emitter.send(SseEmitter.event().data(content));
                            } catch (IOException e) {
                                log.error("Error sending SSE event", e);
                                throw new RuntimeException(e);
                            }
                        }
                    });

            emitter.complete();
            log.info("Successfully completed query generation stream");

        } catch (Exception e) {
            log.error("Error generating query with OpenAI", e);
            emitter.completeWithError(e);
        }
    }

    private String extractContentFromChunk(ChatCompletionChunk chunk) {
        if (chunk.getChoices() != null && !chunk.getChoices().isEmpty()) {
            var choice = chunk.getChoices().get(0);
            if (choice.getMessage() != null && choice.getMessage().getContent() != null) {
                return choice.getMessage().getContent();
            }
        }
        return "";
    }

    private String filterAndFormatLookupData(Lookup lookup, List<String> lookupFieldsUsed) {
        // Validate that all requested fields exist in the lookup data
        validateLookupFields(lookup, lookupFieldsUsed);

        StringBuilder formatted = new StringBuilder();
        formatted.append("=== Lookup Data: ").append(lookup.getLookupName()).append(" ===\n\n");
        formatted.append("Description: ").append(lookup.getDescription()).append("\n\n");

        // Add field descriptions for ONLY the requested fields (filtering #1)
        appendFieldDescriptions(formatted, lookup.getFieldDescriptions(), lookupFieldsUsed);

        // Add data records with ONLY the requested fields (filtering #2)
        appendFilteredDataRecords(formatted, lookup.getData(), lookupFieldsUsed);

        return formatted.toString();
    }

    /**
     * Appends field descriptions for only the requested fields.
     * This is the first filtering step - we only describe fields the user wants.
     */
    private void appendFieldDescriptions(StringBuilder formatted, Map<String, String> fieldDescriptions,
            List<String> lookupFieldsUsed) {
        formatted.append("Field Descriptions:\n");
        for (String field : lookupFieldsUsed) {
            String description = fieldDescriptions.getOrDefault(field, "No description available");
            formatted.append("- ").append(field).append(": ").append(description).append("\n");
        }
        formatted.append("\n");
    }

    /**
     * Appends data records showing ONLY the requested fields.
     * This is the second filtering step - we only send the fields specified in
     * lookupFieldsUsed.
     * This significantly reduces token usage by excluding unwanted fields.
     * 
     * Example: If lookup has 10 fields but user only wants 2, we save ~80% of
     * tokens.
     */
    private void appendFilteredDataRecords(StringBuilder formatted, List<Map<String, String>> data,
            List<String> lookupFieldsUsed) {
        formatted.append("Data Records (showing only fields: ").append(String.join(", ", lookupFieldsUsed))
                .append("):\n");

        int count = 0;
        for (Map<String, String> record : data) {
            formatted.append("Record ").append(++count).append(":\n");

            // FILTERING HAPPENS HERE: Only include fields in lookupFieldsUsed
            for (String field : lookupFieldsUsed) {
                // Use getOrDefault to handle records with inconsistent schemas gracefully
                String value = record.getOrDefault(field, "(not set)");
                formatted.append("  ").append(field).append(": ").append(value).append("\n");
            }
            formatted.append("\n");

            // Limit to first 50 records to avoid token limits
            if (count >= 50) {
                formatted.append("... (showing first 50 records out of ").append(data.size()).append(" total)\n");
                break;
            }
        }
    }

    /**
     * Validates that all requested fields exist in the lookup data.
     * This prevents sending invalid field names to OpenAI and reduces token usage.
     * 
     * @param lookup           The lookup containing the data
     * @param lookupFieldsUsed The fields requested by the user
     * @throws IllegalArgumentException if any field doesn't exist in the lookup
     */
    private void validateLookupFields(Lookup lookup, List<String> lookupFieldsUsed) {
        List<Map<String, String>> data = lookup.getData();
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException(
                    "Lookup '" + lookup.getLookupName() + "' contains no data records");
        }

        Set<String> availableFields = data.get(0).keySet();
        List<String> invalidFields = new ArrayList<>();

        for (String requestedField : lookupFieldsUsed) {
            if (!availableFields.contains(requestedField)) {
                invalidFields.add(requestedField);
            }
        }

        if (!invalidFields.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Invalid fields requested for lookup '%s': %s. Available fields: %s",
                            lookup.getLookupName(),
                            String.join(", ", invalidFields),
                            String.join(", ", availableFields)));
        }
    }

    private String buildSystemPrompt() {
        return """
                You are an expert MongoDB query generator. Your task is to generate valid MongoDB aggregation pipelines for the 'solutionReviews' collection based on user requirements.

                Important Guidelines:
                1. Generate ONLY a valid JSON array representing the MongoDB aggregation pipeline
                2. Do NOT include any explanations, comments, or additional text
                3. The output must be valid JSON that can be directly parsed and executed
                4. Use proper MongoDB aggregation operators like $match, $group, $project, $sort, $lookup, etc.
                5. Consider the schema structure carefully - arrays are denoted with []
                6. For nested fields, use dot notation (e.g., "solutionOverview.businessUnit")
                7. For array fields, use appropriate operators like $unwind, $elemMatch, etc.
                8. Ensure the pipeline is efficient and follows MongoDB best practices

                The output should start with '[' and end with ']'.
                """;
    }

    private String buildUserMessage(String userPrompt, String formattedLookupData) {
        StringBuilder message = new StringBuilder();

        message.append("=== Database Schema for 'solutionReviews' Collection ===\n\n");
        message.append(schemaContent);
        message.append("\n\n");

        message.append(formattedLookupData);
        message.append("\n\n");

        message.append("=== User Query ===\n");
        message.append(userPrompt);
        message.append("\n\n");

        message.append(
                "Based on the schema and lookup data provided above, generate a MongoDB aggregation pipeline that fulfills the user's query. Remember to output ONLY the JSON array, nothing else.");

        return message.toString();
    }
}
