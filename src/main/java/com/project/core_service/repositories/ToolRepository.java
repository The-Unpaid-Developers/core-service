package com.project.core_service.repositories;

import com.project.core_service.models.enterprise_tools.Tool;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository interface for {@link Tool} entities.
 *
 * <p>Extends {@link MongoRepository} to provide CRUD operations and query methods
 * for managing tools in a MongoDB database.</p>
 */
public interface ToolRepository extends MongoRepository<Tool, String> {
}
