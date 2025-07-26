package com.recrutech.recrutechplatform.service;

import com.recrutech.common.exception.NotFoundException;
import com.recrutech.recrutechplatform.dto.application.ApplicationRequest;
import com.recrutech.recrutechplatform.dto.application.ApplicationResponse;
import com.recrutech.recrutechplatform.enums.ApplicationStatus;
import com.recrutech.recrutechplatform.model.Application;
import com.recrutech.recrutechplatform.model.Job;
import com.recrutech.recrutechplatform.repository.ApplicationRepository;
import com.recrutech.recrutechplatform.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private ApplicationService applicationService;

    private Job job;
    private String jobId;
    private String cvFileId;
    private ApplicationRequest applicationRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        jobId = UUID.randomUUID().toString();
        cvFileId = UUID.randomUUID().toString();

        job = new Job();
        job.setId(jobId);
        job.setTitle("Software Engineer");
        job.setDescription("Job description");
        job.setLocation("Berlin");

        applicationRequest = new ApplicationRequest();
        applicationRequest.setCvFileId(cvFileId);
    }

    @Test
    void createApplication_Success() {
        // Arrange
        Application savedApplication = new Application();
        savedApplication.setId(UUID.randomUUID().toString());
        savedApplication.setCvFileId(cvFileId);
        savedApplication.setStatus(ApplicationStatus.RECEIVED);
        savedApplication.setViewedByHr(false);
        savedApplication.setJob(job);
        savedApplication.setCreatedAt(LocalDateTime.now());

        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));
        when(applicationRepository.save(any(Application.class))).thenReturn(savedApplication);

        // Act
        ApplicationResponse response = applicationService.createApplication(jobId, applicationRequest);

        // Assert
        assertNotNull(response);
        assertEquals(savedApplication.getId(), response.getId());
        assertEquals(jobId, response.getJobId());
        assertEquals(cvFileId, response.getCvFileId());
        assertEquals(ApplicationStatus.RECEIVED, response.getStatus());
        assertFalse(response.isViewedByHr());
        assertNotNull(response.getCreatedAt());

        verify(jobRepository).findById(jobId);
        verify(applicationRepository).save(any(Application.class));
    }

    @Test
    void createApplication_JobNotFound() {
        // Arrange
        when(jobRepository.findById(jobId)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            applicationService.createApplication(jobId, applicationRequest);
        });

        assertEquals("Job not found with id: " + jobId, exception.getMessage());
        verify(jobRepository).findById(jobId);
        verify(applicationRepository, never()).save(any(Application.class));
    }

    @Test
    void createApplication_InvalidCvFileId() {
        // Arrange
        applicationRequest.setCvFileId("invalid-uuid");
        when(jobRepository.findById(jobId)).thenReturn(Optional.of(job));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () -> {
            applicationService.createApplication(jobId, applicationRequest);
        });

        assertTrue(exception.getMessage().contains("CV File ID"));
        verify(jobRepository).findById(jobId);
        verify(applicationRepository, never()).save(any(Application.class));
    }
}