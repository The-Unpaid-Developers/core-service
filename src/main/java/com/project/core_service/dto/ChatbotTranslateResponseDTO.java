package com.project.core_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatbotTranslateResponseDTO {
    @JsonProperty("mongo_query")
    private List<Map<String, Object>> mongoQuery;

    private List<Map<String, Object>> results;
}
