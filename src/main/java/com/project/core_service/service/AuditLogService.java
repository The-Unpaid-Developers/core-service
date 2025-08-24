package com.project.core_service.service;

import com.project.core_service.models.audit.AuditLogMeta;
import com.project.core_service.models.audit.AuditLogNode;
import com.project.core_service.models.solutions_review.DocumentState;
import com.project.core_service.models.solutions_review.SolutionReview;
import com.project.core_service.repositories.AuditLogMetaRepository;
import com.project.core_service.repositories.AuditLogNodeRepository;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuditLogService {

    private final AuditLogMetaRepository auditLogMetaRepository;
    private final AuditLogNodeRepository auditLogNodeRepository;

    public AuditLogService(AuditLogMetaRepository auditLogMetaRepository,
            AuditLogNodeRepository auditLogNodeRepository) {
        this.auditLogMetaRepository = auditLogMetaRepository;
        this.auditLogNodeRepository = auditLogNodeRepository;
    }

    public void createAuditLogMeta(AuditLogMeta auditLogMeta) {
        auditLogMetaRepository.save(auditLogMeta);
    }

    public AuditLogMeta getAuditLogMeta(String systemCode) {
        return auditLogMetaRepository.findById(systemCode).orElse(null);
    }

    public AuditLogNode getAuditLogNode(String id) {
        return auditLogNodeRepository.findById(id).orElse(null);
    }

    /**
     * Adds a SolutionReview to the audit log when it becomes CURRENT or OUTDATED.
     * 
     * @param auditLogMeta      the audit log meta to update
     * @param solutionReview    the SR to add to audit log
     * @param changeDescription description of what changed
     * @param srVersion         the version of the solution review
     * @throws IllegalArgumentException if the solution review is not in a valid
     *                                  state for audit logging
     */
    public void addSolutionReviewToAuditLog(AuditLogMeta auditLogMeta, SolutionReview solutionReview,
            String changeDescription, String srVersion) {
        if (auditLogMeta == null || solutionReview == null) {
            log.warn("Cannot add to audit log: auditLogMeta or solutionReview is null");
            return;
        }

        DocumentState currentState = solutionReview.getDocumentState();

        // Only allow add to audit log if SR is CURRENT or OUTDATED
        if (currentState != DocumentState.CURRENT && currentState != DocumentState.OUTDATED) {
            log.debug("Solution review {} not added to audit log - state is {} (must be CURRENT or OUTDATED)",
                    solutionReview.getId(), currentState);
            return;
        }

        // Create new audit log node with the current state captured
        AuditLogNode newNode = new AuditLogNode(solutionReview.getId(), changeDescription, srVersion);

        try {
            // Save node first to ensure we don't have orphaned references
            auditLogNodeRepository.save(newNode);

            // Update audit log meta only after successful node creation
            auditLogMeta.addNewHead(newNode.getId());
            auditLogMetaRepository.save(auditLogMeta);

            log.info("Added SR {} to audit log with change: {} (state: {})",
                    solutionReview.getId(), changeDescription, currentState);
        } catch (Exception e) {
            log.error("Failed to add SR {} to audit log, attempting cleanup", solutionReview.getId(), e);
            // Attempt to clean up the orphaned node if meta update failed
            try {
                auditLogNodeRepository.delete(newNode);
            } catch (Exception cleanupException) {
                log.error("Failed to cleanup orphaned audit log node {}", newNode.getId(), cleanupException);
            }
            throw new RuntimeException("Failed to add solution review to audit log", e);
        }
    }

    public void updateAuditLogNode(AuditLogNode auditLogNode, String changeDescription) {
        auditLogNode.setTimestamp(LocalDateTime.now());
        auditLogNode.setChangeDescription(changeDescription);
        auditLogNodeRepository.save(auditLogNode);
    }

    public void updateAuditLogMeta(AuditLogMeta auditLogMeta) {
        auditLogMetaRepository.save(auditLogMeta);
    }
}
