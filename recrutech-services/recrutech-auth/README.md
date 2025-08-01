# RecruTech Auth Service

The RecruTech Auth Service handles user authentication and authorization for the RecruTech platform. It provides JWT-based authentication and manages user roles for the entire system.

## Architecture

This service is part of the RecruTech microservice architecture:
- **Auth Service** (Port 8082): User authentication and JWT tokens (this service)
- **Platform Service** (Port 8080): Main business logic
- **MySQL Database** (Port 3307): Auth service data persistence
- **Infrastructure Services**: MySQL (3306) and MinIO for the Platform Service

## User Roles

The Auth Service manages the following user roles:
- **APPLICANT**: Can submit and view their own job applications
- **HR**: Can post jobs and manage applications for their company
- **ADMIN**: Has all permissions across the system

## Docker Setup

This service can be run using Docker Compose for easy deployment and development.

### Prerequisites

- Docker and Docker Compose installed on your system
- Java 21 and Maven (if building locally)

### Running with Docker Compose

1. Navigate to the auth service directory:
   ```
   cd recrutech-services/recrutech-auth
   ```

2. Start the Docker containers:
   ```
   docker-compose up -d
   ```

   This will:
   - Start a MySQL database on port 3307
   - Build and start the auth service on port 8082
   - Connect the services using a Docker network

3. To view logs:
   ```
   docker-compose logs -f
   ```

4. To stop the containers:
   ```
   docker-compose down
   ```

### Accessing the Service

Once started, the auth service will be available at:
- http://localhost:8082/api

### Authentication Endpoints

The Auth Service provides the following REST endpoints:

#### Register User
- **POST** `http://localhost:8082/api/auth/register`
- **Description**: Register a new user account
- **Request Body**:
  ```json
  {
    "username": "john.doe",
    "email": "john.doe@example.com",
    "password": "securePassword123",
    "role": "APPLICANT"
  }
  ```
- **Response**: User details and JWT token

#### Login
- **POST** `http://localhost:8082/api/auth/login`
- **Description**: Authenticate user and receive JWT token
- **Request Body**:
  ```json
  {
    "username": "john.doe",
    "password": "securePassword123"
  }
  ```
- **Response**: JWT access token and refresh token

#### Refresh Token
- **POST** `http://localhost:8082/api/auth/refresh`
- **Description**: Get a new access token using refresh token
- **Request Body**:
  ```json
  {
    "refreshToken": "your-refresh-token-here"
  }
  ```
- **Response**: New JWT access token

**Note**: The JWT tokens returned by this service are used to authenticate requests to the Platform Service.

### Configuration

The following environment variables are set in the docker-compose.yml file:
- SPRING_DATASOURCE_URL: Database connection URL
- SPRING_DATASOURCE_USERNAME: Database username
- SPRING_DATASOURCE_PASSWORD: Database password
- SERVER_PORT: The port the service runs on

### Running in Debug Mode

The docker-compose.yml file has been modified to allow running only the MySQL service in Docker while running the auth service manually in debug mode. This is useful for development and debugging purposes.

#### Starting Only the MySQL Service

1. Navigate to the auth service directory:
   ```
   cd recrutech-services/recrutech-auth
   ```

2. Start only the MySQL service:
   ```
   docker-compose up -d mysql
   ```

   This will start only the MySQL database on port 3307 without starting the auth service.

#### Running the Auth Service Manually

You can run the auth service manually using one of the following methods:

1. Using your IDE (IntelliJ IDEA, Eclipse, etc.):
   - Open the project in your IDE
   - Create a debug configuration for the `RecrutechAuthApplication` class
   - Set any necessary VM options for debugging
   - Run the application in debug mode

2. Using Maven from the command line:
   ```
   cd recrutech-services/recrutech-auth
   mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
   ```

   This will start the auth service with remote debugging enabled on port 5005.

3. To connect a debugger to the running application:
   - In IntelliJ IDEA: Run > Attach to Process... > Select the process
   - In Eclipse: Run > Debug Configurations > Remote Java Application > Create a new configuration with host=localhost, port=5005

The application.properties file has been updated to connect to the MySQL service running in Docker on port 3307.

### Troubleshooting

If you encounter issues:

1. **Check the logs**:
   ```bash
   # For MySQL service
   docker-compose logs mysql
   
   # For Auth service (if running with Docker)
   docker-compose logs auth-service
   ```

2. **Verify services are running**:
   ```bash
   docker-compose ps
   ```

3. **Check if ports are available**:
   ```bash
   netstat -an | findstr "3307 8082"
   ```

4. **Common Issues**:
   - **MySQL connection failed**: Wait a bit longer for MySQL to initialize fully
   - **Port 8082 already in use**: Stop any other services using this port
   - **JWT token validation errors**: Ensure the Auth Service is running and accessible
   - **Database schema issues**: Check if Liquibase migrations ran successfully

5. **Reset the service**:
   ```bash
   docker-compose down -v
   docker-compose up -d
   ```
   **Warning**: This will delete all user data in the auth database.

## Additional Resources

For complete setup instructions and overall architecture information, see the [main project README](../../README.md).

For Platform Service integration, see the [Platform Service README](../recrutech-platform/README.md).