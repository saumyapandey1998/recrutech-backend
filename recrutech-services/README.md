# RecruTech Infrastructure Services

This directory contains the Docker configuration for running the RecruTech infrastructure services that support the platform.

## Services

The docker-compose.yml file in this directory configures the following infrastructure services:

1. **MySQL Database** - Stores the application data for the Platform Service
2. **MinIO Object Storage** - Handles file storage for resumes and documents

**Note:** The Platform Service and Auth Service are run separately. This docker-compose file only provides the infrastructure services they depend on.

## Running the Services

### Prerequisites

- Docker and Docker Compose installed on your system
- Java 21 and Maven (if building locally)

### Starting the Services

1. Navigate to the recrutech-services directory:
   ```
   cd recrutech-services
   ```

2. Start the Docker containers:
   ```
   docker-compose up -d
   ```

   This will:
   - Start a MySQL database on port 3306
   - Start a MinIO object storage service on ports 9000 and 9001
   - Connect the services using a Docker network

3. To view logs:
   ```
   docker-compose logs -f
   ```

4. To stop the containers:
   ```
   docker-compose down
   ```

### Accessing the Services

Once started, the infrastructure services will be available at:
- MySQL Database: localhost:3306 (user: user, password: password)
- MinIO Console: http://localhost:9001 (login: minioadmin/minioadmin)
- MinIO API: http://localhost:9000

**Note:** The Platform Service (port 8080) and Auth Service (port 8082) need to be started separately. See their respective README files for instructions.

## Configuration

The docker-compose.yml file configures the following services:

### MySQL Database
- **Port:** 3306
- **Database:** recrutech_service
- **Username:** user
- **Password:** password
- **Root Password:** rootpassword

### MinIO Object Storage
- **API Port:** 9000
- **Console Port:** 9001
- **Access Key:** minioadmin
- **Secret Key:** minioadmin

These services are configured to be accessible by the Platform and Auth services running on the host machine.

## Troubleshooting

If you encounter issues with the infrastructure services:

1. Check the logs:
   ```
   docker-compose logs mysql
   docker-compose logs minio
   ```

2. Verify all services are running:
   ```
   docker-compose ps
   ```

3. Check if the ports are available:
   ```
   netstat -an | findstr "3306 9000 9001"
   ```

4. If services fail to start, you may need to wait a bit longer for MySQL to initialize fully.

5. If you need to restart the services:
   ```
   docker-compose down
   docker-compose up -d
   ```

6. To completely reset the services and data:
   ```
   docker-compose down -v
   docker-compose up -d
   ```
   **Warning:** This will delete all data in the MySQL database and MinIO storage.

## Running with Auth Service

If you also need to run the auth service, you can do so by navigating to the recrutech-auth directory and running its docker-compose file:

```
cd recrutech-auth
docker-compose up -d
```

The auth service will be available at http://localhost:8082/api and uses a separate MySQL instance on port 3307.