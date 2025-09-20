package com.project.core_service.dto;

import com.project.core_service.models.business_capabilities.BusinessCapability;
import com.project.core_service.models.data_asset.DataAsset;
import com.project.core_service.models.enterprise_tools.EnterpriseTool;
import com.project.core_service.models.integration_flow.IntegrationFlow;
import com.project.core_service.models.process_compliance.ProcessCompliant;
import com.project.core_service.models.solution_overview.*;
import com.project.core_service.models.solutions_review.DocumentState;
import com.project.core_service.models.solutions_review.SolutionReview;
import com.project.core_service.models.system_component.SystemComponent;
import com.project.core_service.models.technology_component.TechnologyComponent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for SolutionReviewDTO class.
 * Tests constructors, conversions, factory methods, and utility functions.
 */
class SolutionReviewDTOTest {

    @Mock
    private BusinessCapability mockBusinessCapability;

    @Mock
    private SystemComponent mockSystemComponent;

    @Mock
    private IntegrationFlow mockIntegrationFlow;

    @Mock
    private DataAsset mockDataAsset;

    @Mock
    private TechnologyComponent mockTechnologyComponent;

    @Mock
    private EnterpriseTool mockEnterpriseTool;

    @Mock
    private ProcessCompliant mockProcessCompliant;

    private SolutionOverview realSolutionOverview;
    private SolutionReview testSolutionReview;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        realSolutionOverview = createTestSolutionOverview();
        testSolutionReview = createTestSolutionReview();
    }

    private SolutionOverview createTestSolutionOverview() {
        SolutionDetails solutionDetails = new SolutionDetails(
                "Test Solution",
                "Test Project",
                "AWG001",
                "John Architect",
                "Jane PM",
                "Partner1");

        return new SolutionOverview(
                "overview-1",
                solutionDetails,
                "john.reviewer",
                ReviewType.NEW_BUILD,
                ApprovalStatus.PENDING,
                ReviewStatus.DRAFT,
                "No conditions",
                BusinessUnit.UNKNOWN,
                BusinessDriver.BUSINESS_OR_CUSTOMER_GROWTH,
                "Increase revenue by 10%",
                Arrays.asList(ApplicationUser.CUSTOMERS, ApplicationUser.EMPLOYEE),
                new ArrayList<>());
    }

    private SolutionReview createTestSolutionReview() {
        LocalDateTime testTime = LocalDateTime.now();
        return SolutionReview.builder()
                .id("sr-001")
                .systemCode("sys-001")
                .documentState(DocumentState.DRAFT)
                .solutionOverview(realSolutionOverview)
                .businessCapabilities(Arrays.asList(mockBusinessCapability))
                .systemComponents(Arrays.asList(mockSystemComponent))
                .integrationFlows(Arrays.asList(mockIntegrationFlow))
                .dataAssets(Arrays.asList(mockDataAsset))
                .technologyComponents(Arrays.asList(mockTechnologyComponent))
                .enterpriseTools(Arrays.asList(mockEnterpriseTool))
                .processCompliances(Arrays.asList(mockProcessCompliant))
                .createdAt(testTime)
                .lastModifiedAt(testTime)
                .createdBy("test.creator")
                .lastModifiedBy("test.modifier")
                .build();
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create SolutionReviewDTO with default constructor")
        void shouldCreateSolutionReviewDTOWithDefaultConstructor() {
            SolutionReviewDTO dto = new SolutionReviewDTO();

            assertNotNull(dto);
            assertNull(dto.getId());
            assertNull(dto.getDocumentState());
            assertNull(dto.getSolutionOverview());
            assertNotNull(dto.getBusinessCapabilities());
            assertNotNull(dto.getSystemComponents());
            assertNotNull(dto.getIntegrationFlows());
            assertNotNull(dto.getDataAssets());
            assertNotNull(dto.getTechnologyComponents());
            assertNotNull(dto.getEnterpriseTools());
            assertNotNull(dto.getProcessCompliances());
            assertTrue(dto.getBusinessCapabilities().isEmpty());
        }

        @Test
        @DisplayName("Should create SolutionReviewDTO with all args constructor")
        void shouldCreateSolutionReviewDTOWithAllArgsConstructor() {
            LocalDateTime testTime = LocalDateTime.now();
            List<BusinessCapability> capabilities = Arrays.asList(mockBusinessCapability);

            SolutionReviewDTO dto = new SolutionReviewDTO(
                    "dto-001",
                    "sys-001",
                    DocumentState.SUBMITTED,
                    realSolutionOverview,
                    capabilities,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    testTime,
                    testTime,
                    "creator",
                    "modifier");

            assertEquals("dto-001", dto.getId());
            assertEquals("sys-001", dto.getSystemCode());
            assertEquals(DocumentState.SUBMITTED, dto.getDocumentState());
            assertEquals(realSolutionOverview, dto.getSolutionOverview());
            assertEquals(capabilities, dto.getBusinessCapabilities());
            assertEquals(testTime, dto.getCreatedAt());
            assertEquals(testTime, dto.getLastModifiedAt());
            assertEquals("creator", dto.getCreatedBy());
            assertEquals("modifier", dto.getLastModifiedBy());
        }

        @Test
        @DisplayName("Should create SolutionReviewDTO from SolutionReview entity")
        void shouldCreateSolutionReviewDTOFromSolutionReviewEntity() {
            SolutionReviewDTO dto = new SolutionReviewDTO(testSolutionReview);

            assertEquals(testSolutionReview.getId(), dto.getId());
            assertEquals(testSolutionReview.getDocumentState(), dto.getDocumentState());
            assertEquals(testSolutionReview.getSolutionOverview(), dto.getSolutionOverview());
            assertEquals(testSolutionReview.getCreatedAt(), dto.getCreatedAt());
            assertEquals(testSolutionReview.getLastModifiedAt(), dto.getLastModifiedAt());
            assertEquals(testSolutionReview.getCreatedBy(), dto.getCreatedBy());
            assertEquals(testSolutionReview.getLastModifiedBy(), dto.getLastModifiedBy());

            // Verify lists are defensive copies
            assertNotSame(testSolutionReview.getBusinessCapabilities(), dto.getBusinessCapabilities());
            assertEquals(testSolutionReview.getBusinessCapabilities(), dto.getBusinessCapabilities());
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when creating DTO from null SolutionReview")
        void shouldThrowIllegalArgumentExceptionWhenCreatingDTOFromNullSolutionReview() {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new SolutionReviewDTO(null));
            assertEquals("SolutionReview cannot be null", exception.getMessage());
        }

        @Test
        @DisplayName("Should handle empty lists in SolutionReview entity gracefully")
        void shouldHandleEmptyListsInSolutionReviewEntityGracefully() {
            SolutionReview reviewWithEmptyLists = SolutionReview.builder()
                    .id("sr-empty-lists")
                    .systemCode("sys-001")
                    .documentState(DocumentState.DRAFT)
                    .solutionOverview(realSolutionOverview)
                    .businessCapabilities(new ArrayList<>())
                    .systemComponents(new ArrayList<>())
                    .integrationFlows(new ArrayList<>())
                    .dataAssets(new ArrayList<>())
                    .technologyComponents(new ArrayList<>())
                    .enterpriseTools(new ArrayList<>())
                    .processCompliances(new ArrayList<>())
                    .build();

            SolutionReviewDTO dto = new SolutionReviewDTO(reviewWithEmptyLists);

            assertNotNull(dto.getBusinessCapabilities());
            assertNotNull(dto.getSystemComponents());
            assertNotNull(dto.getIntegrationFlows());
            assertNotNull(dto.getDataAssets());
            assertNotNull(dto.getTechnologyComponents());
            assertNotNull(dto.getEnterpriseTools());
            assertNotNull(dto.getProcessCompliances());
            assertTrue(dto.getBusinessCapabilities().isEmpty());
        }
    }

    @Nested
    @DisplayName("Conversion Tests")
    class ConversionTests {

        @Test
        @DisplayName("Should convert DTO to entity correctly")
        void shouldConvertDTOToEntityCorrectly() {
            SolutionReviewDTO dto = new SolutionReviewDTO(testSolutionReview);

            SolutionReview entity = dto.toEntity();

            assertEquals(dto.getId(), entity.getId());
            assertEquals(dto.getDocumentState(), entity.getDocumentState());
            assertEquals(dto.getSolutionOverview(), entity.getSolutionOverview());
            assertEquals(dto.getCreatedAt(), entity.getCreatedAt());
            assertEquals(dto.getLastModifiedAt(), entity.getLastModifiedAt());
            assertEquals(dto.getCreatedBy(), entity.getCreatedBy());
            assertEquals(dto.getLastModifiedBy(), entity.getLastModifiedBy());

            // Verify lists are defensive copies
            assertNotSame(dto.getBusinessCapabilities(), entity.getBusinessCapabilities());
            assertEquals(dto.getBusinessCapabilities(), entity.getBusinessCapabilities());
        }

        @Test
        @DisplayName("Should maintain data integrity through round-trip conversion")
        void shouldMaintainDataIntegrityThroughRoundTripConversion() {
            // Original entity -> DTO -> entity
            SolutionReviewDTO dto = new SolutionReviewDTO(testSolutionReview);
            SolutionReview convertedEntity = dto.toEntity();

            assertEquals(testSolutionReview.getId(), convertedEntity.getId());
            assertEquals(testSolutionReview.getDocumentState(), convertedEntity.getDocumentState());
            assertEquals(testSolutionReview.getSolutionOverview(), convertedEntity.getSolutionOverview());
            assertEquals(testSolutionReview.getBusinessCapabilities(), convertedEntity.getBusinessCapabilities());
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create DTO from entity using fromEntity factory method")
        void shouldCreateDTOFromEntityUsingFromEntityFactoryMethod() {
            SolutionReviewDTO dto = SolutionReviewDTO.fromEntity(testSolutionReview);

            assertNotNull(dto);
            assertEquals(testSolutionReview.getId(), dto.getId());
            assertEquals(testSolutionReview.getDocumentState(), dto.getDocumentState());
        }

        @Test
        @DisplayName("Should return null when fromEntity is called with null")
        void shouldReturnNullWhenFromEntityIsCalledWithNull() {
            SolutionReviewDTO dto = SolutionReviewDTO.fromEntity(null);

            assertNull(dto);
        }

        @Test
        @DisplayName("Should create draft builder with correct defaults")
        void shouldCreateDraftBuilderWithCorrectDefaults() {
            LocalDateTime beforeBuild = LocalDateTime.now().minusSeconds(1);

            String systemCode = "sys-001";
            SolutionReviewDTO dto = SolutionReviewDTO.newDraftBuilder(systemCode)
                    .solutionOverview(realSolutionOverview)
                    .createdBy("test.creator")
                    .build();

            LocalDateTime afterBuild = LocalDateTime.now().plusSeconds(1);

            assertEquals(systemCode, dto.getSystemCode());
            assertEquals(DocumentState.DRAFT, dto.getDocumentState());
            assertEquals(realSolutionOverview, dto.getSolutionOverview());
            assertEquals("test.creator", dto.getCreatedBy());
            assertNotNull(dto.getCreatedAt());
            assertNotNull(dto.getLastModifiedAt());
            assertTrue(dto.getCreatedAt().isAfter(beforeBuild));
            assertTrue(dto.getCreatedAt().isBefore(afterBuild));
        }

        @Test
        @DisplayName("Should create builder from solution overview")
        void shouldCreateBuilderFromSolutionOverview() {
            String systemCode = "sys-001";
            SolutionReviewDTO dto = SolutionReviewDTO.builderFromSolutionOverview(systemCode, realSolutionOverview)
                    .createdBy("test.creator")
                    .build();

            assertEquals(systemCode, dto.getSystemCode());
            assertEquals(DocumentState.DRAFT, dto.getDocumentState());
            assertEquals(realSolutionOverview, dto.getSolutionOverview());
            assertEquals("test.creator", dto.getCreatedBy());
        }

        @Test
        @DisplayName("Should throw NullPointerException when systemCode called with null")
        void shouldThrowNullPointerExceptionWhenBuilderFromSolutionOverviewCalledWithNull() {
            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> SolutionReviewDTO.builderFromSolutionOverview(null, null));
            assertEquals("SystemCode cannot be null", exception.getMessage());
            NullPointerException exception2 = assertThrows(
                    NullPointerException.class,
                    () -> SolutionReviewDTO.builderFromSolutionOverview("sys-001", null));
            assertEquals("SolutionOverview cannot be null", exception2.getMessage());
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("Should return correct state query results")
        void shouldReturnCorrectStateQueryResults() {
            SolutionReviewDTO draftDto = SolutionReviewDTO.builder()
                    .systemCode("sys-001")
                    .documentState(DocumentState.DRAFT)
                    .build();
            SolutionReviewDTO submittedDto = SolutionReviewDTO.builder()
                    .systemCode("sys-001")
                    .documentState(DocumentState.SUBMITTED)
                    .build();
            SolutionReviewDTO currentDto = SolutionReviewDTO.builder()
                    .systemCode("sys-001")
                    .documentState(DocumentState.ACTIVE)
                    .build();
            SolutionReviewDTO outdatedDto = SolutionReviewDTO.builder()
                    .systemCode("sys-001")
                    .documentState(DocumentState.OUTDATED)
                    .build();

            assertTrue(draftDto.isDraft());
            assertFalse(draftDto.isSubmitted());
            assertFalse(draftDto.isActive());
            assertFalse(draftDto.isOutdated());

            assertFalse(submittedDto.isDraft());
            assertTrue(submittedDto.isSubmitted());
            assertFalse(submittedDto.isActive());
            assertFalse(submittedDto.isOutdated());

            assertFalse(currentDto.isDraft());
            assertFalse(currentDto.isSubmitted());
            assertTrue(currentDto.isActive());
            assertFalse(currentDto.isOutdated());

            assertFalse(outdatedDto.isDraft());
            assertFalse(outdatedDto.isSubmitted());
            assertFalse(outdatedDto.isActive());
            assertTrue(outdatedDto.isOutdated());
        }

        @Test
        @DisplayName("Should return correct hasValidSolutionOverview result")
        void shouldReturnCorrectHasValidSolutionOverviewResult() {
            SolutionReviewDTO dtoWithOverview = SolutionReviewDTO.builder()
                    .systemCode("sys-001")
                    .solutionOverview(realSolutionOverview)
                    .build();
            SolutionReviewDTO dtoWithoutOverview = SolutionReviewDTO.builder()
                    .systemCode("sys-001")
                    .solutionOverview(null)
                    .build();

            assertTrue(dtoWithOverview.hasValidSolutionOverview());
            assertFalse(dtoWithoutOverview.hasValidSolutionOverview());
        }

        @Test
        @DisplayName("Should return correct hasBusinessCapabilities result")
        void shouldReturnCorrectHasBusinessCapabilitiesResult() {
            SolutionReviewDTO dtoWithCapabilities = SolutionReviewDTO.builder()
                    .systemCode("sys-001")
                    .businessCapabilities(Arrays.asList(mockBusinessCapability))
                    .build();
            SolutionReviewDTO dtoWithoutCapabilities = SolutionReviewDTO.builder()
                    .systemCode("sys-001")
                    .build(); // Uses default empty list

            assertTrue(dtoWithCapabilities.hasBusinessCapabilities());
            assertFalse(dtoWithoutCapabilities.hasBusinessCapabilities());
        }

        @Test
        @DisplayName("Should return correct hasSystemComponents result")
        void shouldReturnCorrectHasSystemComponentsResult() {
            SolutionReviewDTO dtoWithComponents = SolutionReviewDTO.builder()
                    .systemCode("sys-001")
                    .systemComponents(Arrays.asList(mockSystemComponent))
                    .build();
            SolutionReviewDTO dtoWithoutComponents = SolutionReviewDTO.builder()
                    .systemCode("sys-001")
                    .build(); // Uses default empty list

            assertTrue(dtoWithComponents.hasSystemComponents());
            assertFalse(dtoWithoutComponents.hasSystemComponents());
        }
    }

    @Nested
    @DisplayName("Builder Pattern Tests")
    class BuilderPatternTests {

        @Test
        @DisplayName("Should handle all fields correctly in builder")
        void shouldHandleAllFieldsCorrectlyInBuilder() {
            LocalDateTime testTime = LocalDateTime.now();
            List<BusinessCapability> capabilities = Arrays.asList(mockBusinessCapability);
            List<SystemComponent> components = Arrays.asList(mockSystemComponent);

            SolutionReviewDTO dto = SolutionReviewDTO.builder()
                    .id("builder-test")
                    .systemCode("sys-001")
                    .documentState(DocumentState.ACTIVE)
                    .solutionOverview(realSolutionOverview)
                    .businessCapabilities(capabilities)
                    .systemComponents(components)
                    .integrationFlows(Arrays.asList(mockIntegrationFlow))
                    .dataAssets(Arrays.asList(mockDataAsset))
                    .technologyComponents(Arrays.asList(mockTechnologyComponent))
                    .enterpriseTools(Arrays.asList(mockEnterpriseTool))
                    .processCompliances(Arrays.asList(mockProcessCompliant))
                    .createdAt(testTime)
                    .lastModifiedAt(testTime)
                    .createdBy("builder.creator")
                    .lastModifiedBy("builder.modifier")
                    .build();

            assertEquals("builder-test", dto.getId());
            assertEquals("sys-001", dto.getSystemCode());
            assertEquals(DocumentState.ACTIVE, dto.getDocumentState());
            assertEquals(realSolutionOverview, dto.getSolutionOverview());
            assertEquals(capabilities, dto.getBusinessCapabilities());
            assertEquals(components, dto.getSystemComponents());
            assertEquals(testTime, dto.getCreatedAt());
            assertEquals(testTime, dto.getLastModifiedAt());
            assertEquals("builder.creator", dto.getCreatedBy());
            assertEquals("builder.modifier", dto.getLastModifiedBy());
        }

        @Test
        @DisplayName("Should use default values for lists when not specified in builder")
        void shouldUseDefaultValuesForListsWhenNotSpecifiedInBuilder() {
            SolutionReviewDTO dto = SolutionReviewDTO.builder()
                    .id("default-test")
                    .systemCode("sys-001")
                    .build();

            assertNotNull(dto.getSystemCode());
            assertNotNull(dto.getBusinessCapabilities());
            assertNotNull(dto.getSystemComponents());
            assertNotNull(dto.getIntegrationFlows());
            assertNotNull(dto.getDataAssets());
            assertNotNull(dto.getTechnologyComponents());
            assertNotNull(dto.getEnterpriseTools());
            assertNotNull(dto.getProcessCompliances());
            assertTrue(dto.getBusinessCapabilities().isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {

        @Test
        @DisplayName("Should handle toString correctly")
        void shouldHandleToStringCorrectly() {
            SolutionReviewDTO dto = new SolutionReviewDTO(testSolutionReview);

            String result = dto.toString();

            assertNotNull(result);
            assertTrue(result.contains("SolutionReviewDTO"));
        }

        @Test
        @DisplayName("Should handle equals and hashCode correctly")
        void shouldHandleEqualsAndHashCodeCorrectly() {
            SolutionReviewDTO dto1 = new SolutionReviewDTO(testSolutionReview);
            SolutionReviewDTO dto2 = new SolutionReviewDTO(testSolutionReview);

            assertEquals(dto1, dto2);
            assertEquals(dto1.hashCode(), dto2.hashCode());

            // Test inequality
            dto2.setDocumentState(DocumentState.SUBMITTED);
            assertNotEquals(dto1, dto2);
        }

        @Test
        @DisplayName("Should handle state changes correctly")
        void shouldHandleStateChangesCorrectly() {
            SolutionReviewDTO dto = new SolutionReviewDTO(testSolutionReview);

            dto.setDocumentState(DocumentState.SUBMITTED);
            assertTrue(dto.isSubmitted());
            assertFalse(dto.isDraft());

            dto.setDocumentState(DocumentState.ACTIVE);
            assertTrue(dto.isActive());
            assertFalse(dto.isSubmitted());
        }

        @Test
        @DisplayName("Should handle list modifications correctly")
        void shouldHandleListModificationsCorrectly() {
            SolutionReviewDTO dto = new SolutionReviewDTO();

            // Lists should be mutable
            dto.getBusinessCapabilities().add(mockBusinessCapability);
            assertEquals(1, dto.getBusinessCapabilities().size());
            assertTrue(dto.hasBusinessCapabilities());

            dto.getSystemComponents().add(mockSystemComponent);
            assertEquals(1, dto.getSystemComponents().size());
            assertTrue(dto.hasSystemComponents());
        }
    }
}
