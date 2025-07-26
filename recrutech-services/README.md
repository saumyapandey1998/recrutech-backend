# Recrutech Backend Application

This README provides instructions on how to run the Recrutech Backend application in different environments.

## Prerequisites

- Docker and Docker Compose

## Quick Start (Recommended)

To start the application with Docker:

1. Navigate to the recrutech-services directory:
   ```bash
   cd recrutech-services
   ```

2. Start the Docker containers:
   ```bash
   docker-compose up -d
   ```

   This will:
   - Build the application inside Docker (no local Java or Maven required)
   - Start all necessary containers (MySQL and the application)
   - The application will be available at http://localhost:8080

   You can also run in foreground mode to see the logs:
   ```bash
   docker-compose up
   ```

## Alternative Start Options

### Running Locally (Without Docker)

If you prefer to start the application manually without Docker, please refer to [README_MANUAL_START.md](README_MANUAL_START.md).

## Stopping the Application

To stop the Docker containers:
```bash
cd recrutech-services
docker-compose down
```

If you want to remove the MySQL data volume as well:
```bash
cd recrutech-services
docker-compose down -v
```

## Troubleshooting

### Database Connection Issues

If you encounter database connection issues, make sure:

1. MySQL is running and accessible
2. The database name, username, and password match the configuration
3. The hostname is correct:
   - Use `localhost` when running the application directly on your machine
   - Use `mysql` when running with Docker Compose

The application is configured to use `localhost` in the application.properties file, which is suitable for local development. When running with Docker Compose, the environment variables in the docker-compose.yml file override these settings to use the correct hostname within the Docker network.
