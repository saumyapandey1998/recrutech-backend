# RecruTech Services

This directory contains the Docker configuration for running the RecruTech platform services.

## Services

The docker-compose.yml file in this directory configures the following services:

1. **MySQL Database** - Stores the application data
2. **MinIO Object Storage** - Handles file storage
3. **RecruTech Platform Application** - The main application service

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
   - Build and start the platform application on port 8080
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

Once started, the services will be available at:
- Platform Application: http://localhost:8080
- MinIO Console: http://localhost:9001 (login with minioadmin/minioadmin)

## Configuration

The following environment variables are set in the docker-compose.yml file:
- SPRING_DATASOURCE_URL: Database connection URL
- SPRING_DATASOURCE_USERNAME: Database username
- SPRING_DATASOURCE_PASSWORD: Database password
- SPRING_APPLICATION_JSON: MinIO configuration

## Troubleshooting

If you encounter issues:

1. Check the logs:
   ```
   docker-compose logs app
   docker-compose logs mysql
   docker-compose logs minio
   ```

2. Verify all services are running:
   ```
   docker-compose ps
   ```

3. Check if the ports are available:
   ```
   netstat -an | findstr "3306 8080 9000 9001"
   ```

4. If the application fails to connect to MySQL, you may need to wait a bit longer for MySQL to initialize fully.

5. If you need to rebuild the application:
   ```
   docker-compose build --no-cache app
   docker-compose up -d
   ```

## Running with Auth Service

If you also need to run the auth service, you can do so by navigating to the recrutech-auth directory and running its docker-compose file:

```
cd recrutech-auth
docker-compose up -d
```

The auth service will be available at http://localhost:8082/api and uses a separate MySQL instance on port 3307.