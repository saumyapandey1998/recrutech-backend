package com.recrutech.recrutechplatform.controller;

import com.recrutech.recrutechplatform.dto.JobRequest;
import com.recrutech.recrutechplatform.dto.JobResponse;
import com.recrutech.recrutechplatform.dto.JobSummaryResponse;
import com.recrutech.recrutechplatform.service.JobService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @PostMapping("/jobs")
    @ResponseStatus(HttpStatus.CREATED)
    public JobResponse createJob(@RequestBody JobRequest jobRequest) {
        return jobService.createJob(jobRequest);
    }

    @GetMapping("/jobs")
    @ResponseStatus(HttpStatus.OK)
    public List<JobSummaryResponse> getAllJobs() {
        return jobService.findAllJobs();
    }

    @GetMapping("/jobs/{id}")
    @ResponseStatus(HttpStatus.OK)
    public JobResponse getJobById(@PathVariable String id) {
        return jobService.findJobById(id);
    }

    @DeleteMapping("/jobs/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJob(@PathVariable String id) {
        jobService.deleteJobById(id);
    }

    @PutMapping("/jobs/{id}")
    @ResponseStatus(HttpStatus.OK)
    public JobResponse updateJob(@PathVariable String id, @RequestBody JobRequest jobRequest) {
        return jobService.updateJob(id, jobRequest);
    }
}
