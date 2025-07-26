package com.recrutech.recrutechplatform.service;

import com.recrutech.common.exception.NotFoundException;
import com.recrutech.common.util.UuidValidator;
import com.recrutech.recrutechplatform.dto.application.ApplicationRequest;
import com.recrutech.recrutechplatform.dto.application.ApplicationResponse;
import com.recrutech.recrutechplatform.enums.ApplicationStatus;
import com.recrutech.recrutechplatform.model.Application;
import com.recrutech.recrutechplatform.model.Job;
import com.recrutech.recrutechplatform.repository.ApplicationRepository;
import com.recrutech.recrutechplatform.repository.JobRepository;
import org.springframework.stereotype.Service;

@Service
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;

    public ApplicationService(ApplicationRepository applicationRepository, JobRepository jobRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
    }

    public ApplicationResponse createApplication(String jobId, ApplicationRequest request) {
        // Verify job exists
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("Job not found with id: " + jobId));

        // Validate cvFileId is a valid UUID
        String cvFileId = request.getCvFileId();
        UuidValidator.validateUuid(cvFileId, "CV File ID");

        // Create new application
        Application application = new Application();
        application.setCvFileId(cvFileId);
        application.setStatus(ApplicationStatus.RECEIVED);
        application.setViewedByHr(false);
        application.setJob(job);

        // Save application
        Application savedApplication = applicationRepository.save(application);

        // Return response
        return ApplicationResponse.builder()
                .id(savedApplication.getId())
                .jobId(jobId)
                .cvFileId(savedApplication.getCvFileId())
                .status(savedApplication.getStatus())
                .viewedByHr(savedApplication.isViewedByHr())
                .createdAt(savedApplication.getCreatedAt())
                .build();
    }
}
