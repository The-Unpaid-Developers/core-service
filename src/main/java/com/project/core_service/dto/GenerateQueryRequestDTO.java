package com.project.core_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateQueryRequestDTO {

    @NotNull(message = "User prompt cannot be null")
    @NotEmpty(message = "User prompt cannot be empty")
    private String userPrompt;

    @NotNull(message = "Lookup name cannot be null")
    @NotEmpty(message = "Lookup name cannot be empty")
    private String lookupName;

    @NotNull(message = "Lookup fields cannot be null")
    @NotEmpty(message = "Lookup fields cannot be empty")
    private List<String> lookupFieldsUsed;
}
