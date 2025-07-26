package com.recrutech.recrutechplatform.controller;

import com.recrutech.recrutechplatform.dto.ApplicationRequest;
import com.recrutech.recrutechplatform.dto.ApplicationResponse;
import com.recrutech.recrutechplatform.service.ApplicationService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling application-related endpoints
 */
@RestController
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
}