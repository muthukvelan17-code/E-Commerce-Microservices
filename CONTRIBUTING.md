# Contributing to E-Commerce Microservices Platform

Thank you for considering contributing to this project! This document outlines the guidelines and best practices for contributing.

## Table of Contents

- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Code Standards](#code-standards)
- [Commit Conventions](#commit-conventions)
- [Pull Request Process](#pull-request-process)

## Getting Started

### Prerequisites

- **Java 21** (LTS) — Ensure `JAVA_HOME` is configured
- **Maven 3.9.6** — Bundled in `./maven/apache-maven-3.9.6/`
- **Docker & Docker Compose** (optional, for container-based infra)

### Local Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/muthukvelan17-code/E-Commerce-Microservices.git
   cd E-Commerce-Microservices
   ```

2. Build all modules:
   ```bash
   ./maven/apache-maven-3.9.6/bin/mvn clean install -DskipTests
   ```

3. Start infrastructure and services:
   ```powershell
   .\start-all.ps1
   ```

## Development Workflow

1. Create a feature branch from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. Make your changes following the [Code Standards](#code-standards).

3. Run the full build to verify:
   ```bash
   mvn clean install
   ```

4. Submit a Pull Request targeting `main`.

## Code Standards

### Java Style

- Follow **Google Java Style Guide** conventions
- Use **Lombok** annotations to reduce boilerplate (`@Data`, `@Builder`, `@RequiredArgsConstructor`)
- Use **MapStruct** for DTO-to-Entity mapping
- Always add `@Slf4j` logging to service classes
- Use `@Transactional` for write operations in service layer

### API Design

- RESTful endpoints: `/api/v1/{resource}`
- Use proper HTTP status codes (201 for creation, 404 for not found, etc.)
- Validate inputs with `jakarta.validation` annotations
- Document all endpoints with **SpringDoc OpenAPI** annotations

### Event-Driven Patterns

- Define all Avro schemas in `common-dto/src/main/avro/`
- Use the **Choreography Saga Pattern** for cross-service workflows
- Always publish compensating events for rollback scenarios

### Project Structure (per service)

```
service-name/
├── src/main/java/com/ecommerce/servicename/
│   ├── config/          # Configuration classes
│   ├── controller/      # REST controllers
│   ├── dto/             # Data Transfer Objects
│   ├── messaging/       # Kafka producers & consumers
│   ├── model/           # JPA/MongoDB entities
│   ├── repository/      # Data access layer
│   └── service/         # Business logic
└── src/main/resources/
    └── application.yml  # Service configuration
```

## Commit Conventions

We follow [Conventional Commits](https://www.conventionalcommits.org/):

| Prefix   | Usage                                |
|----------|--------------------------------------|
| `feat:`  | New feature                          |
| `fix:`   | Bug fix                              |
| `docs:`  | Documentation changes                |
| `chore:` | Build, CI, or maintenance tasks      |
| `refactor:` | Code restructuring (no behavior change) |
| `test:`  | Adding or fixing tests               |
| `perf:`  | Performance improvements             |

**Examples:**
```
feat: add order cancellation endpoint
fix: resolve inventory race condition on concurrent reservations
docs: update API documentation for payment service
```

## Pull Request Process

1. Ensure your branch is up to date with `main`
2. All modules must compile without errors: `mvn clean install -DskipTests`
3. Include clear PR title and description
4. Reference any related issues
5. Request review from at least one maintainer

---

*For questions, open an issue or contact the maintainers.*
