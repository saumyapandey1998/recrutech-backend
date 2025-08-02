# RecruTech Platform Service

The RecruTech Platform Service is the main business logic service that handles job listings, applications management, and file operations. It works in conjunction with the Auth Service for authentication and uses MinIO for file storage.

## Architecture

This service is part of the RecruTech microservice architecture:
- **Auth Service** (Port 8082): Handles user authentication and JWT tokens
- **Platform Service** (Port 8080): Main business logic (this service)
- **MySQL Database** (Port 3306): Data persistence
- **MinIO Object Storage** (Ports 9000/9001): File storage for resumes and documents

## Prerequisites

- Java 21
- Maven
- Docker and Docker Compose (for infrastructure services)
- Running Auth Service
- Running MySQL database
- Running MinIO object storage

## Setup and Running

### Option 1: Quick Start (Recommended)

1. **Start Infrastructure Services** (from project root):
   ```bash
   cd recrutech-services
   docker-compose up -d
   ```
   This starts MySQL and MinIO services.

2. **Start Auth Service** (from project root):
   ```bash
   cd recrutech-services/recrutech-auth
   docker-compose up -d
   ```

3. **Run Platform Service**:
   - **From IDE** (recommended for development):
     - Open the project in your IDE
     - Run the `RecrutechPlatformApplication` class

   - **From command line**:
     ```bash
     cd recrutech-services/recrutech-platform
     mvn spring-boot:run
     ```

### Option 2: Manual Setup

If you prefer to run everything manually without Docker:

1. Set up MySQL database on port 3306
2. Set up MinIO object storage on ports 9000/9001
3. Configure `application.properties` with your database and MinIO settings
4. Start the Auth Service manually
5. Build and run the Platform Service:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

## Configuration

The service requires the following configuration in `application.properties`:

### Database Configuration
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/recrutech_service?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=user
spring.datasource.password=password
```

### MinIO Configuration
```properties
minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket-name=recrutech-files
```

### Auth Service Integration
The Platform Service communicates with the Auth Service for JWT token validation. Ensure the Auth Service is running on port 8082.

## API Endpoints

Once running, the Platform Service provides the following endpoints:

### Job Management
- `GET /api/v1/jobs` - List all jobs
- `GET /api/v1/jobs/{jobId}` - View a specific job
- `POST /api/v1/jobs` - Create a job (HR role required)
- `PUT /api/v1/jobs/{jobId}` - Update a job (HR role required)
- `DELETE /api/v1/jobs/{jobId}` - Delete a job (Admin role required)

### Application Management
- `POST /api/v1/jobs/{jobId}/applications` - Submit an application
- `GET /api/v1/applications/me` - View your applications (User role)
- `GET /api/v1/jobs/{jobId}/applications` - View applications for a job (HR role)
- `PUT /api/v1/applications/{applicationId}/status` - Update application status (HR role)

### File Management
- File upload and download endpoints for CV/resume management

## Authentication

All endpoints (except job listing) require JWT authentication. Include the JWT token in the Authorization header:
```
Authorization: Bearer <your-jwt-token>
```

Get JWT tokens from the Auth Service:
- Register: `POST http://localhost:8082/api/auth/register`
- Login: `POST http://localhost:8082/api/auth/login`

## Development

### Database Migrations
The service uses Liquibase for database schema migrations. Migration files are located in:
```
src/main/resources/META-INF/
```

### Testing
Run tests with:
```bash
mvn test
```

## Troubleshooting

1. **Service won't start**: Ensure MySQL and Auth Service are running
2. **Database connection issues**: Check MySQL is accessible on port 3306
3. **File upload issues**: Verify MinIO is running and accessible
4. **Authentication errors**: Ensure Auth Service is running on port 8082

For more detailed setup instructions, see the [main project README](../../README.md).
