package com.project.core_service.models.audit;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.mongodb.lang.NonNull;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "auditLogMeta")
public class AuditLogMeta {
    @Id
    private String id; // This should match the original SolutionReview.id

    private String head; // id of the head node (most recent version)

    private String tail; // id of the tail node (original version)

    @NonNull
    private LocalDateTime createdAt; // When the audit log was first created

    private LocalDateTime lastModified; // When the last node was added

    private int nodeCount; // Total number of versions in the linked list

    public AuditLogMeta(String solutionReviewId, String headNodeId) {
        this.id = solutionReviewId;
        this.head = headNodeId;
        this.tail = headNodeId; // Initially head and tail are the same
        this.createdAt = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        this.nodeCount = 1;
    }

    // Helper method to update when a new node is added to the front
    public void addNewHead(String newHeadId) {
        this.head = newHeadId;
        this.lastModified = LocalDateTime.now();
        this.nodeCount++;
    }

    public boolean isEmpty() {
        return this.head == null || this.nodeCount == 0;
    }

    public boolean hasOnlyOneVersion() {
        return this.nodeCount == 1;
    }
}
