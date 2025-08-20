package com.project.core_service.models.enterprise_tools;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class EnterpriseToolTest {
    @Mock
    private Tool tool;
    @Test
    void builderSetsFieldsCorrectly() {

        EnterpriseTool enterpriseTool = EnterpriseTool.builder()
                .id("et-123")
                .tool(tool)
                .onboarded(OnboardingStatus.TRUE)
                .integrationStatus("Integrated with core systems")
                .issues("None")
                .solutionOverviewId("sol-101")
                .version(2)
                .build();

        assertEquals("et-123", enterpriseTool.getId());
        assertEquals(tool, enterpriseTool.getTool());
        assertEquals(OnboardingStatus.TRUE, enterpriseTool.getOnboarded());
        assertEquals("Integrated with core systems", enterpriseTool.getIntegrationStatus());
        assertEquals("None", enterpriseTool.getIssues());
        assertEquals("sol-101", enterpriseTool.getSolutionOverviewId());
        assertEquals(2, enterpriseTool.getVersion());
    }

    @Test
    void nonNullFieldsShouldThrowOnNull() {
        assertThrows(NullPointerException.class, () -> {
            EnterpriseTool.builder()
                    .id("et-456")
                    .tool(null) // Should blow up because of @NonNull
                    .onboarded(OnboardingStatus.TRUE)
                    .issues("Pending approval")
                    .solutionOverviewId("sol-202")
                    .build();
        });
    }
    @Test
    void testEnterpriseToolConstructorAndGetters() {
        String enterpriseToolId = UUID.randomUUID().toString();
        EnterpriseTool enterpriseTool = new EnterpriseTool(
                enterpriseToolId,
                tool,
                OnboardingStatus.TRUE,
                "Fully integrated with all pipelines",
                "No issues detected",
                "solution-overview-id-123"
        );

        assertEquals(enterpriseToolId, enterpriseTool.getId());
        assertEquals(tool, enterpriseTool.getTool());
        assertEquals(OnboardingStatus.TRUE, enterpriseTool.getOnboarded());
        assertEquals("Fully integrated with all pipelines", enterpriseTool.getIntegrationStatus());
        assertEquals("No issues detected", enterpriseTool.getIssues());
        assertEquals("solution-overview-id-123", enterpriseTool.getSolutionOverviewId());
    }

    @Test
    void shouldThrowExceptionWhenNullForNonNullFields() {
        assertThrows(NullPointerException.class, () -> new EnterpriseTool(
                "et-002",
                null,  // tool
                OnboardingStatus.TRUE,
                "integration status",
                "issues",
                "sol-002"
        ));

        assertThrows(NullPointerException.class, () -> new EnterpriseTool(
                "et-003",
                tool,
                null,  // onboarded
                "integration status",
                "issues",
                "sol-003"
        ));

        assertThrows(NullPointerException.class, () -> new EnterpriseTool(
                "et-004",
                tool,
                OnboardingStatus.TRUE,
                "integration status",
                null,  // issues
                "sol-004"
        ));

        assertThrows(NullPointerException.class, () -> new EnterpriseTool(
                "et-005",
                tool,
                OnboardingStatus.TRUE,
                "integration status",
                "issues",
                null  // solutionOverviewId
        ));
    }

    @Test
    void testEnterpriseToolEqualityAndHashCode() {
        String enterpriseToolId = UUID.randomUUID().toString();

        EnterpriseTool et1 = new EnterpriseTool(
                enterpriseToolId, tool, OnboardingStatus.TRUE,
                "Integrated", "No issues", "solution-123"
        );

        EnterpriseTool et2 = new EnterpriseTool(
                enterpriseToolId, tool, OnboardingStatus.TRUE,
                "Integrated", "No issues", "solution-123"
        );

        assertEquals(et1, et2);
        assertEquals(et1.hashCode(), et2.hashCode());
    }

    @Test
    void testEnterpriseToolToString() {
        when(tool.toString()).thenReturn("MockedTool");
        EnterpriseTool enterpriseTool = new EnterpriseTool(
                "enterprise-id", tool, OnboardingStatus.TRUE,
                "Integrated", "No issues", "solution-123"
        );

        String toStringResult = enterpriseTool.toString();
        assertTrue(toStringResult.contains("enterprise-id"));
        assertTrue(toStringResult.contains("MockedTool"));
        assertTrue(toStringResult.contains("TRUE"));
    }
}
