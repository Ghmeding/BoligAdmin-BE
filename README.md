# BoligAdmin Backend

A microservice backend system for managing residential properties and tenants. This system consists of a Spring Boot REST API for property administration and a Go-based worker service for asynchronous task processing via RabbitMQ.

## Overview

BoligAdmin is part of a larger property management system designed to help property owners manage their real estate portfolio. The backend consists of two main services:

1. **ba_core (Spring Boot)** - REST API service handling property administration and tenant management with JWT-based security. Publishes tasks to RabbitMQ for async processing.

2. **ba_worker (Go)** IN PROGRESS - Worker service that consumes tasks from RabbitMQ and processes them asynchronously. Handles background jobs such as notifications and external integrations.

## Related Repositories

- **Infrastructure**: For the infrastructure setup (AWS), see [BoligAdmin-INFRA](https://github.com/Ghmeding/BoligAdmin-INFRA)

## Acknowledgments

The following books helped shape and design this project:

- [*Cloud Native Spring in Action*](https://www.oreilly.com/library/view/cloud-native-spring/9781617298424/) by Thomas Vitale
- [*Concurrency in Go*](https://www.oreilly.com/library/view/concurrency-in-go/9781491941294/) by Katherine Cox-Buday

## Architecture
The architectural flow follows an event-driven pattern.

```
┌─────────────────┐     ┌─────────────┐     ┌─────────────────┐
│   ba_core       │────▶│  RabbitMQ   │────▶│   ba_worker     │
│  (Spring Boot)  │     │   Queue     │     │     (Go)        │
│   REST API      │     └─────────────┘     │  Task Consumer  │
└─────────────────┘                         └─────────────────┘
        │
        ▼
┌─────────────────┐
│   PostgreSQL    │
│    Database     │
└─────────────────┘
```

## Tech Stack

### ba_core (Spring Boot Service)
- **Framework**: Spring Boot 3.5.7
- **Language**: Java 21
- **Database**: PostgreSQL
- **Security**: Spring Security with JWT (OAuth2 Resource Server)
- **Build Tool**: Gradle
- **ORM**: Spring Data JPA / Hibernate
- **Authentication**: JWT (JJWT 0.11.5)
- **Message Queue**: Spring AMQP (RabbitMQ)
- **Utilities**: Lombok

### ba_worker (Go Service)
- **Language**: Go 1.21+

## Project Structure

```
services/
├── ba_core/                               # Spring Boot REST API
│   └── src/main/java/ba/core/
│       ├── App.java                       # Spring Boot application entry point
│       ├── config/
│       │   ├── JwtAuthenticationFilter.java   # JWT token validation and extraction
│       │   ├── SecurityConfiguration.java     # Spring Security configuration
│       │   └── RabbitMQConfig.java            # RabbitMQ connection and queue setup
│       ├── controller/
│       │   ├── PropertyController.java        # Property management endpoints
│       │   ├── TenantController.java          # Tenant management endpoints
│       │   └── HealthController.java          # Health check endpoint
│       ├── dto/
│       │   ├── CreatePropertyDTO.java         # DTO for property creation
│       │   ├── CreateTenantDTO.java           # DTO for tenant creation
│       │   └── PropertyDTO.java               # DTO for property responses
│       ├── exception/
│       │   └── GlobalExceptionHandler.java    # Centralized exception handling
│       ├── mapper/                            # Entity-DTO mappers
│       ├── models/
│       │   ├── PropertyEntity.java            # Property entity
│       │   └── TenantEntity.java              # Tenant entity
│       ├── mq/
│       │   ├── TaskPublisher.java             # RabbitMQ message publisher
│       │   └── TaskMessage.java               # Task message payload model
│       ├── repository/
│       │   ├── PropertyRepository.java        # Property data access
│       │   └── TenantRepository.java          # Tenant data access
│       └── service/
│           ├── PropertyService.java           # Property business logic
│           └── TenantService.java             # Tenant business logic
│
```

## Key Features

### Property Management
- **Retrieve Owner Properties**: Fetch all properties owned by an authenticated user
- Endpoint: `GET /property/getAllOwnerProperties`
- Requires JWT authentication

### Tenant Management
- **Create Tenant**: Add a new tenant to the system
- **Manage Tenant Information**: Handle tenant data and associations with properties
- Requires JWT authentication

### Health Check
- **Health Endpoint**: Verify service is running
- Endpoint: `GET /health` (via HealthController)

## API Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|----------------|
| GET | `/property/getAllOwnerProperties` | Get all properties for logged-in owner | Yes |
| POST | `/tenant` | Create a new tenant | Yes |
| GET | `/health` | Health check | No |

## Message Queue (RabbitMQ)

The system uses RabbitMQ for asynchronous task processing between the Spring Boot service and the Go worker.

### Task Flow
1. **ba_core** receives API requests and performs immediate operations
2. For async tasks, **ba_core** publishes messages to RabbitMQ queues
3. **ba_worker** consumes messages and processes them in the background


## Security

The application uses **Spring Security** with **JWT (JSON Web Tokens)** for authentication and authorization:

- All endpoints (except health check) require a valid JWT token in the Authorization header
- JWT tokens are validated by `JwtAuthenticationFilter`
- Owner ID is extracted from the JWT subject claim
- Token validation is configured in `SecurityConfiguration.java`

### Configuration
- Security configuration is defined in `SecurityConfiguration.java`
- JWT tokens are validated against the configured secret key
- OAuth2 Resource Server pattern for stateless authentication

## Database Models

### Property
- `id` (UUID): Unique identifier
- `ownerId` (String): Owner/user ID
- `title` (String): Property title
- `description` (String): Property details
- `createdAt` (LocalDateTime): Creation timestamp
- `updatedAt` (LocalDateTime): Last update timestamp
- Relationship: One-to-many with Tenant

### Tenant
- `id` (UUID): Unique identifier
- `propertyId` (UUID): Associated property
- Tenant-specific information and contact details
- Relationship: Many-to-one with Property

## Configuration

### Environment Variables

#### ba_core (Spring Boot)
```properties
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/boligadmin
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=your_password

# Security
JWT_SECRET_KEY=your_jwt_secret_key

# RabbitMQ
SPRING_RABBITMQ_HOST=localhost
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest
```

### Application Properties
- **Server Port**: `8080` (default)
- **Database Dialect**: PostgreSQL
- **JPA DDL**: `update` (auto-update schema)
- **Transaction Management**: Enabled

## Getting Started

### Prerequisites
- Java 21+
- PostgreSQL database
- Gradle (or use ./gradlew)

### Quick Start with Docker Compose
```bash
# Start all services (PostgreSQL, RabbitMQ, ba_core, ba_worker)
docker-compose up -d
```

### Manual Setup

#### 1. Start RabbitMQ
```bash
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:management
```
Management UI available at `http://localhost:15672` (guest/guest)

#### 2. Build and Run ba_core
```bash
cd services/ba_core
./gradlew build
./gradlew bootRun
```
The API will start on `http://localhost:8080`


### Test
```bash
# Test ba_core
cd services/ba_core
./gradlew test

## Dependencies

### ba_core (Spring Boot)
Key Spring Boot starters:
- `spring-boot-starter-web` - Web and REST support
- `spring-boot-starter-data-jpa` - Database access
- `spring-boot-starter-security` - Security framework
- `spring-boot-starter-oauth2-resource-server` - OAuth2 resource server
- `spring-boot-starter-amqp` - RabbitMQ integration
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` - JWT token handling
- `postgresql` - PostgreSQL database driver
- `lombok` - Code generation for getters/setters/constructors

## Development Notes

### Known TODOs
- Owner validation: Verify that the authenticated user is the owner of the property before returning tenants

### Future Enhancements
- Add property update/delete endpoints
- Add tenant update/delete endpoints
- Implement comprehensive validation
- Add pagination for list endpoints
- Add filtering and search capabilities
- Implement task scheduling for recurring jobs
- Add metrics and monitoring for worker service
- Implement circuit breaker for RabbitMQ connection

## License

This project is part of the BoligAdmin ecosystem.

## Author

Ghmeding
