package com.recrutech.recrutechplatform.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.recrutech.recrutechplatform.dto.job.JobRequest;
import com.recrutech.recrutechplatform.dto.job.JobResponse;
import com.recrutech.recrutechplatform.dto.job.JobSummaryResponse;
import com.recrutech.common.exception.NotFoundException;
import com.recrutech.recrutechplatform.service.JobService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobService jobService;

    private JobRequest jobRequest;
    private JobResponse jobResponse;
    private JobSummaryResponse jobSummaryResponse;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        testDateTime = LocalDateTime.now();

        jobRequest = new JobRequest(
                "Software Engineer",
                "Java developer position",
                "Berlin",
                true
        );

        jobResponse = new JobResponse(
                "test-id-123",
                "Software Engineer",
                "Java developer position",
                "Berlin",
                true
        );

        jobSummaryResponse = new JobSummaryResponse(
                "test-id-123",
                "Software Engineer",
                "Berlin"
        );
    }

    @Test
    void createJob_ShouldReturnCreatedJobResponse() throws Exception {
        // Arrange
        when(jobService.createJob(any(JobRequest.class))).thenReturn(jobResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(jobRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("test-id-123")))
                .andExpect(jsonPath("$.title", is("Software Engineer")))
                .andExpect(jsonPath("$.description", is("Java developer position")))
                .andExpect(jsonPath("$.location", is("Berlin")))
                .andExpect(jsonPath("$.active", is(true)));
    }

    @Test
    void getAllJobs_ShouldReturnListOfJobSummaryResponses() throws Exception {
        // Arrange
        JobSummaryResponse jobSummaryResponse2 = new JobSummaryResponse(
                "test-id-456",
                "Product Manager",
                "Munich"
        );
        List<JobSummaryResponse> jobSummaryResponses = Arrays.asList(jobSummaryResponse, jobSummaryResponse2);

        when(jobService.findAllJobs()).thenReturn(jobSummaryResponses);

        // Act & Assert
        mockMvc.perform(get("/api/v1/jobs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("test-id-123")))
                .andExpect(jsonPath("$[0].title", is("Software Engineer")))
                .andExpect(jsonPath("$[0].location", is("Berlin")))
                .andExpect(jsonPath("$[1].id", is("test-id-456")))
                .andExpect(jsonPath("$[1].title", is("Product Manager")))
                .andExpect(jsonPath("$[1].location", is("Munich")));
    }

    @Test
    void getAllJobs_WhenNoJobs_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(jobService.findAllJobs()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/jobs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void deleteJob_ShouldReturnNoContent() throws Exception {
        // Arrange
        String jobId = "test-id-123";
        doNothing().when(jobService).deleteJobById(jobId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/jobs/{id}", jobId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(jobService, times(1)).deleteJobById(jobId);
    }

    @Test
    void deleteJob_WhenJobDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        String jobId = "non-existent-id";
        doThrow(new NotFoundException("Job not found with id: " + jobId))
                .when(jobService).deleteJobById(jobId);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/jobs/{id}", jobId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Job not found with id: " + jobId)));

        verify(jobService, times(1)).deleteJobById(jobId);
    }

    @Test
    void updateJob_WhenJobExists_ShouldReturnUpdatedJobResponse() throws Exception {
        // Arrange
        String jobId = "test-id-123";
        JobRequest updateRequest = new JobRequest(
                "Updated Software Engineer",
                "Updated Java developer position",
                "Updated Berlin",
                true
        );

        JobResponse updatedResponse = new JobResponse(
                jobId,
                "Updated Software Engineer",
                "Updated Java developer position",
                "Updated Berlin",
                true
        );

        when(jobService.updateJob(eq(jobId), any(JobRequest.class))).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/jobs/{id}", jobId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(jobId)))
                .andExpect(jsonPath("$.title", is("Updated Software Engineer")))
                .andExpect(jsonPath("$.description", is("Updated Java developer position")))
                .andExpect(jsonPath("$.location", is("Updated Berlin")))
                .andExpect(jsonPath("$.active", is(true)));

        verify(jobService, times(1)).updateJob(eq(jobId), any(JobRequest.class));
    }

    @Test
    void updateJob_WhenJobDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        String jobId = "non-existent-id";
        JobRequest updateRequest = new JobRequest(
                "Updated Software Engineer",
                "Updated Java developer position",
                "Updated Berlin",
                true
        );

        when(jobService.updateJob(eq(jobId), any(JobRequest.class)))
                .thenThrow(new NotFoundException("Job not found with id: " + jobId));

        // Act & Assert
        mockMvc.perform(put("/api/v1/jobs/{id}", jobId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Job not found with id: " + jobId)));

        verify(jobService, times(1)).updateJob(eq(jobId), any(JobRequest.class));
    }

    @Test
    void getJobById_WhenJobExists_ShouldReturnJobResponse() throws Exception {
        // Arrange
        String jobId = "test-id-123";
        when(jobService.findJobById(jobId)).thenReturn(jobResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/jobs/{id}", jobId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(jobId)))
                .andExpect(jsonPath("$.title", is("Software Engineer")))
                .andExpect(jsonPath("$.description", is("Java developer position")))
                .andExpect(jsonPath("$.location", is("Berlin")))
                .andExpect(jsonPath("$.active", is(true)));

        verify(jobService, times(1)).findJobById(jobId);
    }

    @Test
    void getJobById_WhenJobDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        String jobId = "non-existent-id";
        when(jobService.findJobById(jobId))
                .thenThrow(new NotFoundException("Job not found with id: " + jobId));

        // Act & Assert
        mockMvc.perform(get("/api/v1/jobs/{id}", jobId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Job not found with id: " + jobId)));

        verify(jobService, times(1)).findJobById(jobId);
    }

    @Test
    void updateJob_ShouldUpdateActiveStatusToFalse() throws Exception {
        // Arrange
        String jobId = "test-id-123";
        JobRequest updateRequest = new JobRequest(
                "Updated Software Engineer",
                "Updated Java developer position",
                "Updated Berlin",
                false
        );

        JobResponse updatedResponse = new JobResponse(
                jobId,
                "Updated Software Engineer",
                "Updated Java developer position",
                "Updated Berlin",
                false
        );

        when(jobService.updateJob(eq(jobId), any(JobRequest.class))).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/v1/jobs/{id}", jobId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(jobId)))
                .andExpect(jsonPath("$.title", is("Updated Software Engineer")))
                .andExpect(jsonPath("$.description", is("Updated Java developer position")))
                .andExpect(jsonPath("$.location", is("Updated Berlin")))
                .andExpect(jsonPath("$.active", is(false)));

        verify(jobService, times(1)).updateJob(eq(jobId), any(JobRequest.class));
    }
}
