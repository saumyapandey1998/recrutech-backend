package com.recrutech.recrutechplatform.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recrutech.common.exception.NotFoundException;
import com.recrutech.recrutechplatform.dto.application.ApplicationRequest;
import com.recrutech.recrutechplatform.dto.application.ApplicationResponse;
import com.recrutech.recrutechplatform.enums.ApplicationStatus;
import com.recrutech.recrutechplatform.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationController.class)
public class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationService applicationService;

    private String jobId;
    private String cvFileId;
    private ApplicationRequest applicationRequest;
    private ApplicationResponse applicationResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
        jobId = UUID.randomUUID().toString();
        cvFileId = UUID.randomUUID().toString();
        String applicationId = UUID.randomUUID().toString();

        applicationRequest = new ApplicationRequest();
        applicationRequest.setCvFileId(cvFileId);

        applicationResponse = ApplicationResponse.builder()
                .id(applicationId)
                .jobId(jobId)
                .cvFileId(cvFileId)
                .status(ApplicationStatus.RECEIVED)
                .viewedByHr(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void submitApplication_Success() throws Exception {
        // Arrange
        when(applicationService.createApplication(eq(jobId), any(ApplicationRequest.class)))
                .thenReturn(applicationResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/jobs/{jobId}/applications", jobId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(applicationResponse.getId()))
                .andExpect(jsonPath("$.jobId").value(jobId))
                .andExpect(jsonPath("$.cvFileId").value(cvFileId))
                .andExpect(jsonPath("$.status").value(ApplicationStatus.RECEIVED.toString()))
                .andExpect(jsonPath("$.viewedByHr").value(false));
    }

    @Test
    void submitApplication_JobNotFound() throws Exception {
        // Arrange
        when(applicationService.createApplication(eq(jobId), any(ApplicationRequest.class)))
                .thenThrow(new NotFoundException("Job not found with id: " + jobId));

        // Act & Assert
        mockMvc.perform(post("/api/v1/jobs/{jobId}/applications", jobId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(applicationRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void submitApplication_InvalidRequest() throws Exception {
        // Act & Assert - Send an empty request body
        mockMvc.perform(post("/api/v1/jobs/{jobId}/applications", jobId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
