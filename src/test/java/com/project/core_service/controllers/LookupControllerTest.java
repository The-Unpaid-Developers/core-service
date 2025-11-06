package com.project.core_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.core_service.dto.BusinessCapabilityLookupDTO;
import com.project.core_service.dto.CreateLookupDTO;
import com.project.core_service.dto.LookupDTO;
import com.project.core_service.dto.LookupFieldDescriptionsDTO;
import com.project.core_service.dto.LookupWODataDTO;
import com.project.core_service.dto.TechComponentLookupDTO;
import com.project.core_service.dto.UpdateLookupDTO;
import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.services.LookupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
    void createLookup_ValidRequest_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "lookupFile",
            "test.csv",
            "text/csv",
            "name,age\nJohn,30".getBytes()
        );

        List<Map<String, String>> data = Arrays.asList(Map.of("name", "John", "age", "30"));
        Map<String, String> fieldDescriptions = Map.of("name", "", "age", "");

        LookupDTO expectedResponse = LookupDTO.builder()
            .id("employees")
            .lookupName("employees")
            .data(data)
            .uploadedAt(new Date())
            .recordCount(1)
            .description("Employee data")
            .fieldDescriptions(fieldDescriptions)
            .build();

        when(lookupService.createLookup(any(CreateLookupDTO.class)))
            .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/lookups")
                .file(file)
                .param("lookupName", "employees")
                .param("description", "Employee data"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("employees"))
            .andExpect(jsonPath("$.lookupName").value("employees"))
            .andExpect(jsonPath("$.recordCount").value(1))
            .andExpect(jsonPath("$.description").value("Employee data"));
    }

    @Test
    void getAllLookups_Success() throws Exception {
        // Arrange
        List<LookupWODataDTO> expectedResponse = Arrays.asList(
            createMockLookupWOData("lookup1", "Lookup 1", 10),
            createMockLookupWOData("lookup2", "Lookup 2", 20)
        );

        when(lookupService.getAllLookups()).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value("lookup1"))
            .andExpect(jsonPath("$[0].lookupName").value("Lookup 1"))
            .andExpect(jsonPath("$[0].recordCount").value(10))
            .andExpect(jsonPath("$[1].id").value("lookup2"));
    }

    @Test
    void getLookupByName_ExistingLookup_Success() throws Exception {
        // Arrange
        String lookupName = "employees";
        List<Map<String, String>> data = Arrays.asList(Map.of("name", "John", "age", "30"));
        Map<String, String> fieldDescriptions = Map.of("name", "Employee name", "age", "Age");

        LookupDTO expectedResponse = LookupDTO.builder()
            .id(lookupName)
            .lookupName(lookupName)
            .data(data)
            .uploadedAt(new Date())
            .recordCount(1)
            .description("Employee Data")
            .fieldDescriptions(fieldDescriptions)
            .build();

        when(lookupService.getLookupByName(lookupName)).thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups/{lookupName}", lookupName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(lookupName))
            .andExpect(jsonPath("$.lookupName").value(lookupName))
            .andExpect(jsonPath("$.description").value("Employee Data"))
            .andExpect(jsonPath("$.recordCount").value(1));
    }

    @Test
    void deleteLookup_Success() throws Exception {
        // Arrange
        String lookupName = "employees";
        doNothing().when(lookupService).deleteLookup(lookupName);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/lookups/{lookupName}", lookupName))
            .andExpect(status().isNoContent());
    }

    private LookupWODataDTO createMockLookupWOData(String id, String name, int recordCount) {
        return LookupWODataDTO.builder()
            .id(id)
            .lookupName(name)
            .recordCount(recordCount)
            .uploadedAt(new Date())
            .description("Test description")
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

    // ===== updateFieldDescriptions Tests =====

    @Test
    void updateFieldDescriptions_Success() throws Exception {
        // Arrange
        String lookupName = "test-lookup";
        Map<String, String> fieldDescriptions = new HashMap<>();
        fieldDescriptions.put("field1", "Description for field1");
        fieldDescriptions.put("field2", "Description for field2");

        LookupFieldDescriptionsDTO requestDTO = LookupFieldDescriptionsDTO.builder()
            .fieldDescriptions(fieldDescriptions)
            .build();

        LookupFieldDescriptionsDTO responseDTO = LookupFieldDescriptionsDTO.builder()
            .fieldDescriptions(fieldDescriptions)
            .build();

        when(lookupService.updateFieldDescriptions(eq(lookupName), any(LookupFieldDescriptionsDTO.class)))
            .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/lookups/{lookupName}/field-descriptions", lookupName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fieldDescriptions.field1").value("Description for field1"))
            .andExpect(jsonPath("$.fieldDescriptions.field2").value("Description for field2"));
    }

    @Test
    void updateFieldDescriptions_LookupNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        String lookupName = "non-existent-lookup";
        LookupFieldDescriptionsDTO requestDTO = LookupFieldDescriptionsDTO.builder()
            .fieldDescriptions(Map.of("field1", "desc1"))
            .build();

        when(lookupService.updateFieldDescriptions(eq(lookupName), any(LookupFieldDescriptionsDTO.class)))
            .thenThrow(new NotFoundException("Lookup with name '" + lookupName + "' not found"));

        // Act & Assert
        mockMvc.perform(put("/api/v1/lookups/{lookupName}/field-descriptions", lookupName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateFieldDescriptions_WithEmptyFieldDescriptions_Success() throws Exception {
        // Arrange
        String lookupName = "test-lookup";
        Map<String, String> emptyFieldDescriptions = new HashMap<>();

        LookupFieldDescriptionsDTO requestDTO = LookupFieldDescriptionsDTO.builder()
            .fieldDescriptions(emptyFieldDescriptions)
            .build();

        LookupFieldDescriptionsDTO responseDTO = LookupFieldDescriptionsDTO.builder()
            .fieldDescriptions(emptyFieldDescriptions)
            .build();

        when(lookupService.updateFieldDescriptions(eq(lookupName), any(LookupFieldDescriptionsDTO.class)))
            .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/lookups/{lookupName}/field-descriptions", lookupName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fieldDescriptions").isEmpty());
    }

    @Test
    void updateFieldDescriptions_WithMultipleFields_Success() throws Exception {
        // Arrange
        String lookupName = "tech-components";
        Map<String, String> fieldDescriptions = new HashMap<>();
        fieldDescriptions.put("Product Name", "Name of the technology product");
        fieldDescriptions.put("Product Version", "Version number of the product");
        fieldDescriptions.put("Adoption Status", "Current adoption status");
        fieldDescriptions.put("End-of-Life Date", "Date when support ends");

        LookupFieldDescriptionsDTO requestDTO = LookupFieldDescriptionsDTO.builder()
            .fieldDescriptions(fieldDescriptions)
            .build();

        LookupFieldDescriptionsDTO responseDTO = LookupFieldDescriptionsDTO.builder()
            .fieldDescriptions(fieldDescriptions)
            .build();

        when(lookupService.updateFieldDescriptions(eq(lookupName), any(LookupFieldDescriptionsDTO.class)))
            .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/v1/lookups/{lookupName}/field-descriptions", lookupName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fieldDescriptions['Product Name']").value("Name of the technology product"))
            .andExpect(jsonPath("$.fieldDescriptions['Product Version']").value("Version number of the product"))
            .andExpect(jsonPath("$.fieldDescriptions['Adoption Status']").value("Current adoption status"))
            .andExpect(jsonPath("$.fieldDescriptions['End-of-Life Date']").value("Date when support ends"));
    }

    // ===== getFieldDescriptions Tests =====

    @Test
    void getFieldDescriptions_Success() throws Exception {
        // Arrange
        String lookupName = "test-lookup";
        Map<String, String> fieldDescriptions = new HashMap<>();
        fieldDescriptions.put("field1", "Description for field1");
        fieldDescriptions.put("field2", "Description for field2");

        LookupFieldDescriptionsDTO responseDTO = LookupFieldDescriptionsDTO.builder()
            .fieldDescriptions(fieldDescriptions)
            .build();

        when(lookupService.getFieldDescriptionsDTO(lookupName)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups/{lookupName}/field-descriptions", lookupName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fieldDescriptions.field1").value("Description for field1"))
            .andExpect(jsonPath("$.fieldDescriptions.field2").value("Description for field2"));
    }

    @Test
    void getFieldDescriptions_LookupNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        String lookupName = "non-existent-lookup";

        when(lookupService.getFieldDescriptionsDTO(lookupName))
            .thenThrow(new NotFoundException("Lookup with name '" + lookupName + "' not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups/{lookupName}/field-descriptions", lookupName))
            .andExpect(status().isNotFound());
    }

    @Test
    void getFieldDescriptions_EmptyFieldDescriptions_Success() throws Exception {
        // Arrange
        String lookupName = "test-lookup";
        Map<String, String> emptyFieldDescriptions = new HashMap<>();

        LookupFieldDescriptionsDTO responseDTO = LookupFieldDescriptionsDTO.builder()
            .fieldDescriptions(emptyFieldDescriptions)
            .build();

        when(lookupService.getFieldDescriptionsDTO(lookupName)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups/{lookupName}/field-descriptions", lookupName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fieldDescriptions").isEmpty());
    }

    // ===== updateLookup Tests =====

    @Test
    void updateLookup_Success() throws Exception {
        // Arrange
        String lookupName = "test-lookup";
        MockMultipartFile file = new MockMultipartFile(
            "lookupFile",
            "test.csv",
            "text/csv",
            "name,age\nJohn,30".getBytes()
        );

        List<Map<String, String>> data = Arrays.asList(Map.of("name", "John", "age", "30"));
        Map<String, String> fieldDescriptions = Map.of("name", "", "age", "");

        LookupDTO expectedResponse = LookupDTO.builder()
            .id(lookupName)
            .lookupName(lookupName)
            .data(data)
            .uploadedAt(new Date())
            .recordCount(1)
            .description("Updated description")
            .fieldDescriptions(fieldDescriptions)
            .build();

        when(lookupService.updateLookup(eq(lookupName), any(UpdateLookupDTO.class)))
            .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/lookups/{lookupName}", lookupName)
                .file(file)
                .param("description", "Updated description")
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(lookupName))
            .andExpect(jsonPath("$.lookupName").value(lookupName))
            .andExpect(jsonPath("$.recordCount").value(1))
            .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    void updateLookup_LookupNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        String lookupName = "non-existent";
        MockMultipartFile file = new MockMultipartFile(
            "lookupFile",
            "test.csv",
            "text/csv",
            "name,age\nJohn,30".getBytes()
        );

        when(lookupService.updateLookup(eq(lookupName), any(UpdateLookupDTO.class)))
            .thenThrow(new NotFoundException("Lookup with name '" + lookupName + "' not found"));

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/lookups/{lookupName}", lookupName)
                .file(file)
                .param("description", "Updated description")
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateLookup_OnlyDescription_Success() throws Exception {
        // Arrange
        String lookupName = "test-lookup";

        List<Map<String, String>> data = Arrays.asList(Map.of("name", "John", "age", "30"));
        Map<String, String> fieldDescriptions = Map.of("name", "Name field", "age", "Age field");

        LookupDTO expectedResponse = LookupDTO.builder()
            .id(lookupName)
            .lookupName(lookupName)
            .data(data)
            .uploadedAt(new Date())
            .recordCount(5)
            .description("Updated description only")
            .fieldDescriptions(fieldDescriptions)
            .build();

        when(lookupService.updateLookup(eq(lookupName), any(UpdateLookupDTO.class)))
            .thenReturn(expectedResponse);

        // Act & Assert
        mockMvc.perform(multipart("/api/v1/lookups/{lookupName}", lookupName)
                .param("description", "Updated description only")
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(lookupName))
            .andExpect(jsonPath("$.lookupName").value(lookupName))
            .andExpect(jsonPath("$.description").value("Updated description only"));
    }
}
