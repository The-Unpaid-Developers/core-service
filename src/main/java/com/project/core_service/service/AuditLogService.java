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
     * @param solutionReview    the SR to add to audit log
     * @param changeDescription description of what changed
     */
    public void addSolutionReviewToAuditLog(AuditLogMeta auditLogMeta, SolutionReview solutionReview,
            String changeDescription, String srVersion) {
        // Only allow add to audit log if SR is CURRENT or OUTDATED
        if (solutionReview.getDocumentState() != DocumentState.CURRENT &&
                solutionReview.getDocumentState() != DocumentState.OUTDATED) {
            return;
        }

        // Create new audit log node
        AuditLogNode newNode = new AuditLogNode(solutionReview.getId(), changeDescription, srVersion);
        auditLogNodeRepository.save(newNode);

        auditLogMeta.addNewHead(newNode.getId());
        auditLogMetaRepository.save(auditLogMeta);

        log.info("Added SR {} to audit log with change: {}", solutionReview.getId(), changeDescription);
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
