# RecuTech Platform

## Docker Setup

This project includes Docker configuration for easy deployment and development.

### Prerequisites

- Docker and Docker Compose installed on your system
- Java 21 and Maven for building the application

### Building and Running

1. Build the application JAR file:
   ```
   mvn clean package
   ```

   > **Important**: This step is mandatory before running Docker Compose because the docker-compose.yml file mounts the JAR file from your local machine into the container. The JAR file must exist before starting the containers.

2. Start the Docker containers:
   ```
   docker-compose up -d
   ```

   This will start:
   - RecuTech Platform application on port 8080
   - MySQL database on port 3306

3. To stop the containers:
   ```
   docker-compose down
   ```

### Environment Configuration

The application is configured to connect to the MySQL database container automatically. 
The following environment variables are set in the docker-compose.yml file:

- `SPRING_DATASOURCE_URL`: jdbc:mysql://mysql:3306/recrutech_service?createDatabaseIfNotExist=true
- `SPRING_DATASOURCE_USERNAME`: user
- `SPRING_DATASOURCE_PASSWORD`: password

If you need to modify these settings, you can update the docker-compose.yml file or override them when running the container.

### Volumes

The MySQL data is persisted using a Docker volume named `mysql-data`. This ensures your data is preserved even if the containers are stopped or removed.
