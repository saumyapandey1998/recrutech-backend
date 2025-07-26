# Storage Service

The Storage Service provides functionality for uploading, retrieving, and managing files, particularly PDF resumes for job applications. It uses MinIO, an object storage service compatible with Amazon S3 API, for storing files.

## API Endpoints

### Upload a File

```
POST /storage/files
```

**Request:**
- Content-Type: multipart/form-data
- Body: file (MultipartFile)

**Response:**
```json
{
  "fileId": "123e4567-e89b-12d3-a456-426614174000",
  "fileName": "lebenslauf.pdf",
  "contentType": "application/pdf",
  "size": 245678
}
```

### Download a File

```
GET /storage/files/{fileId}
```

**Response:**
- Content-Type: application/pdf (or the original file's content type)
- Content-Disposition: attachment; filename="original_filename.pdf"
- Body: The file content

## Integration with Application Service

The Storage Service is designed to work with the Application Service for job applications. The typical workflow is:

1. Upload a PDF resume to the Storage Service
2. Receive a fileId from the Storage Service
3. Submit a job application to the Application Service, including the fileId as cvFileId

Example:

```json
POST /api/v1/jobs/{jobId}/applications
{
  "cvFileId": "123e4567-e89b-12d3-a456-426614174000"
}
```

The Application Service validates that the cvFileId is a valid UUID but does not check if the file actually exists. This allows for a clean separation of concerns between the services.

## Configuration

The Storage Service can be configured in `application.properties`:

```properties
# File Upload Configuration
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# MinIO Configuration
minio.endpoint=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket-name=recrutech-files
```

- `spring.servlet.multipart.max-file-size`: The maximum size of a single file
- `spring.servlet.multipart.max-request-size`: The maximum size of a request
- `minio.endpoint`: The URL of the MinIO server
- `minio.access-key`: The access key for MinIO authentication
- `minio.secret-key`: The secret key for MinIO authentication
- `minio.bucket-name`: The name of the bucket to store files in

## MinIO Setup

To use the Storage Service, you need to have a MinIO server running. You can run MinIO using Docker:

```bash
docker run -p 9000:9000 -p 9001:9001 --name minio \
  -e "MINIO_ROOT_USER=minioadmin" \
  -e "MINIO_ROOT_PASSWORD=minioadmin" \
  -v /path/to/data:/data \
  minio/minio server /data --console-address ":9001"
```

This will start a MinIO server with the default credentials (minioadmin/minioadmin) and expose the API on port 9000 and the web console on port 9001.

You can access the MinIO web console at http://localhost:9001 to manage buckets and objects.
