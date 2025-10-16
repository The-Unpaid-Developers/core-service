package com.project.core_service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Step;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Base class for integration tests using TestContainers for MongoDB.
 * 
 * <p>
 * This class provides:
 * <ul>
 * <li>MongoDB TestContainer setup and lifecycle management</li>
 * <li>Spring Boot application context with all components</li>
 * <li>MockMvc for HTTP request testing</li>
 * <li>Database cleanup between tests</li>
 * <li>Common utilities for integration testing</li>
 * </ul>
 * 
 * <p>
 * Test classes extending this base class will have access to:
 * <ul>
 * <li>{@link MockMvc} for API testing</li>
 * <li>{@link MongoTemplate} for database verification</li>
 * <li>{@link ObjectMapper} for JSON serialization</li>
 * </ul>
 * 
 * <p>
 * Usage:
 * 
 * <pre>
 * {@literal @}DisplayName("My Integration Tests")
 * class MyIntegrationTest extends BaseIntegrationTest {
 *     
 *     {@literal @}Test
 *     void testSomething() throws Exception {
 *         // Test implementation
 *     }
 * }
 * </pre>
 * 
 * @see SpringBootTest
 * @see MongoDBContainer
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false) // Disable security for integration tests
public abstract class BaseIntegrationTest {

    /**
     * Shared MongoDB container using the official MongoDB Docker image.
     * The container is started once and shared across all test classes.
     * This singleton pattern prevents container recreation between test classes.
     */
    protected static final MongoDBContainer mongoDBContainer = createMongoDBContainer();

    /**
     * Creates and starts the shared MongoDB container.
     * 
     * @return Started MongoDB container
     */
    @SuppressWarnings("resource") // Container lifecycle managed manually for test suite
    private static MongoDBContainer createMongoDBContainer() {
        MongoDBContainer container = new MongoDBContainer(DockerImageName.parse("mongo:7.0"))
                .withExposedPorts(27017);
        container.start();
        return container;
    }

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    /**
     * Helper method to create and save a complete SolutionReview with all
     * dependencies.
     * This handles saving @DBRef entities in the correct order.
     * 
     * @param systemCode the system code
     * @param state      the document state
     * @return saved SolutionReview with all dependencies persisted
     */
    protected com.project.core_service.models.solutions_review.SolutionReview createAndSaveSolutionReview(
            String systemCode,
            com.project.core_service.models.solutions_review.DocumentState state) {

        // Create and save SolutionOverview first (it's a @DBRef)
        com.project.core_service.models.solution_overview.SolutionOverview overview = TestDataFactory
                .createSolutionOverview("Test Solution for " + systemCode);
        overview = mongoTemplate.save(overview);

        // Create SolutionReview with the saved overview
        com.project.core_service.models.solutions_review.SolutionReview review = TestDataFactory
                .createSolutionReviewWithOverview(systemCode, state, overview);

        // Save and return
        return mongoTemplate.save(review);
    }

    /**
     * Configures the MongoDB connection string dynamically based on the
     * TestContainer.
     *
     * @param registry the dynamic property registry
     */
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
        registry.add("mongodb.connection.lookups.string", mongoDBContainer::getReplicaSetUrl);
    }

    /**
     * Cleans up the database before each test to ensure test isolation.
     * Drops all collections (except system collections).
     */
    @BeforeEach
    @Step("Clean up database before test")
    public void cleanUpDatabase() {
        try {
            // Drop all collections except system collections
            mongoTemplate.getCollectionNames().forEach(collectionName -> {
                if (!collectionName.startsWith("system.")) {
                    try {
                        mongoTemplate.dropCollection(collectionName);
                    } catch (Exception e) {
                        // Ignore individual collection drop errors
                    }
                }
            });
        } catch (Exception e) {
            // If we can't clean up, log it but continue - the test will still run
            System.err.println("Warning: Could not clean database before test: " + e.getMessage());
        }
    }

    /**
     * Helper method to serialize objects to JSON string.
     * 
     * @param obj the object to serialize
     * @return JSON string representation
     * @throws Exception if serialization fails
     */
    protected String toJson(Object obj) throws Exception {
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * Helper method to deserialize JSON string to object.
     * 
     * @param json  the JSON string
     * @param clazz the target class
     * @param <T>   the type of the target object
     * @return the deserialized object
     * @throws Exception if deserialization fails
     */
    protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
}
