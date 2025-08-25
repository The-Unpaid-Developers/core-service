package com.project.core_service.models.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuditLogMeta class.
 * Tests constructors, helper methods, and utility functions.
 */
class AuditLogMetaTest {

    private static final String SOLUTION_REVIEW_ID = "sr-001";
    private static final String SYSTEM_CODE = "systemCode";
    private static final String HEAD_NODE_ID = "node-001";
    private static final String NEW_HEAD_ID = "node-002";

    @Test
    @DisplayName("Should create AuditLogMeta with default constructor")
    void shouldCreateAuditLogMetaWithDefaultConstructor() {
        AuditLogMeta meta = new AuditLogMeta();

        assertNotNull(meta);
        assertNull(meta.getId());
        assertNull(meta.getHead());
        assertNull(meta.getTail());
        assertNull(meta.getCreatedAt());
        assertNull(meta.getLastModified());
        assertEquals(0, meta.getNodeCount());
    }

    @Test
    @DisplayName("Should create AuditLogMeta with all args constructor")
    void shouldCreateAuditLogMetaWithAllArgsConstructor() {
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime lastModified = LocalDateTime.now();

        AuditLogMeta meta = new AuditLogMeta(
                SOLUTION_REVIEW_ID,
                SYSTEM_CODE,
                HEAD_NODE_ID,
                "tail-001",
                createdAt,
                lastModified,
                3);

        assertEquals(SOLUTION_REVIEW_ID, meta.getId());
        assertEquals(HEAD_NODE_ID, meta.getHead());
        assertEquals("tail-001", meta.getTail());
        assertEquals(createdAt, meta.getCreatedAt());
        assertEquals(lastModified, meta.getLastModified());
        assertEquals(3, meta.getNodeCount());
    }

    @Test
    @DisplayName("Should create AuditLogMeta with specialized constructor")
    void shouldCreateAuditLogMetaWithSpecializedConstructor() {
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        AuditLogMeta meta = new AuditLogMeta(HEAD_NODE_ID, SYSTEM_CODE);

        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        assertEquals(HEAD_NODE_ID, meta.getHead());
        assertEquals(HEAD_NODE_ID, meta.getTail()); // Initially head and tail are the same
        assertEquals(1, meta.getNodeCount());
        assertNotNull(meta.getCreatedAt());
        assertNotNull(meta.getLastModified());
        assertTrue(meta.getCreatedAt().isAfter(beforeCreation));
        assertTrue(meta.getCreatedAt().isBefore(afterCreation));
        assertTrue(meta.getLastModified().isAfter(beforeCreation));
        assertTrue(meta.getLastModified().isBefore(afterCreation));
    }

    @Test
    @DisplayName("Should generate unique ID automatically in specialized constructor")
    void shouldGenerateUniqueIdAutomaticallyInSpecializedConstructor() {
        AuditLogMeta meta = new AuditLogMeta(HEAD_NODE_ID, SYSTEM_CODE);

        // Verify ID is not null and follows expected pattern
        assertNotNull(meta.getId());
        assertTrue(meta.getId().startsWith(SYSTEM_CODE + "_audit_"));

        // Verify ID contains timestamp
        String[] idParts = meta.getId().split("_");
        assertEquals(3, idParts.length);
        assertEquals(SYSTEM_CODE, idParts[0]);
        assertEquals("audit", idParts[1]);

        // Verify timestamp part is numeric
        assertDoesNotThrow(() -> Long.parseLong(idParts[2]));
    }

    @Test
    @DisplayName("Should generate different IDs for different system codes")
    void shouldGenerateDifferentIdsForDifferentSystemCodes() {
        AuditLogMeta meta1 = new AuditLogMeta(HEAD_NODE_ID, "SYS-001");
        AuditLogMeta meta2 = new AuditLogMeta(HEAD_NODE_ID, "SYS-002");

        assertNotNull(meta1.getId());
        assertNotNull(meta2.getId());
        assertNotEquals(meta1.getId(), meta2.getId());

        assertTrue(meta1.getId().startsWith("SYS-001_audit_"));
        assertTrue(meta2.getId().startsWith("SYS-002_audit_"));
    }

    @Test
    @DisplayName("Should generate different IDs for same system code at different times")
    void shouldGenerateDifferentIdsForSameSystemCodeAtDifferentTimes() throws InterruptedException {
        AuditLogMeta meta1 = new AuditLogMeta(HEAD_NODE_ID, SYSTEM_CODE);

        // Small delay to ensure different timestamps
        Thread.sleep(2);

        AuditLogMeta meta2 = new AuditLogMeta(HEAD_NODE_ID, SYSTEM_CODE);

        assertNotNull(meta1.getId());
        assertNotNull(meta2.getId());
        assertNotEquals(meta1.getId(), meta2.getId());

        // Both should start with same system code
        assertTrue(meta1.getId().startsWith(SYSTEM_CODE + "_audit_"));
        assertTrue(meta2.getId().startsWith(SYSTEM_CODE + "_audit_"));
    }

    @Test
    @DisplayName("Should add new head correctly")
    void shouldAddNewHeadCorrectly() {
        AuditLogMeta meta = new AuditLogMeta(HEAD_NODE_ID, SYSTEM_CODE);
        LocalDateTime originalLastModified = meta.getLastModified();
        int originalNodeCount = meta.getNodeCount();
        String originalTail = meta.getTail();

        meta.addNewHead(NEW_HEAD_ID);

        assertEquals(NEW_HEAD_ID, meta.getHead());
        assertEquals(originalTail, meta.getTail()); // Tail should remain the same
        assertEquals(originalNodeCount + 1, meta.getNodeCount());
        assertTrue(meta.getLastModified().isAfter(originalLastModified));
    }

    @Test
    @DisplayName("Should return false for isEmpty when meta has head and count > 0")
    void shouldReturnFalseForIsEmptyWhenMetaHasHeadAndCountGreaterThanZero() {
        AuditLogMeta meta = new AuditLogMeta(HEAD_NODE_ID, SYSTEM_CODE);

        assertFalse(meta.isEmpty());
    }

    @Test
    @DisplayName("Should return true for isEmpty when head is null")
    void shouldReturnTrueForIsEmptyWhenHeadIsNull() {
        AuditLogMeta meta = new AuditLogMeta();
        meta.setHead(null);
        meta.setNodeCount(1);

        assertTrue(meta.isEmpty());
    }

    @Test
    @DisplayName("Should return true for isEmpty when nodeCount is 0")
    void shouldReturnTrueForIsEmptyWhenNodeCountIsZero() {
        AuditLogMeta meta = new AuditLogMeta();
        meta.setHead(HEAD_NODE_ID);
        meta.setNodeCount(0);

        assertTrue(meta.isEmpty());
    }

    @Test
    @DisplayName("Should return true for hasOnlyOneVersion when nodeCount is 1")
    void shouldReturnTrueForHasOnlyOneVersionWhenNodeCountIsOne() {
        AuditLogMeta meta = new AuditLogMeta(HEAD_NODE_ID, SYSTEM_CODE);

        assertTrue(meta.hasOnlyOneVersion());
    }

    @Test
    @DisplayName("Should return false for hasOnlyOneVersion when nodeCount is not 1")
    void shouldReturnFalseForHasOnlyOneVersionWhenNodeCountIsNotOne() {
        AuditLogMeta meta = new AuditLogMeta(HEAD_NODE_ID, SYSTEM_CODE);
        meta.addNewHead(NEW_HEAD_ID);

        assertFalse(meta.hasOnlyOneVersion());
    }

    @Test
    @DisplayName("Should handle multiple head additions correctly")
    void shouldHandleMultipleHeadAdditionsCorrectly() {
        AuditLogMeta meta = new AuditLogMeta(HEAD_NODE_ID, SYSTEM_CODE);
        String originalTail = meta.getTail();

        meta.addNewHead("node-002");
        meta.addNewHead("node-003");
        meta.addNewHead("node-004");

        assertEquals("node-004", meta.getHead());
        assertEquals(originalTail, meta.getTail()); // Original tail should remain
        assertEquals(4, meta.getNodeCount());
        assertFalse(meta.isEmpty());
        assertFalse(meta.hasOnlyOneVersion());
    }

    @Test
    @DisplayName("Should handle setters and getters correctly")
    void shouldHandleSettersAndGettersCorrectly() {
        AuditLogMeta meta = new AuditLogMeta();
        LocalDateTime testTime = LocalDateTime.now();

        meta.setId("test-id");
        meta.setHead("test-head");
        meta.setTail("test-tail");
        meta.setCreatedAt(testTime);
        meta.setLastModified(testTime);
        meta.setNodeCount(5);

        assertEquals("test-id", meta.getId());
        assertEquals("test-head", meta.getHead());
        assertEquals("test-tail", meta.getTail());
        assertEquals(testTime, meta.getCreatedAt());
        assertEquals(testTime, meta.getLastModified());
        assertEquals(5, meta.getNodeCount());
    }

    @Test
    @DisplayName("Should handle toString correctly")
    void shouldHandleToStringCorrectly() {
        AuditLogMeta meta = new AuditLogMeta(HEAD_NODE_ID, SYSTEM_CODE);

        String result = meta.toString();

        assertNotNull(result);
        assertTrue(result.contains("AuditLogMeta"));
    }

    @Test
    @DisplayName("Should handle equals and hashCode correctly")
    void shouldHandleEqualsAndHashCodeCorrectly() {
        LocalDateTime testTime = LocalDateTime.now();

        AuditLogMeta meta1 = new AuditLogMeta(
                SOLUTION_REVIEW_ID,
                SYSTEM_CODE,
                HEAD_NODE_ID,
                "tail-001",
                testTime,
                testTime,
                1);

        AuditLogMeta meta2 = new AuditLogMeta(
                SOLUTION_REVIEW_ID,
                SYSTEM_CODE,
                HEAD_NODE_ID,
                "tail-001",
                testTime,
                testTime,
                1);

        assertEquals(meta1, meta2);
        assertEquals(meta1.hashCode(), meta2.hashCode());

        // Test inequality
        meta2.setNodeCount(2);
        assertNotEquals(meta1, meta2);
    }
}
