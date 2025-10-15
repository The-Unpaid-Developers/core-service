# core-service

Core AWG access service.

## Docker Setup

### Building Locally

```bash
docker build -t smithquaz/fyp-core-service:latest .
```

### Running with Docker Compose

```bash
# Using the published image from DockerHub
docker-compose up -d

# Or build locally
docker-compose up -d --build
```

### Environment Variables

The service requires the following environment variable:

- `CONN_STR`: MongoDB connection string (e.g., `mongodb://user:pass@mongodb:27017/solutions?authSource=admin`)

When running locally with docker-compose, you can create a `.env` file:

```bash
CONN_STR=mongodb://user:pass@mongodb:27017/solutions?authSource=admin
```

## CI/CD

### DockerHub Deployment

The project uses GitHub Actions to automatically build and push Docker images to DockerHub.

**Setup Required:**

1. Go to your GitHub repository settings
2. Navigate to Secrets and Variables → Actions
3. Add the following secrets:
   - `DOCKERHUB_USERNAME`: Your DockerHub username
   - `DOCKERHUB_TOKEN`: Your DockerHub access token (create one at https://hub.docker.com/settings/security)

**Triggering:**

- **Automatic**: Push to `main` branch
- **Manual**: Go to Actions → "Build and Push to DockerHub" → Run workflow
- **Tags**: Create a git tag with format `v*.*.*` (e.g., `v1.0.0`)

**Image Tags:**

- `latest`: Latest build from main branch
- `main-<sha>`: Build from main branch with commit SHA
- `v1.0.0`, `1.0`, `1`: Semantic version tags (when using git tags)

### Pulling the Image

```bash
docker pull smithquaz/fyp-core-service:latest
# or specific version
docker pull smithquaz/fyp-core-service:v1.0.0
```

## Kubernetes Deployment

For Kubernetes deployments, environment variables should be configured via ConfigMaps or Secrets:

```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: core-service-config
data:
  CONN_STR: "mongodb://user:pass@mongodb-service:27017/solutions?authSource=admin"
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: core-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: core-service
  template:
    metadata:
      labels:
        app: core-service
    spec:
      containers:
        - name: core-service
          image: smithquaz/fyp-core-service:latest
          ports:
            - containerPort: 8080
          envFrom:
            - configMapRef:
                name: core-service-config
```

## Development

### Prerequisites

- Java 21
- Maven 3.9+
- MongoDB
- Docker (for integration tests with TestContainers)

### Running Tests

#### Quick Start

```bash
# Run all tests (unit + integration)
mvn clean test

# View beautiful Allure test report
mvn allure:serve

# Generate code coverage report
mvn jacoco:report
open target/site/jacoco/index.html
```

#### Integration Tests

This project includes comprehensive integration tests with:

- ✅ 50+ integration tests covering all features
- ✅ TestContainers with Docker (real MongoDB)
- ✅ Allure reporting for beautiful test reports
- ✅ JaCoCo code coverage analysis
- ✅ Complete end-to-end workflow testing

**Prerequisite**: Docker must be running

**Documentation:** See [`TESTING.md`](TESTING.md) for complete guide

**Run specific tests:**

```bash
# Run only integration tests
mvn test -Dtest="*IntegrationTest"

# Run specific test class
mvn test -Dtest=SolutionReviewControllerIntegrationTest
```

### Running Locally

```bash
export CONN_STR=mongodb://localhost:27017/solutions
mvn spring-boot:run
```

## Testing

### Test Coverage

| Layer       | Coverage | Tests                      |
| ----------- | -------- | -------------------------- |
| Controllers | ~95%     | 30+ integration tests      |
| Services    | ~90%     | Via integration tests      |
| Lifecycle   | 100%     | 15+ state transition tests |
| End-to-End  | 100%     | 4 complete workflow tests  |

### Test Documentation

See **[TESTING.md](TESTING.md)** for complete integration testing documentation:

- Quick start commands
- Test structure and patterns
- State machine reference
- Code coverage goals
- CI/CD integration examples
