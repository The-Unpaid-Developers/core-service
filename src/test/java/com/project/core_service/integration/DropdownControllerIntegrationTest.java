package com.project.core_service.integration;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("Dropdown Controller Integration Tests")
@Feature("Dropdown Management")
class DropdownControllerIntegrationTest extends BaseIntegrationTest {

    private static final String BASE_URL = "/api/v1/dropdowns";
    private static final String LOOKUPS_URL = "/api/v1/lookups";

    // ===== Business Capabilities Tests =====

    @Test
    @DisplayName("Should retrieve business capabilities successfully")
    @Description("Tests the retrieval of business capabilities")
    void getBusinessCapabilities_WithStoredData_Success() throws Exception {
        // Arrange - Upload business capabilities CSV
        String businessCapCsvContent = """
            L1,L2,L3,Description
            Policy Management,Policy Administration,Policy Issuance,Create and issue new insurance policies
            Claims Management,Claims Processing,First Notice of Loss,Capture initial claim information
            Customer Management,Customer Onboarding,Customer Registration,Register new customers in the system""";

        MockMultipartFile file = new MockMultipartFile(
            "lookupFile",
            "business-capabilities.csv",
            "text/csv",
            businessCapCsvContent.getBytes()
        );

        mockMvc.perform(multipart(LOOKUPS_URL)
                .file(file)
                .param("lookupName", "business-capabilities")
                .param("description", "Business capabilities lookup"))
            .andExpect(status().isOk());

        // Act & Assert - Retrieve business capabilities
        mockMvc.perform(get(BASE_URL + "/business-capabilities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].l1").value("Policy Management"))
            .andExpect(jsonPath("$[0].l2").value("Policy Administration"))
            .andExpect(jsonPath("$[0].l3").value("Policy Issuance"));
    }

    @Test
    @DisplayName("Should return 404 when business capabilities not found")
    @Description("Tests error handling when business capabilities lookup does not exist")
    void getBusinessCapabilities_NotFound_Returns404() throws Exception {
        // Ensure no business-capabilities lookup exists
        mongoTemplate.remove(query(where("lookupName").is("business-capabilities")), "lookups");

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/business-capabilities"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Business capabilities lookup not found"));
    }

    // ===== Tech Components Tests =====

    @Test
    @DisplayName("Should retrieve tech components successfully")
    @Description("Tests the retrieval of tech components")
    void getTechComponents_WithStoredData_Success() throws Exception {
        // Arrange - Upload tech components CSV
        String techComponentsCsvContent = """
            Product Name,Product Version,Adoption Status,Product Category,End-of-Life Date
            Spring Boot,3.2,mainstream,Backend Frameworks,11/24/2025
            Node.js,20.x,mainstream,Backend Frameworks,4/30/2026
            .NET Core,8,mainstream,Backend Frameworks,11/10/2026""";

        MockMultipartFile file = new MockMultipartFile(
            "lookupFile",
            "tech_eol.csv",
            "text/csv",
            techComponentsCsvContent.getBytes()
        );

        mockMvc.perform(multipart(LOOKUPS_URL)
                .file(file)
                .param("lookupName", "tech_eol")
                .param("description", "Tech EOL lookup"))
            .andExpect(status().isOk());

        // Act & Assert - Retrieve tech components
        mockMvc.perform(get(BASE_URL + "/tech-components"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(3))
            .andExpect(jsonPath("$[0].productName").value("Spring Boot"))
            .andExpect(jsonPath("$[0].productVersion").value("3.2"));
    }

    @Test
    @DisplayName("Should return 404 when tech components not found")
    @Description("Tests error handling when tech components lookup does not exist")
    void getTechComponents_NotFound_Returns404() throws Exception {
        // Ensure no tech_eol lookup exists
        mongoTemplate.remove(query(where("lookupName").is("tech_eol")), "lookups");

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/tech-components"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Tech components lookup not found"));
    }
}
