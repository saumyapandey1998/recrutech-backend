package com.recrutech.recrutechplatform.controller;

import com.recrutech.recrutechplatform.dto.job.JobRequest;
import com.recrutech.recrutechplatform.dto.job.JobResponse;
import com.recrutech.recrutechplatform.dto.job.JobSummaryResponse;
import com.recrutech.recrutechplatform.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasRole('HR')") // Basis-Berechtigung für alle Endpoints - nur HR Personal
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/jobs")
    @PreAuthorize("hasRole('HR')")
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse createJob(@RequestBody JobRequest jobRequest, 
                                Authentication authentication) {
        // Benutzer-Kontext für Audit-Logging
        String userId = authentication.getName();
        return jobService.createJob(jobRequest, userId);
    }

    @GetMapping("/jobs")
    @PreAuthorize("permitAll()") // Öffentlich zugänglich
    @ResponseStatus(HttpStatus.OK)
    public List<JobSummaryResponse> getAllJobs() {
        return jobService.findAllJobs();
    }

    @GetMapping("/jobs/{id}")
    @PreAuthorize("permitAll()") // Öffentlich zugänglich
    @ResponseStatus(HttpStatus.OK)
    public JobResponse getJobById(@PathVariable String id) {
        return jobService.findJobById(id);
    }

    @DeleteMapping("/jobs/{id}")
    @PreAuthorize("hasRole('HR')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(@PathVariable String id, Authentication authentication) {
        String userId = authentication.getName();
        jobService.deleteJobById(id, userId);
    }

    @PutMapping("/jobs/{id}")
    @PreAuthorize("hasRole('HR')")
    @ResponseStatus(HttpStatus.OK)
    public JobResponse updateJob(@PathVariable String id, 
                               @RequestBody JobRequest jobRequest,
                               Authentication authentication) {
        String userId = authentication.getName();
        return jobService.updateJob(id, jobRequest, userId);
    }
}
