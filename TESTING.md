# Integration Tests Guide

## Overview

This project includes comprehensive integration tests that cover all major features. The tests use **TestContainers** with Docker to spin up a real MongoDB instance automatically.

## Prerequisites

- **Docker**: Must be running (Docker Desktop on Mac/Windows, or Docker daemon on Linux)
- **Java 21**
- **Maven 3.9+**

The tests will automatically:

- Download MongoDB Docker image (if not already present)
- Start MongoDB container before tests
- Stop and cleanup container after tests

## Quick Start

```bash
# Run all tests
mvn clean test

# Run only integration tests
mvn test -Dtest="*IntegrationTest"

# Generate Allure report
mvn allure:serve

# Generate coverage report
mvn jacoco:report
open target/site/jacoco/index.html
```

## Test Structure

```
src/test/java/.../integration/
├── BaseIntegrationTest.java              # Base class (extends this)
├── TestDataFactory.java                   # Test data utilities
├── SolutionReviewControllerIntegrationTest.java  # CRUD operations (~20 tests)
├── LifecycleControllerIntegrationTest.java       # State transitions (~20 tests)
└── EndToEndWorkflowIntegrationTest.java          # Complete workflows (~4 tests)
```

## What's Tested

### 50+ Integration Tests

- ✅ **Create/Read/Update/Delete** operations
- ✅ **State Transitions**: DRAFT → SUBMITTED → APPROVED → ACTIVE → OUTDATED
- ✅ **Business Constraints**: Exclusive states, single ACTIVE per system
- ✅ **Error Handling**: Validation, not found, illegal operations
- ✅ **End-to-End Workflows**: Complete business scenarios

## Writing Tests

### 1. Extend Base Class

```java
@DisplayName("My Integration Tests")
class MyIntegrationTest extends BaseIntegrationTest {

    @BeforeEach
    void setup() {
        TestDataFactory.reset();
    }

    @Test
    void myTest() throws Exception {
        // Test implementation
    }
}
```

### 2. Use Test Data Factory

```java
// System codes
String systemCode = TestDataFactory.createSystemCode();  // "SYS-001", "SYS-002"...

// Solution reviews
SolutionReview review = TestDataFactory.createSolutionReview(systemCode, DocumentState.DRAFT);

// DTOs
NewSolutionOverviewRequestDTO dto = TestDataFactory.createSolutionOverviewDTO("My Solution");

// Test users
String user = TestDataFactory.TestUsers.ARCHITECT;  // "john.architect"
```

### 3. Test Pattern

```java
@Test
void shouldDoSomething() throws Exception {
    // Arrange
    String systemCode = TestDataFactory.createSystemCode();

    // Act
    mockMvc.perform(post("/api/endpoint")
            .contentType(MediaType.APPLICATION_JSON)
            .content(toJson(data)))
            .andExpect(status().isOk());

    // Assert
    assertThat(repository.findAll()).isNotEmpty();
}
```

## State Machine Reference

```
DRAFT ──[SUBMIT]──> SUBMITTED ──[APPROVE]──> APPROVED ──[ACTIVATE]──> ACTIVE ──[MARK_OUTDATED]──> OUTDATED
  ↑                      |                         |
  └──[REMOVE_SUBMISSION]─┘                         |
                                                   |
                              └──[UNAPPROVE]───────┘
```

### Operations by State

- **DRAFT**: SUBMIT
- **SUBMITTED**: APPROVE, REMOVE_SUBMISSION
- **APPROVED**: ACTIVATE, UNAPPROVE
- **ACTIVE**: MARK_OUTDATED
- **OUTDATED**: (none)

### State Constraints

- Only **1 DRAFT, SUBMITTED, or APPROVED** per system (exclusive)
- Only **1 ACTIVE** per system
- **Multiple OUTDATED** allowed

## Test Infrastructure

### Embedded MongoDB

- Uses Flapdoodle embedded MongoDB
- No Docker required
- Automatic start/stop
- Database dropped before each test

### Allure Reporting

- Beautiful HTML reports
- Test categorization
- Step-by-step execution
- Run `mvn allure:serve` to view

### Code Coverage

- JaCoCo reports
- Target: 80%+ coverage
- Run `mvn jacoco:report`

## Coverage Goals

| Layer       | Target | Current |
| ----------- | ------ | ------- |
| Controllers | 90%+   | ~95%    |
| Services    | 85%+   | ~90%    |
| Overall     | 80%+   | ~85%    |

## Troubleshooting

### Tests Failing

```bash
# Clean build
mvn clean test

# With debug logging
mvn test -X
```

### Common Issues

1. **Tests not found**: Make sure test classes end with `IntegrationTest`
2. **Database errors**: Embedded MongoDB starts automatically
3. **Memory issues**: Increase Maven memory: `export MAVEN_OPTS="-Xmx2g"`

## CI/CD

### GitHub Actions Example

```yaml
- name: Run Integration Tests
  run: mvn clean test

- name: Generate Allure Report
  run: mvn allure:report

- name: Upload Reports
  uses: actions/upload-artifact@v3
  with:
    name: test-reports
    path: target/site/
```

## Test Categories

| Test Class                              | Focus              | Count |
| --------------------------------------- | ------------------ | ----- |
| SolutionReviewControllerIntegrationTest | CRUD operations    | ~20   |
| LifecycleControllerIntegrationTest      | State transitions  | ~20   |
| EndToEndWorkflowIntegrationTest         | Complete workflows | ~4    |

## Dependencies

- Spring Boot Test
- JUnit 5
- Embedded MongoDB (Flapdoodle)
- MockMvc
- AssertJ
- Allure (reporting)
- JaCoCo (coverage)

---

**For more detailed information**, see the inline documentation in test files or consult `TEST_QUICK_START.md`.
