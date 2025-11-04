package com.project.core_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.core_service.dto.BusinessCapabilityLookupDTO;
import com.project.core_service.dto.LookupDTO;
import com.project.core_service.dto.TechComponentLookupDTO;
import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.models.lookup.Lookup;
import com.project.core_service.services.LookupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LookupController.class)
@AutoConfigureMockMvc(addFilters = false)
class LookupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LookupService lookupService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void uploadCsvFile_ValidRequest_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.csv",
            "text/csv",
            "name,age\nJohn,30".getBytes()
        );

        LookupDTO expectedResponse = LookupDTO.builder()
            .success(true)
            .lookupName("employees")
            .recordsProcessed(1)
            .message("CSV file processed and stored successfully")
            .build();

        when(lookupService.processCsvFile(any(), eq("employees")))
            .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/lookups/upload")
                .file(file)
                .param("lookupName", "employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.lookupName").value("employees"))
            .andExpect(jsonPath("$.recordsProcessed").value(1));
    }

    @Test
    void uploadCsvFile_MissingFile_BadRequest() throws Exception {
        mockMvc.perform(multipart("/api/v1/lookups/upload")
                .param("lookupName", "test"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getAllLookups_Success() throws Exception {
        // Arrange
        LookupDTO expectedResponse = LookupDTO.builder()
            .totalLookups(2)
            .lookups(Arrays.asList(
                createMockLookup("lookup1", "Lookup 1"),
                createMockLookup("lookup2", "Lookup 2")
            ))
            .build();

        when(lookupService.getAllLookups()).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalLookups").value(2))
            .andExpect(jsonPath("$.lookups").isArray())
            .andExpect(jsonPath("$.lookups[0].id").value("lookup1"));
    }

    @Test
    void getLookupByName_ExistingLookup_Success() throws Exception {
        // Arrange
        String lookupName = "employees";
        LookupDTO expectedResponse = LookupDTO.builder()
            .lookups(Arrays.asList(createMockLookup(lookupName, "Employee Data")))
            .build();

        when(lookupService.getLookupByName(lookupName)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups/{lookupName}", lookupName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lookups[0].id").value(lookupName))
            .andExpect(jsonPath("$.lookups[0].lookupName").value("Employee Data"));
    }

    @Test
    void deleteLookup_Success() throws Exception {
        // Arrange
        String lookupName = "employees";
        LookupDTO expectedResponse = LookupDTO.builder()
            .success(true)
            .lookupName(lookupName)
            .message("Lookup deleted successfully")
            .build();

        when(lookupService.deleteLookup(lookupName)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/lookups/{lookupName}", lookupName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.lookupName").value(lookupName));
    }

    private Lookup createMockLookup(String id, String name) {
        return Lookup.builder()
            .id(id)
            .lookupName(name)
            .recordCount(10)
            .uploadedAt(new Date())
            .build();
    }

    @Test
    void getBusinessCapabilities_Success() throws Exception {
        // Arrange
        List<BusinessCapabilityLookupDTO> expectedCapabilities = Arrays.asList(
            new BusinessCapabilityLookupDTO("Policy Management", "Policy Administration", "Policy Issuance"),
            new BusinessCapabilityLookupDTO("Claims Management", "Claims Processing", "First Notice of Loss"),
            new BusinessCapabilityLookupDTO("Customer Management", "Customer Onboarding", "Customer Registration")
        );

        when(lookupService.getBusinessCapabilities()).thenReturn(expectedCapabilities);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups/business-capabilities"))
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
        when(lookupService.getBusinessCapabilities()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups/business-capabilities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getBusinessCapabilities_NotFound_ThrowsException() throws Exception {
        // Arrange
        when(lookupService.getBusinessCapabilities()).thenThrow(new NotFoundException("Business capabilities lookup not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups/business-capabilities"))
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

        when(lookupService.getBusinessCapabilities()).thenReturn(capabilitiesWithNulls);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups/business-capabilities"))
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

        when(lookupService.getTechComponents()).thenReturn(expectedComponents);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups/tech-components"))
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
        when(lookupService.getTechComponents()).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups/tech-components"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getTechComponents_NotFound_ThrowsException() throws Exception {
        // Arrange
        when(lookupService.getTechComponents()).thenThrow(new NotFoundException("Tech components lookup not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups/tech-components"))
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

        when(lookupService.getTechComponents()).thenReturn(componentsWithNulls);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups/tech-components"))
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