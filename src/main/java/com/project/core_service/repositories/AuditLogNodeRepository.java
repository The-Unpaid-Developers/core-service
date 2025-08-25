package com.project.core_service.repositories;

import com.project.core_service.models.audit.AuditLogNode;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link AuditLogNode} entities.
 * 
 * Provides CRUD operations and custom queries for managing audit log nodes in
 * MongoDB.
 */
@Repository
public interface AuditLogNodeRepository extends MongoRepository<AuditLogNode, String> {

    /**
     * Finds all audit log nodes for a specific solution review.
     * 
     * @param solutionReviewId the solution review ID
     * @return list of audit log nodes
     */
    List<AuditLogNode> findBySolutionReviewId(String solutionReviewId);

    /**
     * Finds an audit log node by its solution review version.
     * 
     * @param solutionsReviewVersion the version ID
     * @return optional audit log node
     */
    Optional<AuditLogNode> findBySolutionsReviewVersion(String solutionsReviewVersion);
}
