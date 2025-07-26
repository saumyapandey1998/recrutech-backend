package com.recrutech.recrutechplatform.service;

import com.recrutech.common.exception.NotFoundException;
import com.recrutech.common.exception.ValidationException;
import com.recrutech.recrutechplatform.config.MinioConfig;
import com.recrutech.recrutechplatform.model.FileMetadata;
import com.recrutech.recrutechplatform.repository.FileMetadataRepository;
import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Service for handling file storage operations using MinIO.
 * This service provides methods for uploading, retrieving, and deleting files.
 */
@Service
public class FileStorageService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;
    private final FileMetadataRepository fileMetadataRepository;

    /**
     * Constructor for FileStorageService.
     *
     * @param minioClient MinIO client for object storage operations
     * @param minioConfig MinIO configuration
     * @param fileMetadataRepository repository for file metadata
     */
    public FileStorageService(
            MinioClient minioClient,
            MinioConfig minioConfig,
            FileMetadataRepository fileMetadataRepository) {
        this.minioClient = minioClient;
        this.minioConfig = minioConfig;
        this.fileMetadataRepository = fileMetadataRepository;

        // Ensure bucket exists
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build());

            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing MinIO bucket: " + e.getMessage(), e);
        }
    }

    /**
     * Store a file in MinIO and return its metadata.
     *
     * @param file the file to store
     * @return the metadata of the stored file
     */
    public FileMetadata storeFile(MultipartFile file) {
        // Validate file
        if (file == null) {
            throw new ValidationException("File cannot be null");
        }

        if (file.isEmpty()) {
            throw new ValidationException("File cannot be empty");
        }

        // Normalize file name
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        // Check if the file name contains invalid characters
        if (originalFileName.contains("..")) {
            throw new ValidationException("Filename contains invalid path sequence: " + originalFileName);
        }

        try {
            // Create a unique file name to prevent duplicates
            String fileExtension = "";
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String objectName = System.currentTimeMillis() + "-" + java.util.UUID.randomUUID() + fileExtension;

            // Upload file to MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .contentType(file.getContentType())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build());

            // Create and save file metadata
            FileMetadata fileMetadata = new FileMetadata();
            fileMetadata.setFileName(originalFileName);
            fileMetadata.setContentType(file.getContentType());
            fileMetadata.setSize(file.getSize());
            fileMetadata.setFilePath(objectName);

            return fileMetadataRepository.save(fileMetadata);
        } catch (Exception e) {
            throw new RuntimeException("Could not store file " + originalFileName + ": " + e.getMessage(), e);
        }
    }

    /**
     * Retrieve a file from MinIO as a Resource.
     *
     * @param fileId the ID of the file to retrieve
     * @return the file as a Resource
     */
    public Resource loadFileAsResource(String fileId) {
        try {
            FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                    .orElseThrow(() -> new NotFoundException("File not found with id: " + fileId));

            // Get object from MinIO
            GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(fileMetadata.getFilePath())
                            .build());

            return new InputStreamResource(response);
        } catch (Exception e) {
            throw new RuntimeException("Could not load file with id: " + fileId, e);
        }
    }

    /**
     * Get file metadata by ID.
     *
     * @param fileId the ID of the file
     * @return the file metadata
     */
    public FileMetadata getFileMetadata(String fileId) {
        return fileMetadataRepository.findById(fileId)
                .orElseThrow(() -> new NotFoundException("File not found with id: " + fileId));
    }

    /**
     * Generate a presigned URL for a file.
     * This can be used to provide temporary access to a file.
     *
     * @param fileId the ID of the file
     * @param expiryTime the expiry time in seconds
     * @return the presigned URL
     */
    public String generatePresignedUrl(String fileId, int expiryTime) {
        try {
            FileMetadata fileMetadata = fileMetadataRepository.findById(fileId)
                    .orElseThrow(() -> new NotFoundException("File not found with id: " + fileId));

            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(fileMetadata.getFilePath())
                            .expiry(expiryTime, TimeUnit.SECONDS)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Could not generate presigned URL for file with id: " + fileId, e);
        }
    }
}
