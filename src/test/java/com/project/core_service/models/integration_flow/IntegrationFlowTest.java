package com.project.core_service.models.integration_flow;

import com.project.core_service.models.shared.Frequency;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

public class IntegrationFlowTest {

    @Test
    void builderSetsFieldsCorrectly() {
        IntegrationFlow flow = IntegrationFlow.builder()
                .id("if-123")
                .componentName("MyComponent")
                .counterpartSystemCode("BSO-999")
                .counterpartSystemRole(CounterpartSystemRole.CONSUMER)
                .integrationMethod(IntegrationMethod.API)
                .frequency(Frequency.ANNUALLY)
                .purpose("Data sync with external system")
                .build();

        assertEquals("if-123", flow.getId());
        assertEquals("MyComponent", flow.getComponentName());
        assertEquals("BSO-999", flow.getCounterpartSystemCode());
        assertEquals(CounterpartSystemRole.CONSUMER, flow.getCounterpartSystemRole());
        assertEquals(IntegrationMethod.API, flow.getIntegrationMethod());
        assertEquals(Frequency.ANNUALLY, flow.getFrequency());
        assertEquals("Data sync with external system", flow.getPurpose());
    }

    @Test
    void nonNullFieldsShouldThrowOnNull() {
        IntegrationFlow.IntegrationFlowBuilder builder = IntegrationFlow.builder()
                .id("if-456")
                .componentName("MyComponent")
                .counterpartSystemCode(null) // should fail because of @NonNull
                .counterpartSystemRole(CounterpartSystemRole.PRODUCER)
                .integrationMethod(IntegrationMethod.FILE)
                .frequency(Frequency.ANNUALLY)
                .purpose("File transfer with external system");

        assertThrows(NullPointerException.class, () -> builder.build());
    }

    @Test
    void testConstructorAndGetters() {
        String id = UUID.randomUUID().toString();
        String componentName = "PaymentService";
        String bsoCode = "BSO-001";
        CounterpartSystemRole role = CounterpartSystemRole.CONSUMER;
        IntegrationMethod method = IntegrationMethod.API;
        Frequency frequency = Frequency.DAILY;
        String purpose = "Data ingestion";

        IntegrationFlow flow = new IntegrationFlow(
                id,
                componentName,
                bsoCode,
                role,
                method,
                frequency,
                purpose);

        assertEquals(id, flow.getId());
        assertEquals(componentName, flow.getComponentName());
        assertEquals(bsoCode, flow.getCounterpartSystemCode());
        assertEquals(role, flow.getCounterpartSystemRole());
        assertEquals(method, flow.getIntegrationMethod());
        assertEquals(frequency, flow.getFrequency());
        assertEquals(purpose, flow.getPurpose());
    }

    @Test
    void shouldThrowExceptionWhenNullForNonNullFields() {
        String id = UUID.randomUUID().toString();

        // counterpartSystemCode null
        assertThrows(NullPointerException.class, () -> new IntegrationFlow(
                id,
                "MyComponent",
                null,
                CounterpartSystemRole.CONSUMER,
                IntegrationMethod.API,
                Frequency.DAILY,
                "Purpose"));

        // externalSystemRole null
        assertThrows(NullPointerException.class, () -> new IntegrationFlow(
                id,
                "MyComponent",
                "BSO-002",
                null,
                IntegrationMethod.API,
                Frequency.DAILY,
                "Purpose"));

        // integrationMethod null
        assertThrows(NullPointerException.class, () -> new IntegrationFlow(
                id,
                "MyComponent",
                "BSO-002",
                CounterpartSystemRole.PRODUCER,
                null,
                Frequency.DAILY,
                "Purpose"));

        // frequency null
        assertThrows(NullPointerException.class, () -> new IntegrationFlow(
                id,
                "MyComponent",
                "BSO-002",
                CounterpartSystemRole.PRODUCER,
                IntegrationMethod.API,
                null,
                "Purpose"));

        // purpose null
        assertThrows(NullPointerException.class, () -> new IntegrationFlow(
                id,
                "MyComponent",
                "BSO-002",
                CounterpartSystemRole.PRODUCER,
                IntegrationMethod.API,
                Frequency.DAILY,
                null));
    }

    @Test
    void testEqualsAndHashCode() {
        String id = UUID.randomUUID().toString();
        String componentName = "BillingService";
        String bsoCode = "BSO-002";
        CounterpartSystemRole role = CounterpartSystemRole.PRODUCER;
        IntegrationMethod method = IntegrationMethod.BATCH;
        Frequency frequency = Frequency.WEEKLY;
        String purpose = "Report generation";

        IntegrationFlow f1 = new IntegrationFlow(
                id, componentName, bsoCode, role, method, frequency, purpose);

        IntegrationFlow f2 = new IntegrationFlow(
                id, componentName, bsoCode, role, method, frequency, purpose);

        assertEquals(f1, f2);
        assertEquals(f1.hashCode(), f2.hashCode());
    }

    @Test
    void testToStringContainsKeyFields() {
        IntegrationFlow flow = new IntegrationFlow(
                "flow-id",
                "NotificationService",
                "BSO-003",
                CounterpartSystemRole.CONSUMER,
                IntegrationMethod.EVENT,
                Frequency.MONTHLY,
                "Batch processing");

        String toString = flow.toString();
        assertTrue(toString.contains("flow-id"));
        assertTrue(toString.contains("NotificationService"));
        assertTrue(toString.contains("BSO-003"));
        assertTrue(toString.contains("CONSUMER"));
        assertTrue(toString.contains("EVENT"));
    }
}