package com.project.core_service.repositories;

import com.project.core_service.models.process_compliance.ProcessCompliant;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessCompliantRepository extends MongoRepository<ProcessCompliant, String> {
}
