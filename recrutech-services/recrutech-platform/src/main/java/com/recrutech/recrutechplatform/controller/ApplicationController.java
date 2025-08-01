package com.recrutech.recrutechplatform.controller;

import com.recrutech.recrutechplatform.dto.application.ApplicationRequest;
import com.recrutech.recrutechplatform.dto.application.ApplicationResponse;
import com.recrutech.recrutechplatform.service.ApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for handling application-related endpoints
 */
@RestController
@PreAuthorize("hasRole('APPLICANT') or hasRole('HR')") // Only applicants and HR can access these endpoints
@RequestMapping("/api/v1")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    /**
     * Endpoint for submitting a job application
     * 
     * @param jobId The ID of the job to apply for
     * @param applicationRequest The application data
     * @return The created application
     */
    @PostMapping("/jobs/{jobId}/applications")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse submitApplication(@PathVariable String jobId,
            @RequestBody ApplicationRequest applicationRequest) {
        return applicationService.createApplication(jobId, applicationRequest);
    }

    @GetMapping("/applications")
    @ResponseStatus(HttpStatus.OK)
    public List<ApplicationResponse> getAllApplications() {
        return applicationService.getAllApplications();
    }

    @GetMapping("/applications/{applicationId}")
    @ResponseStatus(HttpStatus.OK)
    public ApplicationResponse getApplicationById(@PathVariable String applicationId) {
        return applicationService.getApplicationById(applicationId);
    }
}