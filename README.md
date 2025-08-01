# RecruTech - Job Application Management System

## Overview
RecruTech is a comprehensive job application management system built with a microservice architecture. The platform enables companies to post job listings and applicants to submit and track their applications.

## Architecture
The system is currently composed of the following services:

| Service               | Port | Function                                                  |
|-----------------------|------|-----------------------------------------------------------|
| **MySQL Database**    | 3306 | Main application database                                 |
| **MinIO Object Storage** | 9000/9001 | File storage for resumes and documents                |
| **Auth Service**      | 8082 | User authentication, JWT tokens, Roles (APPLICANT, HR, ADMIN) |
| **Platform Service**  | 8080 | Job listings, applications management, main business logic |

### Current Implementation Status
- ✅ MySQL Database - Fully operational
- ✅ MinIO Object Storage - Fully operational  
- ✅ Auth Service - Fully operational with JWT authentication
- ✅ Platform Service - Core functionality implemented
- 🚧 Additional microservices (API Gateway, Notification Service) - Planned for future releases

## Authentication
- User roles:
  - `APPLICANT`: Submit & view applications
  - `HR`: Post jobs, manage applications
  - `ADMIN`: All permissions
- Authentication via JWT (Bearer Token) through custom Auth Service
- Auth endpoints:
  - Register: `POST /api/auth/register`
  - Login: `POST /api/auth/login`
  - Refresh Token: `POST /api/auth/refresh`

## Data Model
The core entities include:
- **Job**: Contains job details like title, description, location
- **Application**: Links users to jobs and their CV files
- **ApplicationStatus**: Tracks the status of applications (RECEIVED, VIEWED, etc.)

## Getting Started

### Prerequisites
- Docker and Docker Compose
- Java 21 (for local development)
- Maven (for local development)

### Quick Start (Recommended)
1. Clone the repository:
   ```bash
   git clone https://github.com/ckc-efehan/recrutech-backend.git
   cd recrutech-backend
   ```

2. Start the infrastructure services (MySQL & MinIO):
   ```bash
   cd recrutech-services
   docker-compose up -d
   ```

3. Start the Auth Service:
   ```bash
   cd recrutech-auth
   docker-compose up -d
   ```

4. Start the Platform Service:
   - Either run it from your IDE (recommended for development)
   - Or build and run with Maven:
     ```bash
     cd recrutech-platform
     mvn spring-boot:run
     ```

   This will make the services available at:
   - Platform Service: http://localhost:8080
   - Auth Service: http://localhost:8082/api
   - MinIO Console: http://localhost:9001 (login: minioadmin/minioadmin)

### Running Locally (Without Docker)
If you prefer to run the services without Docker:

1. Ensure you have Java 21 and Maven installed
2. Set up MySQL databases (one for auth service on port 3307, one for platform service on port 3306)
3. Set up MinIO object storage
4. Configure the application.properties files in each service with your database connection details
5. Build and run each service:
   ```bash
   # Build all services
   mvn clean install
   
   # Run Auth Service (in one terminal)
   cd recrutech-services/recrutech-auth
   mvn spring-boot:run
   
   # Run Platform Service (in another terminal)
   cd recrutech-services/recrutech-platform
   mvn spring-boot:run
   ```

**Note:** Running with Docker is strongly recommended as it handles all the infrastructure setup automatically.

## API Endpoints

### Auth Service (Port 8082)
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Login and get JWT token
- `POST /api/auth/refresh` - Refresh JWT token

### Platform Service (Port 8080)
#### Job Management
- `GET /api/v1/jobs` - List all jobs
- `GET /api/v1/jobs/{jobId}` - View a specific job
- `POST /api/v1/jobs` - Create a job (HR role required)
- `PUT /api/v1/jobs/{jobId}` - Update a job (HR role required)
- `DELETE /api/v1/jobs/{jobId}` - Delete a job (Admin role required)

#### Application Management
- `POST /api/v1/jobs/{jobId}/applications` - Submit an application
- `GET /api/v1/applications/me` - View your applications (Applicant role)
- `GET /api/v1/jobs/{jobId}/applications` - View applications for a job (HR role)
- `PUT /api/v1/applications/{applicationId}/status` - Update application status (HR role)

#### File Management
- File upload and download endpoints for CV/resume management

## Development

### Project Structure
```
recrutech-backend/
├── recrutech-services/
│   ├── recrutech-common/       # Shared utilities and base classes
│   ├── recrutech-auth/         # Authentication service
│   │   ├── src/main/java/      # Auth service source code
│   │   ├── docker-compose.yml  # Auth service Docker config
│   │   └── README.md           # Auth service documentation
│   ├── recrutech-platform/     # Main platform service
│   │   ├── src/main/java/      # Platform service source code
│   │   │   ├── controller/     # REST controllers
│   │   │   ├── service/        # Business logic
│   │   │   ├── repository/     # Data access
│   │   │   ├── model/          # Entity classes
│   │   │   └── dto/            # Data transfer objects
│   │   └── README.md           # Platform service documentation
│   ├── docker-compose.yml      # Infrastructure services (MySQL, MinIO)
│   └── README.md               # Services overview documentation
├── README.md                   # This file - main project documentation
└── projectInstruction.md       # Project specifications
```

### Service-Specific Documentation
For detailed setup and configuration instructions for each service, refer to:
- [Infrastructure Services (MySQL, MinIO)](recrutech-services/README.md)
- [Auth Service](recrutech-services/recrutech-auth/README.md)
- [Platform Service](recrutech-services/recrutech-platform/README.md)

### Database Migrations
The project uses Liquibase for database schema migrations. Migration files are located in:
```
recrutech-services/recrutech-platform/src/main/resources/META-INF/
```

## Contributing
1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License
This project is licensed under the MIT License - see the LICENSE file for details.