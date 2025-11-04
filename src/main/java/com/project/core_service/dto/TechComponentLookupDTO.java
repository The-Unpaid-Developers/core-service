package com.project.core_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a single tech component with product name and version.
 * Used for returning tech component data from lookup tables.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TechComponentLookupDTO {
    private String productName;
    private String productVersion;
}