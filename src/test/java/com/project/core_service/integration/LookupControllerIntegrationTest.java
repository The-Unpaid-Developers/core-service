package com.project.core_service.integration;

import com.project.core_service.dto.LookupDTO;
import com.project.core_service.models.lookup.Lookup;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

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

    @Test
    @DisplayName("Should upload CSV file and store lookup successfully")
    @Description("Tests the complete flow of uploading a CSV file, parsing it, and storing it in MongoDB")
    void uploadCsvFile_ValidFile_Success() throws Exception {
        // Arrange
        String csvContent = "name,age,department\nJohn Doe,30,Engineering\nJane Smith,25,Marketing";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "employees.csv",
            "text/csv",
            csvContent.getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart(BASE_URL + "/upload")
                .file(file)
                .param("lookupName", "employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.lookupName").value("employees"))
            .andExpect(jsonPath("$.recordsProcessed").value(2))
            .andExpect(jsonPath("$.message").value("CSV file processed and stored successfully"));

        // Verify data was stored in MongoDB
        List<Lookup> lookups = mongoTemplate.findAll(Lookup.class, "lookups");
        assertThat(lookups).hasSize(1);
        assertThat(lookups.get(0).getLookupName()).isEqualTo("employees");
        assertThat(lookups.get(0).getRecordCount()).isEqualTo(2);
    }

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
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.totalLookups").value(2))
            .andExpect(jsonPath("$.lookups", hasSize(2)));
    }

    @Test
    @DisplayName("Should retrieve specific lookup by name")
    @Description("Tests retrieving a specific lookup by its name")
    void getLookupByName_ExistingLookup_Success() throws Exception {
        // Arrange
        uploadLookup("employees", "name,age,department\nJohn Doe,30,Engineering\nJane Smith,25,Marketing");

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.lookups", hasSize(1)))
            .andExpect(jsonPath("$.lookups[0].id").value("employees"))
            .andExpect(jsonPath("$.lookups[0].lookupName").value("employees"))
            .andExpect(jsonPath("$.lookups[0].recordCount").value(2))
            .andExpect(jsonPath("$.lookups[0].data", hasSize(2)))
            .andExpect(jsonPath("$.lookups[0].data[0].name").value("John Doe"))
            .andExpect(jsonPath("$.lookups[0].data[0].age").value("30"))
            .andExpect(jsonPath("$.lookups[0].data[0].department").value("Engineering"));
    }

    @Test
    @DisplayName("Should return 404 when lookup not found")
    @Description("Tests that a 404 error is returned when requesting a non-existent lookup")
    void getLookupByName_NonExistent_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/non-existent"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete lookup successfully")
    @Description("Tests the complete flow of deleting a lookup")
    void deleteLookup_ExistingLookup_Success() throws Exception {
        // Arrange
        uploadLookup("employees", "name,age\nJohn,30");

        // Verify it exists
        assertThat(mongoTemplate.findAll(Lookup.class, "lookups")).hasSize(1);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.lookupName").value("employees"))
            .andExpect(jsonPath("$.message").value("Lookup deleted successfully"));

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

    @Test
    @DisplayName("Should replace existing lookup on re-upload")
    @Description("Tests that uploading a CSV with the same name replaces the existing lookup")
    void uploadCsvFile_SameName_ReplacesExisting() throws Exception {
        // Arrange - Upload initial lookup
        uploadLookup("employees", "name,age\nJohn,30\nJane,25");

        // Verify initial state
        List<Lookup> initialLookups = mongoTemplate.findAll(Lookup.class, "lookups");
        assertThat(initialLookups).hasSize(1);
        assertThat(initialLookups.get(0).getRecordCount()).isEqualTo(2);

        // Act - Upload new data with same name
        String newCsvContent = "name,age,department\nBob,35,Sales\nAlice,28,HR\nCharlie,32,IT";
        MockMultipartFile newFile = new MockMultipartFile(
            "file",
            "employees.csv",
            "text/csv",
            newCsvContent.getBytes()
        );

        mockMvc.perform(multipart(BASE_URL + "/upload")
                .file(newFile)
                .param("lookupName", "employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recordsProcessed").value(3));

        // Assert - Verify replacement
        List<Lookup> updatedLookups = mongoTemplate.findAll(Lookup.class, "lookups");
        assertThat(updatedLookups).hasSize(1);
        assertThat(updatedLookups.get(0).getRecordCount()).isEqualTo(3);
        assertThat(updatedLookups.get(0).getData()).hasSize(3);
    }

    @Test
    @DisplayName("Should handle CSV with special characters")
    @Description("Tests uploading CSV with Unicode and special characters")
    void uploadCsvFile_SpecialCharacters_Success() throws Exception {
        // Arrange
        String csvContent = "name,city,description\nJosé García,São Paulo,Café & Résumé\nMüller Schmidt,Zürich,Über große Erfolge";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "international.csv",
            "text/csv",
            csvContent.getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart(BASE_URL + "/upload")
                .file(file)
                .param("lookupName", "international"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.recordsProcessed").value(2));

        // Verify data integrity
        mockMvc.perform(get(BASE_URL + "/international"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lookups[0].data[0].name").value("José García"))
            .andExpect(jsonPath("$.lookups[0].data[0].city").value("São Paulo"))
            .andExpect(jsonPath("$.lookups[0].data[0].description").value("Café & Résumé"));
    }

    @Test
    @DisplayName("Should handle CSV with quoted fields containing commas")
    @Description("Tests uploading CSV with commas inside quoted fields")
    void uploadCsvFile_QuotedFieldsWithCommas_Success() throws Exception {
        // Arrange
        String csvContent = "name,address,notes\n\"John Doe\",\"123 Main St, Apt 4, New York, NY\",\"Likes coffee, tea, and water\"";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "addresses.csv",
            "text/csv",
            csvContent.getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart(BASE_URL + "/upload")
                .file(file)
                .param("lookupName", "addresses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recordsProcessed").value(1));

        // Verify data integrity
        mockMvc.perform(get(BASE_URL + "/addresses"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lookups[0].data[0].address").value("123 Main St, Apt 4, New York, NY"))
            .andExpect(jsonPath("$.lookups[0].data[0].notes").value("Likes coffee, tea, and water"));
    }

    @Test
    @DisplayName("Should reject empty CSV file")
    @Description("Tests that an empty file is rejected with appropriate error")
    void uploadCsvFile_EmptyFile_BadRequest() throws Exception {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file",
            "empty.csv",
            "text/csv",
            new byte[0]
        );

        // Act & Assert
        mockMvc.perform(multipart(BASE_URL + "/upload")
                .file(emptyFile)
                .param("lookupName", "test"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should reject non-CSV file")
    @Description("Tests that non-CSV files are rejected")
    void uploadCsvFile_InvalidFileType_BadRequest() throws Exception {
        // Arrange
        MockMultipartFile txtFile = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "some content".getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart(BASE_URL + "/upload")
                .file(txtFile)
                .param("lookupName", "test"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle CSV with BOM marker")
    @Description("Tests handling of CSV files with UTF-8 BOM")
    void uploadCsvFile_WithBOM_Success() throws Exception {
        // Arrange
        String csvContent = "\uFEFFname,age\nJohn,30\nJane,25";
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "bom.csv",
            "text/csv",
            csvContent.getBytes("UTF-8")
        );

        // Act & Assert
        mockMvc.perform(multipart(BASE_URL + "/upload")
                .file(file)
                .param("lookupName", "bom-test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recordsProcessed").value(2));

        // Verify headers were cleaned (BOM removed)
        mockMvc.perform(get(BASE_URL + "/bom-test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lookups[0].data[0].name").value("John"));
    }

    @Test
    @DisplayName("Should handle large CSV file")
    @Description("Tests uploading and retrieving a large CSV file with many rows")
    void uploadCsvFile_LargeFile_Success() throws Exception {
        // Arrange - Create CSV with 500 rows
        StringBuilder csvBuilder = new StringBuilder("id,name,value\n");
        for (int i = 1; i <= 500; i++) {
            csvBuilder.append(i).append(",Name").append(i).append(",Value").append(i).append("\n");
        }

        MockMultipartFile largeFile = new MockMultipartFile(
            "file",
            "large.csv",
            "text/csv",
            csvBuilder.toString().getBytes()
        );

        // Act & Assert
        mockMvc.perform(multipart(BASE_URL + "/upload")
                .file(largeFile)
                .param("lookupName", "large-dataset"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.recordsProcessed").value(500));

        // Verify retrieval
        MvcResult result = mockMvc.perform(get(BASE_URL + "/large-dataset"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lookups[0].recordCount").value(500))
            .andReturn();

        // Verify data integrity
        LookupDTO response = fromJson(result.getResponse().getContentAsString(), LookupDTO.class);
        assertThat(response.getLookups().get(0).getData()).hasSize(500);
    }

    @Test
    @DisplayName("Should handle complete CRUD lifecycle")
    @Description("Tests create, read, update, and delete operations in sequence")
    void lookupLifecycle_FullCRUD_Success() throws Exception {
        // 1. Create
        uploadLookup("lifecycle-test", "name,value\nItem1,Value1");

        // 2. Read
        mockMvc.perform(get(BASE_URL + "/lifecycle-test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lookups[0].recordCount").value(1));

        // 3. Update (re-upload with more data)
        uploadLookup("lifecycle-test", "name,value\nItem1,Updated1\nItem2,Value2");

        mockMvc.perform(get(BASE_URL + "/lifecycle-test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lookups[0].recordCount").value(2));

        // 4. Delete
        mockMvc.perform(delete(BASE_URL + "/lifecycle-test"))
            .andExpect(status().isOk());

        // Verify deletion
        mockMvc.perform(get(BASE_URL + "/lifecycle-test"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should handle multiple concurrent lookups")
    @Description("Tests managing multiple different lookups simultaneously")
    void multipleLookups_Concurrent_Success() throws Exception {
        // Arrange - Upload multiple lookups
        uploadLookup("employees", "name,age\nJohn,30");
        uploadLookup("departments", "id,name\n1,Engineering");
        uploadLookup("projects", "code,title\nP001,Project Alpha");

        // Assert - All lookups are independent and retrievable
        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalLookups").value(3));

        mockMvc.perform(get(BASE_URL + "/employees"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lookups[0].data[0].name").value("John"));

        mockMvc.perform(get(BASE_URL + "/departments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lookups[0].data[0].name").value("Engineering"));

        mockMvc.perform(get(BASE_URL + "/projects"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.lookups[0].data[0].title").value("Project Alpha"));

        // Delete one should not affect others
        mockMvc.perform(delete(BASE_URL + "/employees"))
            .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalLookups").value(2));
    }

    // Helper method to upload a lookup
    private void uploadLookup(String lookupName, String csvContent) throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            lookupName + ".csv",
            "text/csv",
            csvContent.getBytes()
        );

        mockMvc.perform(multipart(BASE_URL + "/upload")
                .file(file)
                .param("lookupName", lookupName))
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should retrieve business capabilities successfully")
    @Description("Tests the retrieval of business capabilities from lookup data stored in MongoDB")
    void getBusinessCapabilities_WithStoredData_Success() throws Exception {
        // Arrange - Upload business capabilities CSV first
        String businessCapCsvContent = "L1,L2,L3,Description\n" +
            "Policy Management,Policy Administration,Policy Issuance,Create and issue new insurance policies to customers\n" +
            "Claims Management,Claims Processing,First Notice of Loss,Capture initial claim information from customers\n" +
            "Customer Management,Customer Onboarding,Customer Registration,Register new customers in the system";

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "business-capabilities.csv",
            "text/csv",
            businessCapCsvContent.getBytes()
        );

        // Upload the business capabilities data first
        mockMvc.perform(multipart(BASE_URL + "/upload")
                .file(file)
                .param("lookupName", "business-capabilities"))
            .andExpect(status().isOk());

        // Act & Assert - Retrieve business capabilities
        mockMvc.perform(get(BASE_URL + "/business-capabilities"))
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
    @DisplayName("Should return 404 when business capabilities lookup not found")
    @Description("Tests error handling when business capabilities lookup does not exist in the database")
    void getBusinessCapabilities_NotFound_Returns404() throws Exception {
        // Ensure no business-capabilities lookup exists by cleaning database
        mongoTemplate.remove(query(where("lookupName").is("business-capabilities")), "lookups");

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/business-capabilities"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Business capabilities lookup not found"));
    }

    @Test
    @DisplayName("Should return empty array when business capabilities has no data")
    @Description("Tests handling of empty business capabilities lookup")
    void getBusinessCapabilities_EmptyData_ReturnsEmptyArray() throws Exception {
        // Arrange - Create a business capabilities lookup with empty data
        Lookup emptyLookup = Lookup.builder()
            .id("business-capabilities")
            .lookupName("business-capabilities")
            .data(List.of())
            .recordCount(0)
            .uploadedAt(new java.util.Date())
            .build();

        mongoTemplate.save(emptyLookup, "lookups");

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/business-capabilities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Should handle business capabilities with missing fields gracefully")
    @Description("Tests handling of business capabilities data with null or missing L1, L2, L3 fields")
    void getBusinessCapabilities_WithMissingFields_HandlesGracefully() throws Exception {
        // Arrange - Upload business capabilities CSV with some missing fields
        String csvWithMissingFields = "L1,L2,L3,Description\n" +
            "Policy Management,Policy Administration,Policy Issuance,Complete policy process\n" +
            "Claims Management,,First Notice of Loss,Partial claims process\n" +
            ",Customer Onboarding,,Partial customer process";

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "business-capabilities-partial.csv", 
            "text/csv",
            csvWithMissingFields.getBytes()
        );

        // Upload the data first
        mockMvc.perform(multipart(BASE_URL + "/upload")
                .file(file)
                .param("lookupName", "business-capabilities"))
            .andExpect(status().isOk());

        // Act & Assert - Retrieve and verify handling of missing fields
        MvcResult result = mockMvc.perform(get(BASE_URL + "/business-capabilities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].l1").value("Policy Management"))
            .andExpect(jsonPath("$[0].l2").value("Policy Administration"))
            .andExpect(jsonPath("$[0].l3").value("Policy Issuance"))
            .andExpect(jsonPath("$[1].l1").value("Claims Management"))
            .andExpect(jsonPath("$[1].l2").value(""))
            .andExpect(jsonPath("$[1].l3").value("First Notice of Loss"))
            .andExpect(jsonPath("$[2].l1").value(""))
            .andExpect(jsonPath("$[2].l2").value("Customer Onboarding"))
            .andExpect(jsonPath("$[2].l3").value(""))
            .andReturn();

        // Additional verification that response is valid JSON array
        String responseContent = result.getResponse().getContentAsString();
        assertThat(responseContent).startsWith("[");
        assertThat(responseContent).endsWith("]");
    }

    @Test
    @DisplayName("Should handle large business capabilities dataset efficiently")
    @Description("Tests performance and handling of large business capabilities dataset")
    void getBusinessCapabilities_LargeDataset_HandlesEfficiently() throws Exception {
        // Arrange - Create a large business capabilities CSV
        StringBuilder csvBuilder = new StringBuilder("L1,L2,L3,Description\n");
        for (int i = 1; i <= 100; i++) {
            csvBuilder.append(String.format("Level1_%d,Level2_%d,Level3_%d,Description for capability %d\n", i, i, i, i));
        }

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "large-business-capabilities.csv",
            "text/csv",
            csvBuilder.toString().getBytes()
        );

        // Upload the large dataset
        mockMvc.perform(multipart(BASE_URL + "/upload")
                .file(file)
                .param("lookupName", "business-capabilities"))
            .andExpect(status().isOk());

        // Act & Assert - Retrieve large dataset efficiently
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get(BASE_URL + "/business-capabilities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(100))
            .andExpect(jsonPath("$[0].l1").value("Level1_1"))
            .andExpect(jsonPath("$[0].l2").value("Level2_1"))
            .andExpect(jsonPath("$[0].l3").value("Level3_1"))
            .andExpect(jsonPath("$[99].l1").value("Level1_100"))
            .andExpect(jsonPath("$[99].l2").value("Level2_100"))
            .andExpect(jsonPath("$[99].l3").value("Level3_100"));

        long endTime = System.currentTimeMillis();
        long executionTime = endTime - startTime;
        
        // Assert that the operation completes within reasonable time (less than 5 seconds)
        assertThat(executionTime).isLessThan(5000);
    }
}
