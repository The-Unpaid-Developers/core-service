package com.project.core_service.integration;

import com.project.core_service.dto.LookupFieldDescriptionsDTO;
import com.project.core_service.models.lookup.Lookup;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Lookup Controller Integration Tests")
@Feature("Lookup Management")
class LookupControllerIntegrationTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/v1/lookups";

    // ===== Create Lookup Tests =====

    @Test
    @DisplayName("Should create CSV lookup successfully")
    @Description("Tests creating a new lookup from CSV file")
    void createLookup_ValidFile_Success() throws Exception {
        // Arrange
        String csvContent = "name,age,department\nJohn Doe,30,Engineering\nJane Smith,25,Marketing";
        MockMultipartFile file = new MockMultipartFile(
            "lookupFile",
            "employees.csv",
            "text/csv",
            csvContent.getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart(BASE_URL)
                .file(file)
                .param("lookupName", "employees")
                .param("description", "Employee lookup"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("employees"))
            .andExpect(jsonPath("$.lookupName").value("employees"))
            .andExpect(jsonPath("$.recordCount").value(2))
            .andExpect(jsonPath("$.description").value("Employee lookup"));

        // Verify data was stored in MongoDB
        List<Lookup> lookups = mongoTemplate.findAll(Lookup.class, "lookups");
        assertThat(lookups).hasSize(1);
        assertThat(lookups.get(0).getLookupName()).isEqualTo("employees");
        assertThat(lookups.get(0).getRecordCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should reject empty CSV file")
    @Description("Tests that an empty file is rejected")
    void createLookup_EmptyFile_BadRequest() throws Exception {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
            "lookupFile",
            "empty.csv",
            "text/csv",
            new byte[0]
        );

        // Act & Assert
        mockMvc.perform(multipart(BASE_URL)
                .file(emptyFile)
                .param("lookupName", "test")
                .param("description", "Test lookup"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject non-CSV file")
    @Description("Tests that non-CSV files are rejected")
    void createLookup_InvalidFileType_BadRequest() throws Exception {
        // Arrange
        MockMultipartFile txtFile = new MockMultipartFile(
            "lookupFile",
            "test.txt",
            "text/plain",
            "some content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart(BASE_URL)
                .file(txtFile)
                .param("lookupName", "test")
                .param("description", "Test lookup"))
            .andExpect(status().isBadRequest());
    }

    // ===== Get All Lookups Tests =====

    @Test
    @DisplayName("Should retrieve all lookups successfully")
    @Description("Tests retrieving all lookups from the database")
    void getAllLookups_Success() throws Exception {
        // Arrange - Upload two lookups
        uploadLookup("employees", "name,age\nJohn,30\nJane,25");
        uploadLookup("departments", "id,name\n1,Engineering\n2,Marketing");

        // Act & Assert
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2));
    }

    // ===== Get Lookup By Name Tests =====

    @Test
    @DisplayName("Should retrieve specific lookup by name")
    @Description("Tests retrieving a specific lookup by its name")
    void getLookupByName_ExistingLookup_Success() throws Exception {
        // Arrange
        uploadLookup("employees", "name,age,department\nJohn Doe,30,Engineering\nJane Smith,25,Marketing");

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("employees"))
            .andExpect(jsonPath("$.lookupName").value("employees"))
            .andExpect(jsonPath("$.recordCount").value(2))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data", hasSize(2)))
            .andExpect(jsonPath("$.data[0].name").value("John Doe"))
            .andExpect(jsonPath("$.data[0].age").value("30"))
            .andExpect(jsonPath("$.data[0].department").value("Engineering"));
    }

    @Test
    @DisplayName("Should return 404 when lookup not found")
    @Description("Tests that a 404 error is returned when requesting a non-existent lookup")
    void getLookupByName_NonExistent_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/non-existent"))
            .andExpect(status().isNotFound());
    }

    // ===== Delete Lookup Tests =====

    @Test
    @DisplayName("Should delete lookup successfully")
    @Description("Tests deleting a lookup")
    void deleteLookup_ExistingLookup_Success() throws Exception {
        // Arrange
        uploadLookup("employees", "name,age\nJohn,30");

        // Verify it exists
        assertThat(mongoTemplate.findAll(Lookup.class, "lookups")).hasSize(1);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/employees"))
            .andExpect(status().isNoContent());

        // Verify it was deleted from MongoDB
        assertThat(mongoTemplate.findAll(Lookup.class, "lookups")).isEmpty();
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent lookup")
    @Description("Tests that a 404 error is returned when deleting a lookup that doesn't exist")
    void deleteLookup_NonExistent_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/non-existent"))
            .andExpect(status().isNotFound());
    }

    // ===== Update Lookup Tests =====

    @Test
    @DisplayName("Should update existing lookup")
    @Description("Tests updating a lookup with new CSV data")
    void updateLookup_WithNewData_Success() throws Exception {
        // Arrange - Upload initial lookup
        uploadLookup("employees", "name,age\nJohn,30\nJane,25");

        // Verify initial state
        List<Lookup> initialLookups = mongoTemplate.findAll(Lookup.class, "lookups");
        assertThat(initialLookups).hasSize(1);
        assertThat(initialLookups.get(0).getRecordCount()).isEqualTo(2);

        // Act - Update with new data
        String newCsvContent = "name,age,department\nBob,35,Sales\nAlice,28,HR\nCharlie,32,IT";
        MockMultipartFile newFile = new MockMultipartFile(
            "lookupFile",
            "employees.csv",
            "text/csv",
            newCsvContent.getBytes()
        );

        mockMvc.perform(multipart(BASE_URL + "/employees")
                .file(newFile)
                .param("description", "Updated employee lookup")
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recordCount").value(3));

        // Assert - Verify update
        List<Lookup> updatedLookups = mongoTemplate.findAll(Lookup.class, "lookups");
        assertThat(updatedLookups).hasSize(1);
        assertThat(updatedLookups.get(0).getRecordCount()).isEqualTo(3);
        assertThat(updatedLookups.get(0).getData()).hasSize(3);
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent lookup")
    @Description("Tests error handling when updating a lookup that doesn't exist")
    void updateLookup_NonExistent_NotFound() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "lookupFile",
            "test.csv",
            "text/csv",
            "name,value\nTest,123".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart(BASE_URL + "/non-existent")
                .file(file)
                .param("description", "Test")
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
            .andExpect(status().isNotFound());
    }

    // ===== Update Field Descriptions Tests =====

    @Test
    @DisplayName("Should update field descriptions successfully")
    @Description("Tests updating field descriptions for an existing lookup")
    void updateFieldDescriptions_ExistingLookup_Success() throws Exception {
        // Arrange - Upload a lookup first
        String csvContent = "name,age,department\nJohn,30,Engineering\nJane,25,Marketing";
        uploadLookup("employees", csvContent);

        // Create field descriptions DTO
        Map<String, String> fieldDescriptions = new HashMap<>();
        fieldDescriptions.put("name", "Employee full name");
        fieldDescriptions.put("age", "Employee age in years");
        fieldDescriptions.put("department", "Department where employee works");

        LookupFieldDescriptionsDTO contextDTO = LookupFieldDescriptionsDTO.builder()
            .fieldDescriptions(fieldDescriptions)
            .build();

        // Act & Assert - Update field descriptions
        mockMvc.perform(put(BASE_URL + "/employees/field-descriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(contextDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fieldDescriptions.name").value("Employee full name"))
            .andExpect(jsonPath("$.fieldDescriptions.age").value("Employee age in years"))
            .andExpect(jsonPath("$.fieldDescriptions.department").value("Department where employee works"));

        // Verify field descriptions were saved in MongoDB
        Lookup savedLookup = mongoTemplate.findOne(
            query(where("_id").is("employees")),
            Lookup.class,
            "lookups"
        );

        assertThat(savedLookup).isNotNull();
        assertThat(savedLookup.getFieldDescriptions()).isNotNull();
        assertThat(savedLookup.getFieldDescriptions()).hasSize(3);
        assertThat(savedLookup.getFieldDescriptions().get("name")).isEqualTo("Employee full name");
    }

    @Test
    @DisplayName("Should return 404 when updating field descriptions for non-existent lookup")
    @Description("Tests error handling when updating field descriptions for a non-existent lookup")
    void updateFieldDescriptions_NonExistent_Returns404() throws Exception {
        // Arrange
        LookupFieldDescriptionsDTO contextDTO = LookupFieldDescriptionsDTO.builder()
            .fieldDescriptions(Map.of("field1", "description1"))
            .build();

        // Act & Assert
        mockMvc.perform(put(BASE_URL + "/non-existent/field-descriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(contextDTO)))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Lookup with name 'non-existent' not found"));
    }

    // ===== Get Field Descriptions Tests =====

    @Test
    @DisplayName("Should retrieve field descriptions successfully")
    @Description("Tests retrieving field descriptions for an existing lookup")
    void getFieldDescriptions_ExistingLookup_Success() throws Exception {
        // Arrange - Upload a lookup and set field descriptions
        uploadLookup("employees", "name,age\nJohn,30");

        Map<String, String> fieldDescriptions = Map.of(
            "name", "Employee name",
            "age", "Employee age"
        );

        LookupFieldDescriptionsDTO contextDTO = LookupFieldDescriptionsDTO.builder()
            .fieldDescriptions(fieldDescriptions)
            .build();

        mockMvc.perform(put(BASE_URL + "/employees/field-descriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(toJson(contextDTO)))
            .andExpect(status().isOk());

        // Act & Assert - Retrieve field descriptions
        mockMvc.perform(get(BASE_URL + "/employees/field-descriptions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fieldDescriptions.name").value("Employee name"))
            .andExpect(jsonPath("$.fieldDescriptions.age").value("Employee age"));
    }

    @Test
    @DisplayName("Should return 404 when getting field descriptions for non-existent lookup")
    @Description("Tests error handling when retrieving field descriptions for a non-existent lookup")
    void getFieldDescriptions_NonExistent_Returns404() throws Exception {
        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/non-existent/field-descriptions"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Lookup with name 'non-existent' not found"));
    }

    // ===== Helper Methods =====

    private void uploadLookup(String lookupName, String csvContent) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "lookupFile",
            lookupName + ".csv",
            "text/csv",
            csvContent.getBytes()
        );

        mockMvc.perform(multipart(BASE_URL)
                .file(file)
                .param("lookupName", lookupName)
                .param("description", "Test lookup for " + lookupName))
            .andExpect(status().isOk());
    }
}
