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
@Document(collection = "auditLogNodes")
public class AuditLogNode {
    @Id
    private String id;

    @NonNull
    private String solutionReviewId; // References SolutionReview.id

    @NonNull
    private String solutionsReviewVersion;

    private String next; // References next node's id

    @NonNull
    private LocalDateTime timestamp; // When this version was created

    private String changeDescription; // Description of what changed

    public AuditLogNode(String solutionReviewId, String changeDescription, String solutionsReviewVersion) {
        this.solutionReviewId = solutionReviewId;
        this.solutionsReviewVersion = solutionsReviewVersion;
        this.changeDescription = changeDescription;
        this.timestamp = LocalDateTime.now();
        this.next = null;
    }

    public AuditLogNode(String solutionReviewId, String changeDescription, String solutionsReviewVersion, String next) {
        this.solutionReviewId = solutionReviewId;
        this.solutionsReviewVersion = solutionsReviewVersion;
        this.changeDescription = changeDescription;
        this.timestamp = LocalDateTime.now();
        this.next = next;
    }

    public boolean isTail() {
        return this.next == null;
    }

    public boolean hasNext() {
        return this.next != null;
    }
}
