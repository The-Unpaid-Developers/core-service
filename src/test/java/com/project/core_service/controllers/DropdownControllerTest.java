package com.project.core_service.controllers;

import com.project.core_service.dto.BusinessCapabilityLookupDTO;
import com.project.core_service.dto.TechComponentLookupDTO;
import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.services.DropdownService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DropdownController.class)
@AutoConfigureMockMvc(addFilters = false)
class DropdownControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DropdownService dropdownService;

    // ===== Business Capabilities Tests =====

    @Test
    void getBusinessCapabilities_Success() throws Exception {
        // Arrange
        List<BusinessCapabilityLookupDTO> expectedCapabilities = Arrays.asList(
            new BusinessCapabilityLookupDTO("Policy Management", "Policy Administration", "Policy Issuance"),
            new BusinessCapabilityLookupDTO("Claims Management", "Claims Processing", "First Notice of Loss"),
            new BusinessCapabilityLookupDTO("Customer Management", "Customer Onboarding", "Customer Registration")
        );

        when(dropdownService.getBusinessCapabilities()).thenReturn(expectedCapabilities);

        // Act & Assert
        mockMvc.perform(get("/api/v1/dropdowns/business-capabilities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].l1").value("Policy Management"))
            .andExpect(jsonPath("$[0].l2").value("Policy Administration"))
            .andExpect(jsonPath("$[0].l3").value("Policy Issuance"))
            .andExpect(jsonPath("$[1].l1").value("Claims Management"))
            .andExpect(jsonPath("$[1].l2").value("Claims Processing"))
            .andExpect(jsonPath("$[1].l3").value("First Notice of Loss"))
            .andExpect(jsonPath("$[2].l1").value("Customer Management"))
            .andExpect(jsonPath("$[2].l2").value("Customer Onboarding"))
            .andExpect(jsonPath("$[2].l3").value("Customer Registration"));
    }

    @Test
    void getBusinessCapabilities_EmptyList_Success() throws Exception {
        // Arrange
        when(dropdownService.getBusinessCapabilities()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/dropdowns/business-capabilities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getBusinessCapabilities_NotFound_ThrowsException() throws Exception {
        // Arrange
        when(dropdownService.getBusinessCapabilities()).thenThrow(new NotFoundException("Business capabilities lookup not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/dropdowns/business-capabilities"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getBusinessCapabilities_WithNullValues_Success() throws Exception {
        // Arrange
        List<BusinessCapabilityLookupDTO> capabilitiesWithNulls = Arrays.asList(
            new BusinessCapabilityLookupDTO("Policy Management", null, "Policy Issuance"),
            new BusinessCapabilityLookupDTO(null, "Claims Processing", null),
            new BusinessCapabilityLookupDTO("Customer Management", "Customer Onboarding", "Customer Registration")
        );

        when(dropdownService.getBusinessCapabilities()).thenReturn(capabilitiesWithNulls);

        // Act & Assert
        mockMvc.perform(get("/api/v1/dropdowns/business-capabilities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].l1").value("Policy Management"))
            .andExpect(jsonPath("$[0].l2").doesNotExist())
            .andExpect(jsonPath("$[0].l3").value("Policy Issuance"))
            .andExpect(jsonPath("$[1].l1").doesNotExist())
            .andExpect(jsonPath("$[1].l2").value("Claims Processing"))
            .andExpect(jsonPath("$[1].l3").doesNotExist());
    }

    // ===== Tech Components Tests =====

    @Test
    void getTechComponents_Success() throws Exception {
        // Arrange
        List<TechComponentLookupDTO> expectedComponents = Arrays.asList(
            new TechComponentLookupDTO("Spring Boot", "3.2"),
            new TechComponentLookupDTO("Node.js", "20.x"),
            new TechComponentLookupDTO(".NET Core", "8")
        );

        when(dropdownService.getTechComponents()).thenReturn(expectedComponents);

        // Act & Assert
        mockMvc.perform(get("/api/v1/dropdowns/tech-components"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].productName").value("Spring Boot"))
            .andExpect(jsonPath("$[0].productVersion").value("3.2"))
            .andExpect(jsonPath("$[1].productName").value("Node.js"))
            .andExpect(jsonPath("$[1].productVersion").value("20.x"))
            .andExpect(jsonPath("$[2].productName").value(".NET Core"))
            .andExpect(jsonPath("$[2].productVersion").value("8"));
    }

    @Test
    void getTechComponents_EmptyList_Success() throws Exception {
        // Arrange
        when(dropdownService.getTechComponents()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/dropdowns/tech-components"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getTechComponents_NotFound_ThrowsException() throws Exception {
        // Arrange
        when(dropdownService.getTechComponents()).thenThrow(new NotFoundException("Tech components lookup not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/dropdowns/tech-components"))
            .andExpect(status().isNotFound());
    }

    @Test
    void getTechComponents_WithNullValues_Success() throws Exception {
        // Arrange
        List<TechComponentLookupDTO> componentsWithNulls = Arrays.asList(
            new TechComponentLookupDTO("Spring Boot", null),
            new TechComponentLookupDTO(null, "20.x"),
            new TechComponentLookupDTO(".NET Core", "8")
        );

        when(dropdownService.getTechComponents()).thenReturn(componentsWithNulls);

        // Act & Assert
        mockMvc.perform(get("/api/v1/dropdowns/tech-components"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].productName").value("Spring Boot"))
            .andExpect(jsonPath("$[0].productVersion").doesNotExist())
            .andExpect(jsonPath("$[1].productName").doesNotExist())
            .andExpect(jsonPath("$[1].productVersion").value("20.x"))
            .andExpect(jsonPath("$[2].productName").value(".NET Core"))
            .andExpect(jsonPath("$[2].productVersion").value("8"));
    }
}
