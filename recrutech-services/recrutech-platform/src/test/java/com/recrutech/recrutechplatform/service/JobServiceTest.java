package com.recrutech.recrutechplatform.service;

import com.recrutech.recrutechplatform.dto.JobRequest;
import com.recrutech.recrutechplatform.dto.JobResponse;
import com.recrutech.recrutechplatform.dto.JobSummaryResponse;
import com.recrutech.common.exception.NotFoundException;
import com.recrutech.recrutechplatform.model.Job;
import com.recrutech.recrutechplatform.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    // Define valid UUIDs for testing
    private static final String TEST_UUID_1 = "123e4567-e89b-12d3-a456-426614174000";
    private static final String TEST_UUID_2 = "123e4567-e89b-12d3-a456-426614174001";
    private static final String NON_EXISTENT_UUID = "123e4567-e89b-12d3-a456-426614174999";

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private JobService jobService;

    private JobRequest jobRequest;
    private Job job;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.now();

        // Initialize test data
        jobRequest = new JobRequest(
                "Software Engineer",
                "Java developer position",
                "Berlin",
                true
        );

        job = Job.builder()
                .id(TEST_UUID_1)
                .title("Software Engineer")
                .description("Java developer position")
                .location("Berlin")
                .createdAt(testDateTime)
                .createdBy("HR Manager")
                .active(true)
                .build();
    }

    @Test
    void createJob_ShouldReturnJobResponse() {
        // Arrange
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> {
            Job savedJob = invocation.getArgument(0);
            savedJob.setId(TEST_UUID_1); // Simulate ID generation
            return savedJob;
        });

        // Act
        JobResponse result = jobService.createJob(jobRequest);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_UUID_1, result.id());
        assertEquals(jobRequest.title(), result.title());
        assertEquals(jobRequest.description(), result.description());
        assertEquals(jobRequest.location(), result.location());
        assertEquals(jobRequest.active(), result.active());

        verify(jobRepository, times(1)).save(any(Job.class));
    }

    @Test
    void findAllJobs_ShouldReturnListOfJobSummaryResponses() {
        // Arrange
        Job job2 = Job.builder()
                .id(TEST_UUID_2)
                .title("Product Manager")
                .description("Product management role")
                .location("Munich")
                .createdAt(testDateTime)
                .createdBy("HR Director")
                .active(true)
                .build();

        when(jobRepository.findAll()).thenReturn(Arrays.asList(job, job2));

        // Act
        List<JobSummaryResponse> result = jobService.findAllJobs();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify first job
        assertEquals(TEST_UUID_1, result.get(0).id());
        assertEquals("Software Engineer", result.get(0).title());
        assertEquals("Berlin", result.get(0).location());

        // Verify second job
        assertEquals(TEST_UUID_2, result.get(1).id());
        assertEquals("Product Manager", result.get(1).title());
        assertEquals("Munich", result.get(1).location());

        verify(jobRepository, times(1)).findAll();
    }

    @Test
    void findAllJobs_WhenNoJobs_ShouldReturnEmptyList() {
        // Arrange
        when(jobRepository.findAll()).thenReturn(List.of());

        // Act
        List<JobSummaryResponse> result = jobService.findAllJobs();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(jobRepository, times(1)).findAll();
    }

    @Test
    void findJobById_WhenJobExists_ShouldReturnJobResponse() {
        // Arrange
        String jobId = TEST_UUID_1;
        when(jobRepository.findById(jobId)).thenReturn(java.util.Optional.of(job));

        // Act
        JobResponse result = jobService.findJobById(jobId);

        // Assert
        assertNotNull(result);
        assertEquals(jobId, result.id());
        assertEquals("Software Engineer", result.title());
        assertEquals("Java developer position", result.description());
        assertEquals("Berlin", result.location());
        assertEquals(true, result.active());
        verify(jobRepository, times(1)).findById(jobId);
    }

    @Test
    void findJobById_WhenJobDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        String jobId = NON_EXISTENT_UUID;
        when(jobRepository.findById(jobId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            jobService.findJobById(jobId);
        });

        assertEquals("Job not found with id: " + jobId, exception.getMessage());
        verify(jobRepository, times(1)).findById(jobId);
    }

    @Test
    void deleteJobById_WhenJobExists_ShouldDeleteJob() {
        // Arrange
        String jobId = TEST_UUID_1;
        when(jobRepository.findById(jobId)).thenReturn(java.util.Optional.of(job));

        // Act
        jobService.deleteJobById(jobId);

        // Assert
        verify(jobRepository, times(1)).findById(jobId);
        verify(jobRepository, times(1)).deleteById(jobId);
    }

    @Test
    void deleteJobById_WhenJobDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        String jobId = NON_EXISTENT_UUID;
        when(jobRepository.findById(jobId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            jobService.deleteJobById(jobId);
        });

        assertEquals("Job not found with id: " + jobId, exception.getMessage());
        verify(jobRepository, times(1)).findById(jobId);
        verify(jobRepository, never()).deleteById(jobId);
    }

    @Test
    void updateJob_WhenJobExists_ShouldReturnUpdatedJobResponse() {
        // Arrange
        String jobId = TEST_UUID_1;
        JobRequest updateRequest = new JobRequest(
                "Updated Software Engineer",
                "Updated Java developer position",
                "Updated Berlin",
                true
        );

        Job existingJob = Job.builder()
                .id(jobId)
                .title("Software Engineer")
                .description("Java developer position")
                .location("Berlin")
                .createdAt(testDateTime)
                .createdBy("HR Manager")
                .active(true)
                .build();

        Job updatedJob = Job.builder()
                .id(jobId)
                .title(updateRequest.title())
                .description(updateRequest.description())
                .location(updateRequest.location())
                .createdAt(testDateTime)
                .createdBy("HR Manager")
                .active(true)
                .build();

        when(jobRepository.findById(jobId)).thenReturn(java.util.Optional.of(existingJob));
        when(jobRepository.save(any(Job.class))).thenReturn(updatedJob);

        // Act
        JobResponse result = jobService.updateJob(jobId, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(jobId, result.id());
        assertEquals(updateRequest.title(), result.title());
        assertEquals(updateRequest.description(), result.description());
        assertEquals(updateRequest.location(), result.location());
        assertEquals(updateRequest.active(), result.active());
        verify(jobRepository, times(1)).findById(jobId);
        verify(jobRepository, times(1)).save(any(Job.class));
    }

    @Test
    void updateJob_WhenJobDoesNotExist_ShouldThrowNotFoundException() {
        // Arrange
        String jobId = NON_EXISTENT_UUID;
        JobRequest updateRequest = new JobRequest(
                "Updated Software Engineer",
                "Updated Java developer position",
                "Updated Berlin",
                true
        );

        when(jobRepository.findById(jobId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            jobService.updateJob(jobId, updateRequest);
        });

        assertEquals("Job not found with id: " + jobId, exception.getMessage());
        verify(jobRepository, times(1)).findById(jobId);
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    void updateJob_ShouldUpdateActiveStatusToFalse() {
        // Arrange
        String jobId = TEST_UUID_1;
        JobRequest updateRequest = new JobRequest(
                "Updated Software Engineer",
                "Updated Java developer position",
                "Updated Berlin",
                false
        );

        Job existingJob = Job.builder()
                .id(jobId)
                .title("Software Engineer")
                .description("Java developer position")
                .location("Berlin")
                .createdAt(testDateTime)
                .createdBy("HR Manager")
                .active(true)
                .build();

        Job updatedJob = Job.builder()
                .id(jobId)
                .title(updateRequest.title())
                .description(updateRequest.description())
                .location(updateRequest.location())
                .createdAt(testDateTime)
                .createdBy("HR Manager")
                .active(false)
                .build();

        when(jobRepository.findById(jobId)).thenReturn(java.util.Optional.of(existingJob));
        when(jobRepository.save(any(Job.class))).thenReturn(updatedJob);

        // Act
        JobResponse result = jobService.updateJob(jobId, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals(jobId, result.id());
        assertEquals(updateRequest.title(), result.title());
        assertEquals(updateRequest.description(), result.description());
        assertEquals(updateRequest.location(), result.location());
        assertEquals(false, result.active());
        verify(jobRepository, times(1)).findById(jobId);
        verify(jobRepository, times(1)).save(any(Job.class));
    }
}
