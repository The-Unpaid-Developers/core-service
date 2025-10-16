package com.project.core_service.dto;

import com.project.core_service.models.business_capabilities.BusinessCapability;
import com.project.core_service.models.business_capabilities.L1Capability;
import com.project.core_service.models.business_capabilities.L2Capability;
import com.project.core_service.models.business_capabilities.L3Capability;
import com.project.core_service.models.solution_overview.*;
import com.project.core_service.models.solutions_review.DocumentState;
import com.project.core_service.models.solutions_review.SolutionReview;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BusinessCapabilityDiagramDTO class.
 * Tests constructors, factory methods, getters/setters, and edge cases.
 */
@DisplayName("BusinessCapabilityDiagramDTO Tests")
class BusinessCapabilityDiagramDTOTest {

    private BusinessCapabilityDiagramDTO dto;
    private SolutionReview solutionReview;
    private SolutionOverview solutionOverview;
    private List<BusinessCapability> businessCapabilities;

    @BeforeEach
    void setUp() {
        // Create test data
        SolutionDetails solutionDetails = new SolutionDetails(
                "Test Solution",
                "Test Project", 
                "TST-001",
                "Test Architect",
                "Test PM",
                "Test Partner");

        Concern testConcern = new Concern(
                "concern-001",
                ConcernType.RISK,
                "Test concern",
                "High impact",
                "Under review",
                ConcernStatus.UNKNOWN,
                LocalDateTime.of(2025, 10, 16, 10, 0, 0));

        solutionOverview = SolutionOverview.newDraftBuilder()
                .id("overview-001")
                .solutionDetails(solutionDetails)
                .reviewedBy("Test Reviewer")
                .businessUnit(BusinessUnit.UNKNOWN)
                .businessDriver(BusinessDriver.BUSINESS_OR_CUSTOMER_GROWTH)
                .valueOutcome("Test value outcome")
                .applicationUsers(List.of(ApplicationUser.EMPLOYEE))
                .concerns(List.of(testConcern))
                .build();

        businessCapabilities = List.of(
                BusinessCapability.builder()
                        .id("bc-001")
                        .l1Capability(L1Capability.UNKNOWN)
                        .l2Capability(L2Capability.UNKNOWN)
                        .l3Capability(L3Capability.UNKNOWN)
                        .remarks("Customer management")
                        .build(),
                BusinessCapability.builder()
                        .id("bc-002")
                        .l1Capability(L1Capability.UNKNOWN)
                        .l2Capability(L2Capability.UNKNOWN)
                        .l3Capability(L3Capability.UNKNOWN)
                        .remarks("Order processing")
                        .build());

        solutionReview = SolutionReview.newDraftBuilder()
                .id("sr-001")
                .systemCode("SYS-123")
                .solutionOverview(solutionOverview)
                .documentState(DocumentState.ACTIVE)
                .businessCapabilities(businessCapabilities)
                .build();

        dto = new BusinessCapabilityDiagramDTO(
                "SYS-123",
                solutionOverview,
                businessCapabilities);
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create DTO with no-args constructor")
        void shouldCreateDTOWithNoArgsConstructor() {
            // Act
            BusinessCapabilityDiagramDTO emptyDto = new BusinessCapabilityDiagramDTO();

            // Assert
            assertNotNull(emptyDto);
            assertNull(emptyDto.getSystemCode());
            assertNull(emptyDto.getSolutionOverview());
            assertNull(emptyDto.getBusinessCapabilities());
        }

        @Test
        @DisplayName("Should create DTO with all-args constructor")
        void shouldCreateDTOWithAllArgsConstructor() {
            // Act
            BusinessCapabilityDiagramDTO newDto = new BusinessCapabilityDiagramDTO(
                    "SYS-456",
                    solutionOverview,
                    businessCapabilities);

            // Assert
            assertNotNull(newDto);
            assertEquals("SYS-456", newDto.getSystemCode());
            assertEquals(solutionOverview, newDto.getSolutionOverview());
            assertEquals(businessCapabilities, newDto.getBusinessCapabilities());
            assertEquals(2, newDto.getBusinessCapabilities().size());
        }

        @Test
        @DisplayName("Should create DTO with null values")
        void shouldCreateDTOWithNullValues() {
            // Act
            BusinessCapabilityDiagramDTO nullDto = new BusinessCapabilityDiagramDTO(null, null, null);

            // Assert
            assertNotNull(nullDto);
            assertNull(nullDto.getSystemCode());
            assertNull(nullDto.getSolutionOverview());
            assertNull(nullDto.getBusinessCapabilities());
        }
    }

    @Nested
    @DisplayName("Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set systemCode")
        void shouldGetAndSetSystemCode() {
            // Arrange
            BusinessCapabilityDiagramDTO testDto = new BusinessCapabilityDiagramDTO();
            String expectedSystemCode = "SYS-999";

            // Act
            testDto.setSystemCode(expectedSystemCode);

            // Assert
            assertEquals(expectedSystemCode, testDto.getSystemCode());
        }

        @Test
        @DisplayName("Should get and set solutionOverview")
        void shouldGetAndSetSolutionOverview() {
            // Arrange
            BusinessCapabilityDiagramDTO testDto = new BusinessCapabilityDiagramDTO();

            // Act
            testDto.setSolutionOverview(solutionOverview);

            // Assert
            assertEquals(solutionOverview, testDto.getSolutionOverview());
        }

        @Test
        @DisplayName("Should get and set businessCapabilities")
        void shouldGetAndSetBusinessCapabilities() {
            // Arrange
            BusinessCapabilityDiagramDTO testDto = new BusinessCapabilityDiagramDTO();
            List<BusinessCapability> newCapabilities = List.of(
                    BusinessCapability.builder()
                            .id("bc-003")
                            .l1Capability(L1Capability.UNKNOWN)
                            .l2Capability(L2Capability.UNKNOWN)
                            .l3Capability(L3Capability.UNKNOWN)
                            .remarks("New capability")
                            .build());

            // Act
            testDto.setBusinessCapabilities(newCapabilities);

            // Assert
            assertEquals(newCapabilities, testDto.getBusinessCapabilities());
            assertEquals(1, testDto.getBusinessCapabilities().size());
            assertEquals("bc-003", testDto.getBusinessCapabilities().get(0).getId());
        }

        @Test
        @DisplayName("Should handle null values in setters")
        void shouldHandleNullValuesInSetters() {
            // Arrange
            BusinessCapabilityDiagramDTO testDto = new BusinessCapabilityDiagramDTO("test", solutionOverview, businessCapabilities);

            // Act
            testDto.setSystemCode(null);
            testDto.setSolutionOverview(null);
            testDto.setBusinessCapabilities(null);

            // Assert
            assertNull(testDto.getSystemCode());
            assertNull(testDto.getSolutionOverview());
            assertNull(testDto.getBusinessCapabilities());
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create DTO from SolutionReview successfully")
        void shouldCreateDTOFromSolutionReviewSuccessfully() {
            // Act
            BusinessCapabilityDiagramDTO result = BusinessCapabilityDiagramDTO.fromSolutionReview(solutionReview);

            // Assert
            assertNotNull(result);
            assertEquals("SYS-123", result.getSystemCode());
            assertEquals(solutionOverview, result.getSolutionOverview());
            assertEquals(businessCapabilities, result.getBusinessCapabilities());
            assertEquals(2, result.getBusinessCapabilities().size());
        }

        @Test
        @DisplayName("Should create DTO with empty business capabilities")
        void shouldCreateDTOWithEmptyBusinessCapabilities() {
            // Arrange
            SolutionReview reviewWithEmptyCapabilities = SolutionReview.newDraftBuilder()
                    .id("sr-002")
                    .systemCode("SYS-456")
                    .solutionOverview(solutionOverview)
                    .documentState(DocumentState.ACTIVE)
                    .businessCapabilities(new ArrayList<>())
                    .build();

            // Act
            BusinessCapabilityDiagramDTO result = BusinessCapabilityDiagramDTO.fromSolutionReview(reviewWithEmptyCapabilities);

            // Assert
            assertNotNull(result);
            assertEquals("SYS-456", result.getSystemCode());
            assertEquals(solutionOverview, result.getSolutionOverview());
            assertNotNull(result.getBusinessCapabilities());
            assertTrue(result.getBusinessCapabilities().isEmpty());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when SolutionReview is null")
        void shouldThrowIllegalArgumentExceptionWhenSolutionReviewIsNull() {
            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> BusinessCapabilityDiagramDTO.fromSolutionReview(null));

            assertEquals("SolutionReview cannot be null", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {

        @Test
        @DisplayName("Should be equal when all fields are the same")
        void shouldBeEqualWhenAllFieldsAreTheSame() {
            // Arrange
            BusinessCapabilityDiagramDTO dto1 = new BusinessCapabilityDiagramDTO("SYS-123", solutionOverview, businessCapabilities);
            BusinessCapabilityDiagramDTO dto2 = new BusinessCapabilityDiagramDTO("SYS-123", solutionOverview, businessCapabilities);

            // Act & Assert
            assertEquals(dto1, dto2);
            assertEquals(dto1.hashCode(), dto2.hashCode());
        }

        @Test
        @DisplayName("Should not be equal when systemCode differs")
        void shouldNotBeEqualWhenSystemCodeDiffers() {
            // Arrange
            BusinessCapabilityDiagramDTO dto1 = new BusinessCapabilityDiagramDTO("SYS-123", solutionOverview, businessCapabilities);
            BusinessCapabilityDiagramDTO dto2 = new BusinessCapabilityDiagramDTO("SYS-456", solutionOverview, businessCapabilities);

            // Act & Assert
            assertNotEquals(dto1, dto2);
        }

        @Test
        @DisplayName("Should not be equal when compared to null")
        void shouldNotBeEqualWhenComparedToNull() {
            // Act & Assert
            assertNotEquals(null, dto);
        }

        @Test
        @DisplayName("Should not be equal when compared to different type")
        void shouldNotBeEqualWhenComparedToDifferentType() {
            // Act & Assert
            assertNotEquals("not a DTO", dto);
        }
    }

    @Nested
    @DisplayName("ToString Tests")
    class ToStringTests {

        @Test
        @DisplayName("Should generate toString with all fields")
        void shouldGenerateToStringWithAllFields() {
            // Act
            String result = dto.toString();

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("BusinessCapabilityDiagramDTO"));
            assertTrue(result.contains("systemCode=SYS-123"));
        }

        @Test
        @DisplayName("Should generate toString with null fields")
        void shouldGenerateToStringWithNullFields() {
            // Arrange
            BusinessCapabilityDiagramDTO nullDto = new BusinessCapabilityDiagramDTO(null, null, null);

            // Act
            String result = nullDto.toString();

            // Assert
            assertNotNull(result);
            assertTrue(result.contains("BusinessCapabilityDiagramDTO"));
            assertTrue(result.contains("systemCode=null"));
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should handle empty systemCode")
        void shouldHandleEmptySystemCode() {
            // Arrange
            BusinessCapabilityDiagramDTO emptySystemCodeDto = new BusinessCapabilityDiagramDTO("", solutionOverview, businessCapabilities);

            // Act & Assert
            assertEquals("", emptySystemCodeDto.getSystemCode());
            assertNotNull(emptySystemCodeDto.getSolutionOverview());
            assertNotNull(emptySystemCodeDto.getBusinessCapabilities());
        }

        @Test
        @DisplayName("Should handle large business capabilities list")
        void shouldHandleLargeBusinessCapabilitiesList() {
            // Arrange
            List<BusinessCapability> largeList = new ArrayList<>();
            for (int i = 0; i < 100; i++) {
                largeList.add(BusinessCapability.builder()
                        .id("bc-" + i)
                        .l1Capability(L1Capability.UNKNOWN)
                        .l2Capability(L2Capability.UNKNOWN)
                        .l3Capability(L3Capability.UNKNOWN)
                        .remarks("Capability " + i)
                        .build());
            }

            // Act
            BusinessCapabilityDiagramDTO largeDto = new BusinessCapabilityDiagramDTO("SYS-LARGE", solutionOverview, largeList);

            // Assert
            assertEquals("SYS-LARGE", largeDto.getSystemCode());
            assertEquals(100, largeDto.getBusinessCapabilities().size());
            assertEquals("bc-0", largeDto.getBusinessCapabilities().get(0).getId());
            assertEquals("bc-99", largeDto.getBusinessCapabilities().get(99).getId());
        }
    }
}