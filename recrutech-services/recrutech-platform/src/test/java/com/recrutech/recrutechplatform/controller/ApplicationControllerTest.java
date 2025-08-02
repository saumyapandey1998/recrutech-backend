package com.recrutech.recrutechplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recrutech.recrutechplatform.controller.ApplicationController;
import com.recrutech.recrutechplatform.dto.application.ApplicationRequest;
import com.recrutech.recrutechplatform.dto.application.ApplicationResponse;
import com.recrutech.recrutechplatform.enums.ApplicationStatus;
import com.recrutech.common.exception.GlobalExceptionHandler;
import com.recrutech.common.exception.NotFoundException;
import com.recrutech.recrutechplatform.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ApplicationControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ApplicationService applicationService;

    @InjectMocks
    private ApplicationController applicationController;

    private ApplicationRequest applicationRequest;
    private ApplicationResponse applicationResponse;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(applicationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        testDateTime = LocalDateTime.now();

        applicationRequest = new ApplicationRequest();
        applicationRequest.setCvFileId("123e4567-e89b-12d3-a456-426614174000");

        applicationResponse = ApplicationResponse.builder()
                .id("app-id-123")
                .jobId("job-id-456")
                .cvFileId("123e4567-e89b-12d3-a456-426614174000")
                .status(ApplicationStatus.RECEIVED)
                .viewedByHr(false)
                .createdAt(testDateTime)
                .build();
    }

    @Test
    void submitApplication_ShouldReturnCreatedApplicationResponse() throws Exception {
        // Arrange
        String jobId = "job-id-456";
        when(applicationService.createApplication(eq(jobId), any(ApplicationRequest.class)))
                .thenReturn(applicationResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/jobs/{jobId}/applications", jobId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("app-id-123")))
                .andExpect(jsonPath("$.jobId", is("job-id-456")))
                .andExpect(jsonPath("$.cvFileId", is("123e4567-e89b-12d3-a456-426614174000")))
                .andExpect(jsonPath("$.status", is("RECEIVED")))
                .andExpect(jsonPath("$.viewedByHr", is(false)));

        verify(applicationService, times(1)).createApplication(eq(jobId), any(ApplicationRequest.class));
    }

    @Test
    void submitApplication_WhenJobDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        String jobId = "non-existent-job-id";
        when(applicationService.createApplication(eq(jobId), any(ApplicationRequest.class)))
                .thenThrow(new NotFoundException("Job not found with id: " + jobId));

        // Act & Assert
        mockMvc.perform(post("/api/v1/jobs/{jobId}/applications", jobId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Job not found with id: " + jobId)));

        verify(applicationService, times(1)).createApplication(eq(jobId), any(ApplicationRequest.class));
    }

    @Test
    void getAllApplications_ShouldReturnListOfApplicationResponses() throws Exception {
        // Arrange
        ApplicationResponse applicationResponse2 = ApplicationResponse.builder()
                .id("app-id-789")
                .jobId("job-id-101")
                .cvFileId("123e4567-e89b-12d3-a456-426614174001")
                .status(ApplicationStatus.UNDER_REVIEW)
                .viewedByHr(true)
                .createdAt(testDateTime)
                .build();

        List<ApplicationResponse> applicationResponses = List.of(applicationResponse, applicationResponse2);
        when(applicationService.getAllApplications()).thenReturn(applicationResponses);

        // Act & Assert
        mockMvc.perform(get("/api/v1/applications")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is("app-id-123")))
                .andExpect(jsonPath("$[0].jobId", is("job-id-456")))
                .andExpect(jsonPath("$[0].cvFileId", is("123e4567-e89b-12d3-a456-426614174000")))
                .andExpect(jsonPath("$[0].status", is("RECEIVED")))
                .andExpect(jsonPath("$[0].viewedByHr", is(false)))
                .andExpect(jsonPath("$[1].id", is("app-id-789")))
                .andExpect(jsonPath("$[1].jobId", is("job-id-101")))
                .andExpect(jsonPath("$[1].cvFileId", is("123e4567-e89b-12d3-a456-426614174001")))
                .andExpect(jsonPath("$[1].status", is("UNDER_REVIEW")))
                .andExpect(jsonPath("$[1].viewedByHr", is(true)));

        verify(applicationService, times(1)).getAllApplications();
    }

    @Test
    void getAllApplications_WhenNoApplications_ShouldReturnEmptyList() throws Exception {
        // Arrange
        when(applicationService.getAllApplications()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/v1/applications")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(applicationService, times(1)).getAllApplications();
    }

    @Test
    void getApplicationById_WhenApplicationExists_ShouldReturnApplicationResponse() throws Exception {
        // Arrange
        String applicationId = "app-id-123";
        when(applicationService.getApplicationById(applicationId)).thenReturn(applicationResponse);

        // Act & Assert
        mockMvc.perform(get("/api/v1/applications/{applicationId}", applicationId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("app-id-123")))
                .andExpect(jsonPath("$.jobId", is("job-id-456")))
                .andExpect(jsonPath("$.cvFileId", is("123e4567-e89b-12d3-a456-426614174000")))
                .andExpect(jsonPath("$.status", is("RECEIVED")))
                .andExpect(jsonPath("$.viewedByHr", is(false)));

        verify(applicationService, times(1)).getApplicationById(applicationId);
    }

    @Test
    void getApplicationById_WhenApplicationDoesNotExist_ShouldReturnNotFound() throws Exception {
        // Arrange
        String applicationId = "non-existent-app-id";
        when(applicationService.getApplicationById(applicationId))
                .thenThrow(new NotFoundException("Application not found with id: " + applicationId));

        // Act & Assert
        mockMvc.perform(get("/api/v1/applications/{applicationId}", applicationId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", is("Application not found with id: " + applicationId)));

        verify(applicationService, times(1)).getApplicationById(applicationId);
    }

    @Test
    void submitApplication_WithDifferentStatus_ShouldReturnCorrectStatus() throws Exception {
        // Arrange
        String jobId = "job-id-456";
        ApplicationResponse reviewResponse = ApplicationResponse.builder()
                .id("app-id-123")
                .jobId(jobId)
                .cvFileId("123e4567-e89b-12d3-a456-426614174000")
                .status(ApplicationStatus.UNDER_REVIEW)
                .viewedByHr(true)
                .createdAt(testDateTime)
                .build();

        when(applicationService.createApplication(eq(jobId), any(ApplicationRequest.class)))
                .thenReturn(reviewResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/jobs/{jobId}/applications", jobId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("app-id-123")))
                .andExpect(jsonPath("$.jobId", is(jobId)))
                .andExpect(jsonPath("$.status", is("UNDER_REVIEW")))
                .andExpect(jsonPath("$.viewedByHr", is(true)));

        verify(applicationService, times(1)).createApplication(eq(jobId), any(ApplicationRequest.class));
    }

    @Test
    void getAllApplications_WithDifferentStatuses_ShouldReturnAllStatuses() throws Exception {
        // Arrange
        ApplicationResponse receivedApp = ApplicationResponse.builder()
                .id("app-id-1")
                .jobId("job-id-1")
                .cvFileId("123e4567-e89b-12d3-a456-426614174001")
                .status(ApplicationStatus.RECEIVED)
                .viewedByHr(false)
                .createdAt(testDateTime)
                .build();

        ApplicationResponse invitedApp = ApplicationResponse.builder()
                .id("app-id-2")
                .jobId("job-id-2")
                .cvFileId("123e4567-e89b-12d3-a456-426614174002")
                .status(ApplicationStatus.INVITED)
                .viewedByHr(true)
                .createdAt(testDateTime)
                .build();

        ApplicationResponse rejectedApp = ApplicationResponse.builder()
                .id("app-id-3")
                .jobId("job-id-3")
                .cvFileId("123e4567-e89b-12d3-a456-426614174003")
                .status(ApplicationStatus.REJECTED)
                .viewedByHr(true)
                .createdAt(testDateTime)
                .build();

        List<ApplicationResponse> applicationResponses = List.of(receivedApp, invitedApp, rejectedApp);
        when(applicationService.getAllApplications()).thenReturn(applicationResponses);

        // Act & Assert
        mockMvc.perform(get("/api/v1/applications")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].status", is("RECEIVED")))
                .andExpect(jsonPath("$[1].status", is("INVITED")))
                .andExpect(jsonPath("$[2].status", is("REJECTED")));

        verify(applicationService, times(1)).getAllApplications();
    }
}