package com.project.core_service.repositories;

import com.project.core_service.models.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for {@link Query} entities.
 * 
 * <p>
 * Extends {@link MongoRepository} to provide CRUD operations for managing
 * database queries in a MongoDB database.
 * </p>
 */
@Repository
public interface QueryRepository extends MongoRepository<Query, String> {
}
