package com.project.core_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing a single business capability with L1, L2, and L3 levels.
 * Used for returning business capability data from lookup tables.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessCapabilityLookupDTO {
    private String l1;
    private String l2;
    private String l3;
}