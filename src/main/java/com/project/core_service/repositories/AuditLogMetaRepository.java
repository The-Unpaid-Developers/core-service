package com.project.core_service.repositories;

import com.project.core_service.models.audit.AuditLogMeta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link AuditLogMeta} entities.
 * 
 * Provides CRUD operations for managing audit log metadata in MongoDB.
 */
@Repository
public interface AuditLogMetaRepository extends MongoRepository<AuditLogMeta, String> {
}
