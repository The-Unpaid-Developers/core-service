package com.project.core_service;

import com.project.core_service.integration.BaseIntegrationTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Integration test that verifies the Spring application context loads
 * successfully with all dependencies.
 * Extends BaseIntegrationTest to use Testcontainers for MongoDB.
 * 
 * This test is disabled by default because it requires Docker.
 * Enable it manually if you need to verify application context loading.
 */
@Disabled("Integration test - requires Docker/Testcontainers")
class CoreServiceApplicationTests extends BaseIntegrationTest {

	@Test
	void contextLoads() {
		// This test verifies that the Spring application context loads successfully
		// MongoDB connection is provided by BaseIntegrationTest's Testcontainers setup
	}

}
