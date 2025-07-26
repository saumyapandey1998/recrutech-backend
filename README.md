# RecruTech - Job Application Management System

## Overview
RecruTech is a comprehensive job application management system built with a microservice architecture. The platform enables companies to post job listings and applicants to submit and track their applications.

## Architecture
The system is composed of the following microservices:

| Service               | Function                                                  |
|-----------------------|-----------------------------------------------------------|
| **API Gateway**       | Routes requests to services, Authentication via Keycloak  |
| **Keycloak**          | User management, Login, Roles (APPLICANT, HR, ADMIN)      |
| **Job-Service**       | Management of job listings                                |
| **Application-Service**| Manage applications, set status, link CVs                 |
| **Storage-Service**   | Store resumes (PDF)                                       |
| **Notification-Service** | Send emails for applications & status changes (Event-based) |

## Authentication
- User roles:
  - `APPLICANT`: Submit & view applications
  - `HR`: Post jobs, manage applications
  - `ADMIN`: All permissions
- Authentication via JWT (Bearer Token) through Keycloak

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

2. Start the application with Docker:
   ```bash
   cd recrutech-services
   docker-compose up -d
   ```

   This will:
   - Build the application inside Docker
   - Start all necessary containers (MySQL and the application)
   - Make the application available at http://localhost:8080

### Running Locally (Without Docker)
If you prefer to run the application without Docker:

1. Ensure you have Java 21 and Maven installed
2. Set up a MySQL database
3. Configure the application.properties file with your database connection details
4. Build and run the application:
   ```bash
   mvn clean install
   java -jar recrutech-services/recrutech-platform/target/recrutech-platform-0.0.1-SNAPSHOT.jar
   ```

## API Endpoints

### Job-Service
- `GET /api/v1/jobs` - List all jobs
- `GET /api/v1/jobs/{jobId}` - View a specific job
- `POST /api/v1/jobs` - Create a job (HR role)
- `PUT /api/v1/jobs/{jobId}` - Update a job
- `DELETE /api/v1/jobs/{jobId}` - Delete a job (Admin role)

### Application-Service (Coming Soon)
- `POST /jobs/{jobId}/applications` - Submit an application
- `GET /applications/me` - View all your applications
- `GET /jobs/{jobId}/applications` - View all applications for a job (HR)
- And more...

## Development

### Project Structure
```
recrutech-backend/
├── recrutech-services/
│   ├── recrutech-common/       # Shared utilities and base classes
│   ├── recrutech-platform/     # Main application service
│   │   ├── controller/         # REST controllers
│   │   ├── service/            # Business logic
│   │   ├── repository/         # Data access
│   │   ├── model/              # Entity classes
│   │   └── dto/                # Data transfer objects
│   └── docker-compose.yml      # Docker configuration
└── projectInstruction.md       # Project specifications
```

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