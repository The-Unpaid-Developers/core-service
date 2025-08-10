package com.project.core_service.models.enterprise_tools;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;

public class EnterpriseToolTest {

    @Test
    void testEnterpriseToolConstructorAndGetters() {
        String toolId = UUID.randomUUID().toString();
        Tool tool = new Tool(toolId, "Datadog", ToolType.OBSERVABILITY, 1);

        String enterpriseToolId = UUID.randomUUID().toString();
        EnterpriseTool enterpriseTool = new EnterpriseTool(
                enterpriseToolId,
                tool,
                OnboardingStatus.TRUE,
                "Fully integrated with all pipelines",
                "No issues detected",
                "solution-overview-id-123",
                2
        );

        assertEquals(enterpriseToolId, enterpriseTool.getId());
        assertEquals(tool, enterpriseTool.getTool());
        assertEquals(OnboardingStatus.TRUE, enterpriseTool.getOnboarded());
        assertEquals("Fully integrated with all pipelines", enterpriseTool.getIntegrationStatus());
        assertEquals("No issues detected", enterpriseTool.getIssues());
        assertEquals("solution-overview-id-123", enterpriseTool.getSolutionOverviewId());
        assertEquals(2, enterpriseTool.getVersion());
    }

    @Test
    void shouldThrowExceptionWhenNullForNonNullFields() {
        Tool tool = new Tool("tool-001", "Datadog", ToolType.OBSERVABILITY, 1);

        assertThrows(NullPointerException.class, () -> new EnterpriseTool(
                "et-002",
                null,  // tool
                OnboardingStatus.TRUE,
                "integration status",
                "issues",
                "sol-002",
                1
        ));

        assertThrows(NullPointerException.class, () -> new EnterpriseTool(
                "et-003",
                tool,
                null,  // onboarded
                "integration status",
                "issues",
                "sol-003",
                1
        ));

        assertThrows(NullPointerException.class, () -> new EnterpriseTool(
                "et-004",
                tool,
                OnboardingStatus.TRUE,
                "integration status",
                null,  // issues
                "sol-004",
                1
        ));

        assertThrows(NullPointerException.class, () -> new EnterpriseTool(
                "et-005",
                tool,
                OnboardingStatus.TRUE,
                "integration status",
                "issues",
                null,  // solutionOverviewId
                1
        ));
    }

    @Test
    void testEnterpriseToolEqualityAndHashCode() {
        String toolId = UUID.randomUUID().toString();
        Tool tool = new Tool(toolId, "Datadog", ToolType.OBSERVABILITY, 1);

        String enterpriseToolId = UUID.randomUUID().toString();

        EnterpriseTool et1 = new EnterpriseTool(
                enterpriseToolId, tool, OnboardingStatus.TRUE,
                "Integrated", "No issues", "solution-123", 1
        );

        EnterpriseTool et2 = new EnterpriseTool(
                enterpriseToolId, tool, OnboardingStatus.TRUE,
                "Integrated", "No issues", "solution-123", 1
        );

        assertEquals(et1, et2);
        assertEquals(et1.hashCode(), et2.hashCode());
    }

    @Test
    void testEnterpriseToolToString() {
        Tool tool = new Tool("tool-id", "Datadog", ToolType.OBSERVABILITY, 1);
        EnterpriseTool enterpriseTool = new EnterpriseTool(
                "enterprise-id", tool, OnboardingStatus.TRUE,
                "Integrated", "No issues", "solution-123", 1
        );

        String toStringResult = enterpriseTool.toString();
        assertTrue(toStringResult.contains("enterprise-id"));
        assertTrue(toStringResult.contains("Datadog"));
        assertTrue(toStringResult.contains("OBSERVABILITY"));
        assertTrue(toStringResult.contains("TRUE"));
    }
}
