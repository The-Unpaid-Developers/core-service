package com.project.core_service.repositories;

import com.project.core_service.models.solutions_review.DocumentState;
import com.project.core_service.models.solutions_review.SolutionReview;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link SolutionReview} entities.
 *
 * <p>
 * Extends {@link MongoRepository} to provide CRUD operations and query methods
 * for managing solution reviews in a MongoDB database.
 * </p>
 *
 * <p>
 * Includes a custom query method for retrieving reviews by system code with
 * pagination.
 * </p>
 */
@Repository
public interface SolutionReviewRepository extends MongoRepository<SolutionReview, String> {

    /**
     * Retrieves all {@link SolutionReview} entries for a given system code with custom sorting:
     * ACTIVE first, then DRAFT/SUBMITTED/APPROVED, then OUTDATED, all sorted by lastModifiedAt DESC.
     *
     * @param systemCode the system code used for filtering
     * @return a {@link List} of solution reviews sorted by priority and lastModifiedAt
     */
    @Aggregation(pipeline = {
        "{ $match: { 'systemCode': ?0 } }",
        "{ $addFields: { " +
            "'sortPriority': { " +
                "$switch: { " +
                    "branches: [ " +
                        "{ case: { $eq: ['$documentState', 'ACTIVE'] }, then: 1 }, " +
                        "{ case: { $in: ['$documentState', ['DRAFT', 'SUBMITTED', 'APPROVED']] }, then: 2 }, " +
                        "{ case: { $eq: ['$documentState', 'OUTDATED'] }, then: 3 } " +
                    "], " +
                    "default: 4 " +
                "} " +
            "} " +
        "} }",
        "{ $sort: { 'sortPriority': 1, 'lastModifiedAt': -1 } }",
        "{ $project: { 'sortPriority': 0 } }"
    })
    List<SolutionReview> findAllBySystemCode(String systemCode);

    /**
     * Retrieves the first {@link SolutionReview} entry filtered by system code and
     * document state.
     *
     * @param systemCode     the system code used for filtering
     * @param documentStates the list of document states used for filtering
     * @return an {@link Optional} containing the first solution review that matches
     *         the given criteria, or empty if none found
     */
    Optional<SolutionReview> findFirstBySystemCodeAndDocumentStateIn(String systemCode,
            List<DocumentState> documentStates);

    /**
     * Checks if a document exists in exclusive states (DRAFT, SUBMITTED, APPROVED)
     * for the given system code.
     *
     * @param systemCode     the system code to check
     * @param documentStates the exclusive states to check for
     * @return true if any document exists in the specified exclusive states
     */
    boolean existsBySystemCodeAndDocumentStateIn(String systemCode, List<DocumentState> documentStates);

    /**
     * Finds all documents in exclusive states for the given system code.
     *
     * @param systemCode     the system code to check
     * @param documentStates the exclusive states to check for
     * @return list of documents in exclusive states
     */
    List<SolutionReview> findAllBySystemCodeAndDocumentStateIn(String systemCode, List<DocumentState> documentStates);

    /**
     * Counts documents in exclusive states for the given system code.
     *
     * @param systemCode     the system code to check
     * @param documentStates the exclusive states to count
     * @return count of documents in exclusive states
     */
    long countBySystemCodeAndDocumentStateIn(String systemCode, List<DocumentState> documentStates);

    /**
     * Retrieves all distinct system codes.
     *
     * @return a {@link List} of distinct system codes
     */
    @Aggregation(pipeline = {
        "{ $group: { _id: '$systemCode' } }",
        "{ $sort: { _id: 1 } }",
        "{ $project: { _id: 0, systemCode: '$_id' } }"
    })
    List<String> findAllDistinctSystemCodes();

    @Aggregation(pipeline = {
        "{ $group: { _id: '$systemCode' } }",
        "{ $count: 'total' }"
    })
    long countDistinctSystemCodes();

    /**
     * Retrieves the active {@link SolutionReview} for a given system code.
     *
     * @param systemCode the system code used for filtering
     * @return an {@link Optional} containing the active solution review, or empty if none found
     */
    default Optional<SolutionReview> findActiveBySystemCode(String systemCode) {
        return findBySystemCodeAndDocumentState(systemCode, DocumentState.ACTIVE);
    }

     /**
     * Retrieves all {@link SolutionReview} entries for a given system code, sorted by the provided criteria.
     *
     * @param systemCode the system code used for filtering
     * @param sort       the sorting criteria for the results
     * @return a {@link List} of solution reviews for the given system code
     */
    @Query("{ 'systemCode': ?0 }")
    List<SolutionReview> findBySystemCode(String systemCode, Sort sort);

    /**
     * Retrieves all {@link SolutionReview} entries with a specific document state, with pagination.
     *
     * @param documentState the document state used for filtering
     * @param pageable      the pagination information
     * @return a {@link Page} of solution reviews with the specified document state
     */
    Page<SolutionReview> findByDocumentState(DocumentState documentState, Pageable pageable);

    /**
     * Retrieves all {@link SolutionReview} entries with a specific document state.
     *
     * @param documentState the document state used for filtering
     * @return a {@link List} of solution reviews with the specified document state
     */
    List<SolutionReview> findByDocumentState(DocumentState documentState);

    Optional<SolutionReview> findBySystemCodeAndDocumentState(String systemCode, DocumentState documentState);
}
