package com.project.core_service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.core_service.dto.BusinessCapabilityLookupDTO;
import com.project.core_service.dto.LookupDTO;
import com.project.core_service.dto.LookupContextDTO;
import com.project.core_service.dto.TechComponentLookupDTO;
import com.project.core_service.exceptions.NotFoundException;
import com.project.core_service.models.lookup.Lookup;
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

    // ===== addLookupContext Tests =====

    @Test
    void addLookupContext_Success() throws Exception {
        // Arrange
        String lookupName = "test-lookup";
        Map<String, String> fieldsDescription = new HashMap<>();
        fieldsDescription.put("field1", "Description for field1");
        fieldsDescription.put("field2", "Description for field2");

        LookupContextDTO requestDTO = LookupContextDTO.builder()
            .description("Test lookup description")
            .fieldsDescription(fieldsDescription)
            .build();

        LookupContextDTO responseDTO = LookupContextDTO.builder()
            .description("Test lookup description")
            .fieldsDescription(fieldsDescription)
            .build();

        when(lookupService.addLookupContext(eq(lookupName), any(LookupContextDTO.class)))
            .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/lookups/{lookupName}/add-lookup-context", lookupName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Test lookup description"))
            .andExpect(jsonPath("$.fieldsDescription.field1").value("Description for field1"))
            .andExpect(jsonPath("$.fieldsDescription.field2").value("Description for field2"));
    }

    @Test
    void addLookupContext_LookupNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        String lookupName = "non-existent-lookup";
        LookupContextDTO requestDTO = LookupContextDTO.builder()
            .description("Test description")
            .fieldsDescription(Map.of("field1", "desc1"))
            .build();

        when(lookupService.addLookupContext(eq(lookupName), any(LookupContextDTO.class)))
            .thenThrow(new NotFoundException("Lookup with name '" + lookupName + "' not found"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/lookups/{lookupName}/add-lookup-context", lookupName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isNotFound());
    }

    @Test
    void addLookupContext_WithEmptyFieldsDescription_Success() throws Exception {
        // Arrange
        String lookupName = "test-lookup";
        Map<String, String> emptyFieldsDescription = new HashMap<>();

        LookupContextDTO requestDTO = LookupContextDTO.builder()
            .description("Test description with empty fields")
            .fieldsDescription(emptyFieldsDescription)
            .build();

        LookupContextDTO responseDTO = LookupContextDTO.builder()
            .description("Test description with empty fields")
            .fieldsDescription(emptyFieldsDescription)
            .build();

        when(lookupService.addLookupContext(eq(lookupName), any(LookupContextDTO.class)))
            .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/lookups/{lookupName}/add-lookup-context", lookupName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Test description with empty fields"))
            .andExpect(jsonPath("$.fieldsDescription").isEmpty());
    }

    @Test
    void addLookupContext_WithMultipleFields_Success() throws Exception {
        // Arrange
        String lookupName = "tech-components";
        Map<String, String> fieldsDescription = new HashMap<>();
        fieldsDescription.put("Product Name", "Name of the technology product");
        fieldsDescription.put("Product Version", "Version number of the product");
        fieldsDescription.put("Adoption Status", "Current adoption status");
        fieldsDescription.put("End-of-Life Date", "Date when support ends");

        LookupContextDTO requestDTO = LookupContextDTO.builder()
            .description("Tech components with EOL information")
            .fieldsDescription(fieldsDescription)
            .build();

        LookupContextDTO responseDTO = LookupContextDTO.builder()
            .description("Tech components with EOL information")
            .fieldsDescription(fieldsDescription)
            .build();

        when(lookupService.addLookupContext(eq(lookupName), any(LookupContextDTO.class)))
            .thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/v1/lookups/{lookupName}/add-lookup-context", lookupName)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Tech components with EOL information"))
            .andExpect(jsonPath("$.fieldsDescription['Product Name']").value("Name of the technology product"))
            .andExpect(jsonPath("$.fieldsDescription['Product Version']").value("Version number of the product"))
            .andExpect(jsonPath("$.fieldsDescription['Adoption Status']").value("Current adoption status"))
            .andExpect(jsonPath("$.fieldsDescription['End-of-Life Date']").value("Date when support ends"));
    }

    // ===== getFieldNames Tests =====

    @Test
    void getFieldNames_Success() throws Exception {
        // Arrange
        String lookupName = "test-lookup";
        List<String> fieldNames = Arrays.asList("name", "age", "department");

        when(lookupService.getFieldNames(lookupName)).thenReturn(fieldNames);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups/{lookupName}/get-field-names", lookupName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0]").value("name"))
            .andExpect(jsonPath("$[1]").value("age"))
            .andExpect(jsonPath("$[2]").value("department"));
    }

    @Test
    void getFieldNames_LookupNotFound_ReturnsNotFound() throws Exception {
        // Arrange
        String lookupName = "non-existent-lookup";

        when(lookupService.getFieldNames(lookupName))
            .thenThrow(new NotFoundException("Lookup with name '" + lookupName + "' not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups/{lookupName}/get-field-names", lookupName))
            .andExpect(status().isNotFound());
    }

    @Test
    void getFieldNames_EmptyData_ReturnsEmptyList() throws Exception {
        // Arrange
        String lookupName = "empty-lookup";

        when(lookupService.getFieldNames(lookupName)).thenReturn(Arrays.asList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups/{lookupName}/get-field-names", lookupName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getFieldNames_WithMultipleFields_Success() throws Exception {
        // Arrange
        String lookupName = "tech-components";
        List<String> fieldNames = Arrays.asList(
            "Product Name",
            "Product Version",
            "Adoption Status",
            "Product Category",
            "End-of-Life Date"
        );

        when(lookupService.getFieldNames(lookupName)).thenReturn(fieldNames);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups/{lookupName}/get-field-names", lookupName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(5))
            .andExpect(jsonPath("$[0]").value("Product Name"))
            .andExpect(jsonPath("$[1]").value("Product Version"))
            .andExpect(jsonPath("$[2]").value("Adoption Status"))
            .andExpect(jsonPath("$[3]").value("Product Category"))
            .andExpect(jsonPath("$[4]").value("End-of-Life Date"));
    }

    @Test
    void getFieldNames_WithSpecialCharacters_Success() throws Exception {
        // Arrange
        String lookupName = "special-lookup";
        List<String> fieldNames = Arrays.asList(
            "field_with_underscore",
            "field-with-dash",
            "field with spaces",
            "field.with.dots"
        );

        when(lookupService.getFieldNames(lookupName)).thenReturn(fieldNames);

        // Act & Assert
        mockMvc.perform(get("/api/v1/lookups/{lookupName}/get-field-names", lookupName))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(4))
            .andExpect(jsonPath("$[0]").value("field_with_underscore"))
            .andExpect(jsonPath("$[1]").value("field-with-dash"))
            .andExpect(jsonPath("$[2]").value("field with spaces"))
            .andExpect(jsonPath("$[3]").value("field.with.dots"));
    }
}