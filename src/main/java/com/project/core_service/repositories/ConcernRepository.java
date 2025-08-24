package com.project.core_service.repositories;

import com.project.core_service.models.solution_overview.Concern;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Repository interface for {@link Concern} entities.
 *
 * <p>Extends {@link MongoRepository} to provide CRUD operations and query methods
 * for managing concerns in a MongoDB database.</p>
 */
public interface ConcernRepository extends MongoRepository<Concern, String> {
}
