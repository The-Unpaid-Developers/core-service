package com.project.core_service.models.audit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for AuditLogNode class.
 * Tests constructors and utility methods.
 */
class AuditLogNodeTest {

    private static final String SOLUTION_REVIEW_ID = "sr-001";
    private static final String CHANGE_DESCRIPTION = "Initial version created";
    private static final String SOLUTIONS_REVIEW_VERSION = "v1.0";
    private static final String NEXT_NODE_ID = "node-002";

    @Test
    @DisplayName("Should create AuditLogNode with default constructor")
    void shouldCreateAuditLogNodeWithDefaultConstructor() {
        AuditLogNode node = new AuditLogNode();

        assertNotNull(node);
        assertNull(node.getId());
        assertNull(node.getSolutionReviewId());
        assertNull(node.getSolutionsReviewVersion());
        assertNull(node.getNext());
        assertNull(node.getTimestamp());
        assertNull(node.getChangeDescription());
    }

    @Test
    @DisplayName("Should create AuditLogNode with all args constructor")
    void shouldCreateAuditLogNodeWithAllArgsConstructor() {
        LocalDateTime timestamp = LocalDateTime.now();

        AuditLogNode node = new AuditLogNode(
                "node-001",
                SOLUTION_REVIEW_ID,
                SOLUTIONS_REVIEW_VERSION,
                NEXT_NODE_ID,
                timestamp,
                CHANGE_DESCRIPTION);

        assertEquals("node-001", node.getId());
        assertEquals(SOLUTION_REVIEW_ID, node.getSolutionReviewId());
        assertEquals(SOLUTIONS_REVIEW_VERSION, node.getSolutionsReviewVersion());
        assertEquals(NEXT_NODE_ID, node.getNext());
        assertEquals(timestamp, node.getTimestamp());
        assertEquals(CHANGE_DESCRIPTION, node.getChangeDescription());
    }

    @Test
    @DisplayName("Should create AuditLogNode with three-parameter constructor")
    void shouldCreateAuditLogNodeWithThreeParameterConstructor() {
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        AuditLogNode node = new AuditLogNode(
                SOLUTION_REVIEW_ID,
                CHANGE_DESCRIPTION,
                SOLUTIONS_REVIEW_VERSION);

        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        assertEquals(SOLUTION_REVIEW_ID, node.getSolutionReviewId());
        assertEquals(CHANGE_DESCRIPTION, node.getChangeDescription());
        assertEquals(SOLUTIONS_REVIEW_VERSION, node.getSolutionsReviewVersion());
        assertNull(node.getNext()); // Should be null by default
        assertNotNull(node.getTimestamp());
        assertTrue(node.getTimestamp().isAfter(beforeCreation));
        assertTrue(node.getTimestamp().isBefore(afterCreation));
    }

    @Test
    @DisplayName("Should create AuditLogNode with four-parameter constructor")
    void shouldCreateAuditLogNodeWithFourParameterConstructor() {
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        AuditLogNode node = new AuditLogNode(
                SOLUTION_REVIEW_ID,
                CHANGE_DESCRIPTION,
                SOLUTIONS_REVIEW_VERSION,
                NEXT_NODE_ID);

        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        assertEquals(SOLUTION_REVIEW_ID, node.getSolutionReviewId());
        assertEquals(CHANGE_DESCRIPTION, node.getChangeDescription());
        assertEquals(SOLUTIONS_REVIEW_VERSION, node.getSolutionsReviewVersion());
        assertEquals(NEXT_NODE_ID, node.getNext());
        assertNotNull(node.getTimestamp());
        assertTrue(node.getTimestamp().isAfter(beforeCreation));
        assertTrue(node.getTimestamp().isBefore(afterCreation));
    }

    @Test
    @DisplayName("Should return true for isTail when next is null")
    void shouldReturnTrueForIsTailWhenNextIsNull() {
        AuditLogNode node = new AuditLogNode(
                SOLUTION_REVIEW_ID,
                CHANGE_DESCRIPTION,
                SOLUTIONS_REVIEW_VERSION);

        assertTrue(node.isTail());
        assertFalse(node.hasNext());
    }

    @Test
    @DisplayName("Should return false for isTail when next is not null")
    void shouldReturnFalseForIsTailWhenNextIsNotNull() {
        AuditLogNode node = new AuditLogNode(
                SOLUTION_REVIEW_ID,
                CHANGE_DESCRIPTION,
                SOLUTIONS_REVIEW_VERSION,
                NEXT_NODE_ID);

        assertFalse(node.isTail());
        assertTrue(node.hasNext());
    }

    @Test
    @DisplayName("Should return true for hasNext when next is not null")
    void shouldReturnTrueForHasNextWhenNextIsNotNull() {
        AuditLogNode node = new AuditLogNode();
        node.setNext(NEXT_NODE_ID);

        assertTrue(node.hasNext());
        assertFalse(node.isTail());
    }

    @Test
    @DisplayName("Should return false for hasNext when next is null")
    void shouldReturnFalseForHasNextWhenNextIsNull() {
        AuditLogNode node = new AuditLogNode();
        node.setNext(null);

        assertFalse(node.hasNext());
        assertTrue(node.isTail());
    }

    @Test
    @DisplayName("Should handle setters and getters correctly")
    void shouldHandleSettersAndGettersCorrectly() {
        AuditLogNode node = new AuditLogNode();
        LocalDateTime testTime = LocalDateTime.now();

        node.setId("test-id");
        node.setSolutionReviewId(SOLUTION_REVIEW_ID);
        node.setSolutionsReviewVersion(SOLUTIONS_REVIEW_VERSION);
        node.setNext(NEXT_NODE_ID);
        node.setTimestamp(testTime);
        node.setChangeDescription(CHANGE_DESCRIPTION);

        assertEquals("test-id", node.getId());
        assertEquals(SOLUTION_REVIEW_ID, node.getSolutionReviewId());
        assertEquals(SOLUTIONS_REVIEW_VERSION, node.getSolutionsReviewVersion());
        assertEquals(NEXT_NODE_ID, node.getNext());
        assertEquals(testTime, node.getTimestamp());
        assertEquals(CHANGE_DESCRIPTION, node.getChangeDescription());
    }

    @Test
    @DisplayName("Should handle toString correctly")
    void shouldHandleToStringCorrectly() {
        AuditLogNode node = new AuditLogNode(
                SOLUTION_REVIEW_ID,
                CHANGE_DESCRIPTION,
                SOLUTIONS_REVIEW_VERSION);

        String result = node.toString();

        assertNotNull(result);
        assertTrue(result.contains("AuditLogNode"));
    }

    @Test
    @DisplayName("Should handle equals and hashCode correctly")
    void shouldHandleEqualsAndHashCodeCorrectly() {
        LocalDateTime testTime = LocalDateTime.now();

        AuditLogNode node1 = new AuditLogNode(
                "node-001",
                SOLUTION_REVIEW_ID,
                SOLUTIONS_REVIEW_VERSION,
                NEXT_NODE_ID,
                testTime,
                CHANGE_DESCRIPTION);

        AuditLogNode node2 = new AuditLogNode(
                "node-001",
                SOLUTION_REVIEW_ID,
                SOLUTIONS_REVIEW_VERSION,
                NEXT_NODE_ID,
                testTime,
                CHANGE_DESCRIPTION);

        assertEquals(node1, node2);
        assertEquals(node1.hashCode(), node2.hashCode());

        // Test inequality
        node2.setChangeDescription("Different description");
        assertNotEquals(node1, node2);
    }

    @Test
    @DisplayName("Should handle edge cases for utility methods")
    void shouldHandleEdgeCasesForUtilityMethods() {
        AuditLogNode node = new AuditLogNode();

        // Test with null next
        node.setNext(null);
        assertTrue(node.isTail());
        assertFalse(node.hasNext());

        // Test with empty string next
        node.setNext("");
        assertFalse(node.isTail());
        assertTrue(node.hasNext());

        // Test with whitespace next
        node.setNext("   ");
        assertFalse(node.isTail());
        assertTrue(node.hasNext());
    }

    @Test
    @DisplayName("Should maintain consistency between isTail and hasNext")
    void shouldMaintainConsistencyBetweenIsTailAndHasNext() {
        AuditLogNode node = new AuditLogNode();

        // When next is null
        node.setNext(null);
        assertEquals(node.isTail(), !node.hasNext());

        // When next is not null
        node.setNext("some-id");
        assertEquals(node.isTail(), !node.hasNext());

        // Multiple state changes
        for (int i = 0; i < 5; i++) {
            node.setNext(i % 2 == 0 ? null : "next-" + i);
            assertEquals(node.isTail(), !node.hasNext());
        }
    }

    @Test
    @DisplayName("Should handle timestamp generation in constructors")
    void shouldHandleTimestampGenerationInConstructors() {
        LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);

        // Test three-parameter constructor
        AuditLogNode node1 = new AuditLogNode(
                SOLUTION_REVIEW_ID,
                CHANGE_DESCRIPTION,
                SOLUTIONS_REVIEW_VERSION);

        // Test four-parameter constructor
        AuditLogNode node2 = new AuditLogNode(
                SOLUTION_REVIEW_ID,
                CHANGE_DESCRIPTION,
                SOLUTIONS_REVIEW_VERSION,
                NEXT_NODE_ID);

        LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);

        // Both should have timestamps set
        assertNotNull(node1.getTimestamp());
        assertNotNull(node2.getTimestamp());

        // Both timestamps should be within the expected range
        assertTrue(node1.getTimestamp().isAfter(beforeCreation));
        assertTrue(node1.getTimestamp().isBefore(afterCreation));
        assertTrue(node2.getTimestamp().isAfter(beforeCreation));
        assertTrue(node2.getTimestamp().isBefore(afterCreation));

        // Timestamps should be very close but node2 should be slightly later
        assertTrue(node2.getTimestamp().isAfter(node1.getTimestamp()) ||
                node2.getTimestamp().isEqual(node1.getTimestamp()));
    }

    @Test
    @DisplayName("Should handle empty values gracefully")
    void shouldHandleEmptyValuesGracefully() {
        AuditLogNode node = new AuditLogNode();

        // Test with null for next (allowed)
        node.setNext(null);
        assertNull(node.getNext());
        assertTrue(node.isTail());
        assertFalse(node.hasNext());

        // Test with empty strings (for @NonNull fields, we can't set null)
        node.setSolutionReviewId("");
        node.setSolutionsReviewVersion("");
        node.setChangeDescription("");
        node.setNext("");

        assertEquals("", node.getSolutionReviewId());
        assertEquals("", node.getSolutionsReviewVersion());
        assertEquals("", node.getChangeDescription());
        assertEquals("", node.getNext());
        assertFalse(node.isTail()); // Empty string is not null
        assertTrue(node.hasNext()); // Empty string is not null
    }

    @Test
    @DisplayName("Should demonstrate linked list behavior")
    void shouldDemonstratLinkedListBehavior() {
        // Create a chain of nodes to simulate audit log
        AuditLogNode tailNode = new AuditLogNode(
                SOLUTION_REVIEW_ID,
                "Initial creation",
                "v1.0");
        tailNode.setId("tail-node-id");

        AuditLogNode middleNode = new AuditLogNode(
                SOLUTION_REVIEW_ID,
                "Updated description",
                "v2.0",
                "tail-node-id");
        middleNode.setId("middle-node-id");

        AuditLogNode headNode = new AuditLogNode(
                SOLUTION_REVIEW_ID,
                "Final revision",
                "v3.0",
                "middle-node-id");
        headNode.setId("head-node-id");

        // Verify tail node (no next reference)
        assertTrue(tailNode.isTail());
        assertFalse(tailNode.hasNext());
        assertNull(tailNode.getNext());

        // Verify middle node (has next reference)
        assertFalse(middleNode.isTail());
        assertTrue(middleNode.hasNext());
        assertNotNull(middleNode.getNext());
        assertEquals("tail-node-id", middleNode.getNext());

        // Verify head node (has next reference)
        assertFalse(headNode.isTail());
        assertTrue(headNode.hasNext());
        assertNotNull(headNode.getNext());
        assertEquals("middle-node-id", headNode.getNext());

        // Verify complete chain
        assertEquals("middle-node-id", headNode.getNext());
        assertEquals("tail-node-id", middleNode.getNext());
        assertNull(tailNode.getNext());
    }
}
