package com.project.core_service.models.integration_flow;

import com.project.core_service.models.shared.Frequency;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

public class IntegrationFlowTest {

    @Test
    void testConstructorAndGetters() {
        String id = UUID.randomUUID().toString();
        String bsoCode = "BSO-001";
        ExternalSystemRole role = ExternalSystemRole.CONSUMER;
        IntegrationMethod method = IntegrationMethod.API;
        Frequency frequency = Frequency.DAILY; // Assuming Frequency is an enum you have
        String purpose = "Data ingestion";
        int version = 1;

        IntegrationFlow flow = new IntegrationFlow(
                id,
                bsoCode,
                role,
                method,
                frequency,
                purpose,
                version
        );

        assertEquals(id, flow.getId());
        assertEquals(bsoCode, flow.getBsoCodeOfExternalSystem());
        assertEquals(role, flow.getExternalSystemRole());
        assertEquals(method, flow.getIntegrationMethod());
        assertEquals(frequency, flow.getFrequency());
        assertEquals(purpose, flow.getPurpose());
        assertEquals(version, flow.getVersion());
    }
    @Test
    void shouldThrowExceptionWhenNullForNonNullFields() {
        String id = UUID.randomUUID().toString();

        // bsoCodeOfExternalSystem null
        assertThrows(NullPointerException.class, () -> new IntegrationFlow(
                id,
                null,
                ExternalSystemRole.CONSUMER,
                IntegrationMethod.API,
                Frequency.DAILY,
                "Purpose",
                1
        ));

        // externalSystemRole null
        assertThrows(NullPointerException.class, () -> new IntegrationFlow(
                id,
                "BSO-002",
                null,
                IntegrationMethod.API,
                Frequency.DAILY,
                "Purpose",
                1
        ));

        // integrationMethod null
        assertThrows(NullPointerException.class, () -> new IntegrationFlow(
                id,
                "BSO-002",
                ExternalSystemRole.PRODUCER,
                null,
                Frequency.DAILY,
                "Purpose",
                1
        ));

        // frequency null
        assertThrows(NullPointerException.class, () -> new IntegrationFlow(
                id,
                "BSO-002",
                ExternalSystemRole.PRODUCER,
                IntegrationMethod.API,
                null,
                "Purpose",
                1
        ));

        // purpose null
        assertThrows(NullPointerException.class, () -> new IntegrationFlow(
                id,
                "BSO-002",
                ExternalSystemRole.PRODUCER,
                IntegrationMethod.API,
                Frequency.DAILY,
                null,
                1
        ));
    }
    @Test
    void testEqualsAndHashCode() {
        String id = UUID.randomUUID().toString();
        String bsoCode = "BSO-002";
        ExternalSystemRole role = ExternalSystemRole.PRODUCER;
        IntegrationMethod method = IntegrationMethod.BATCH;
        Frequency frequency = Frequency.WEEKLY;
        String purpose = "Report generation";
        int version = 2;

        IntegrationFlow f1 = new IntegrationFlow(
                id, bsoCode, role, method, frequency, purpose, version
        );

        IntegrationFlow f2 = new IntegrationFlow(
                id, bsoCode, role, method, frequency, purpose, version
        );

        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    void testToStringContainsKeyFields() {
        IntegrationFlow flow = new IntegrationFlow(
                "flow-id",
                "BSO-003",
                ExternalSystemRole.CONSUMER,
                IntegrationMethod.EVENT,
                Frequency.MONTHLY,
                "Batch processing",
                3
        );

        String toString = flow.toString();
        assertTrue(toString.contains("flow-id"));
        assertTrue(toString.contains("BSO-003"));
        assertTrue(toString.contains("CONSUMER"));
        assertTrue(toString.contains("EVENT"));
    }
}
