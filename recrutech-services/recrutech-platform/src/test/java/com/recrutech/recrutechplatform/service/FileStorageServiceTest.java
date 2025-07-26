package com.recrutech.recrutechplatform.service;

import com.recrutech.common.exception.NotFoundException;
import com.recrutech.common.exception.ValidationException;
import com.recrutech.recrutechplatform.config.MinioConfig;
import com.recrutech.recrutechplatform.model.FileMetadata;
import com.recrutech.recrutechplatform.repository.FileMetadataRepository;
import io.minio.*;
import io.minio.http.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileStorageServiceTest {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioConfig minioConfig;

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    private FileStorageService fileStorageService;

    private String bucketName;
    private String fileId;
    private FileMetadata fileMetadata;
    private MultipartFile multipartFile;

    @BeforeEach
    void setUp() throws Exception {
        // Setup test data
        bucketName = "test-bucket";
        fileId = UUID.randomUUID().toString();

        // Setup MinioConfig mock
        when(minioConfig.getBucketName()).thenReturn(bucketName);

        // Setup bucket exists check
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        // Create FileStorageService instance after mocks are set up
        fileStorageService = new FileStorageService(minioClient, minioConfig, fileMetadataRepository);

        // Setup file metadata
        fileMetadata = new FileMetadata();
        fileMetadata.setId(fileId);
        fileMetadata.setFileName("test.pdf");
        fileMetadata.setContentType("application/pdf");
        fileMetadata.setSize(1024L);
        fileMetadata.setFilePath("test-path/test.pdf");

        // Setup multipart file
        multipartFile = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "test content".getBytes()
        );
    }

    @Test
    void storeFile_Success() throws Exception {
        // Arrange
        when(fileMetadataRepository.save(any(FileMetadata.class))).thenReturn(fileMetadata);

        // Act
        FileMetadata result = fileStorageService.storeFile(multipartFile);

        // Assert
        assertNotNull(result);
        assertEquals(fileId, result.getId());
        assertEquals("test.pdf", result.getFileName());
        assertEquals("application/pdf", result.getContentType());
        assertEquals(1024L, result.getSize());

        verify(minioClient).putObject(any(PutObjectArgs.class));
        verify(fileMetadataRepository).save(any(FileMetadata.class));
    }

    @Test
    void storeFile_NullFile() throws Exception {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            fileStorageService.storeFile(null);
        });

        assertEquals("File cannot be null", exception.getMessage());
        verify(minioClient, never()).putObject(any(PutObjectArgs.class));
        verify(fileMetadataRepository, never()).save(any(FileMetadata.class));
    }

    @Test
    void storeFile_EmptyFile() throws Exception {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            fileStorageService.storeFile(emptyFile);
        });

        assertEquals("File cannot be empty", exception.getMessage());
        verify(minioClient, never()).putObject(any(PutObjectArgs.class));
        verify(fileMetadataRepository, never()).save(any(FileMetadata.class));
    }

    @Test
    void loadFileAsResource_Success() throws Exception {
        // Arrange
        GetObjectResponse getObjectResponse = mock(GetObjectResponse.class);

        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.of(fileMetadata));
        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(getObjectResponse);

        // Act
        Resource resource = fileStorageService.loadFileAsResource(fileId);

        // Assert
        assertNotNull(resource);
        verify(fileMetadataRepository).findById(fileId);
        verify(minioClient).getObject(any(GetObjectArgs.class));
    }

    @Test
    void loadFileAsResource_FileNotFound() throws Exception {
        // Arrange
        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fileStorageService.loadFileAsResource(fileId);
        });

        assertTrue(exception.getMessage().contains("Could not load file with id: " + fileId));
        assertTrue(exception.getCause() instanceof NotFoundException);
        verify(fileMetadataRepository).findById(fileId);
        verify(minioClient, never()).getObject(any(GetObjectArgs.class));
    }

    @Test
    void getFileMetadata_Success() {
        // Arrange
        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.of(fileMetadata));

        // Act
        FileMetadata result = fileStorageService.getFileMetadata(fileId);

        // Assert
        assertNotNull(result);
        assertEquals(fileId, result.getId());
        assertEquals("test.pdf", result.getFileName());
        assertEquals("application/pdf", result.getContentType());
        assertEquals(1024L, result.getSize());

        verify(fileMetadataRepository).findById(fileId);
    }

    @Test
    void getFileMetadata_FileNotFound() {
        // Arrange
        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            fileStorageService.getFileMetadata(fileId);
        });

        assertEquals("File not found with id: " + fileId, exception.getMessage());
        verify(fileMetadataRepository).findById(fileId);
    }

    @Test
    void generatePresignedUrl_Success() throws Exception {
        // Arrange
        String presignedUrl = "https://minio.example.com/test-bucket/test-path/test.pdf?X-Amz-Algorithm=AWS4-HMAC-SHA256&...";

        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.of(fileMetadata));
        when(minioClient.getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class))).thenReturn(presignedUrl);

        // Act
        String result = fileStorageService.generatePresignedUrl(fileId, 3600);

        // Assert
        assertEquals(presignedUrl, result);
        verify(fileMetadataRepository).findById(fileId);
        verify(minioClient).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }

    @Test
    void generatePresignedUrl_FileNotFound() throws Exception {
        // Arrange
        when(fileMetadataRepository.findById(fileId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            fileStorageService.generatePresignedUrl(fileId, 3600);
        });

        assertTrue(exception.getMessage().contains("Could not generate presigned URL for file with id: " + fileId));
        assertTrue(exception.getCause() instanceof NotFoundException);
        verify(fileMetadataRepository).findById(fileId);
        verify(minioClient, never()).getPresignedObjectUrl(any(GetPresignedObjectUrlArgs.class));
    }
}
