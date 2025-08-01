package com.recrutech.recrutechplatform.service;

import com.recrutech.recrutechplatform.dto.job.JobRequest;
import com.recrutech.recrutechplatform.dto.job.JobResponse;
import com.recrutech.recrutechplatform.dto.job.JobSummaryResponse;
import com.recrutech.common.exception.NotFoundException;
import com.recrutech.common.exception.ValidationException;
import com.recrutech.common.validator.JobValidator;
import com.recrutech.recrutechplatform.model.Job;
import com.recrutech.recrutechplatform.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class JobService {

    private final JobRepository jobRepository;

    @Autowired
    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    private Job findJobByIdOrThrow(String id, String operation) {
        return jobRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Job with id {} not found for {}", id, operation);
                    return new NotFoundException("Job not found with id: " + id);
                });
    }

    @Transactional
    public JobResponse createJob(JobRequest jobRequest) {
        log.debug("Creating new job with title: {}", jobRequest.title());

        JobValidator.requireNonNull(jobRequest);
        JobValidator.validateJobData(jobRequest.title(), jobRequest.description(), jobRequest.location());

        Job job = Job.builder()
                .title(jobRequest.title())
                .description(jobRequest.description())
                .location(jobRequest.location())
                .active(jobRequest.active() != null ? jobRequest.active() : true)
                .build();

        Job savedJob = jobRepository.save(job);
        log.info("Job created successfully with id: {}", savedJob.getId());

        return mapToJobResponse(savedJob);
    }

    @Transactional
    public JobResponse createJob(JobRequest jobRequest, String userId) {
        log.debug("Creating new job with title: {} for user: {}", jobRequest.title(), userId);

        JobValidator.requireNonNull(jobRequest);
        JobValidator.validateJobData(jobRequest.title(), jobRequest.description(), jobRequest.location());

        Job job = Job.builder()
                .title(jobRequest.title())
                .description(jobRequest.description())
                .location(jobRequest.location())
                .createdBy(userId) // Benutzer-Zuordnung
                .active(jobRequest.active() != null ? jobRequest.active() : true)
                .build();

        Job savedJob = jobRepository.save(job);
        log.info("Job created by user {} with id: {}", userId, savedJob.getId());

        return mapToJobResponse(savedJob);
    }

    /**
     * Retrieves all jobs with summary information (id, title, location).
     *
     * @return a list of all jobs with summary information
     */
    @Transactional(readOnly = true)
    public List<JobSummaryResponse> findAllJobs() {
        log.debug("Retrieving all jobs");
        List<Job> jobs = jobRepository.findAll();
        log.info("Retrieved {} jobs", jobs.size());

        return jobs.stream()
                .map(this::mapToJobSummaryResponse)
                .toList();
    }

    /**
     * Retrieves a job by its ID with full details.
     *
     * @param id the ID of the job to retrieve
     * @return the job response with full details
     * @throws ValidationException if the ID is invalid
     * @throws NotFoundException if the job is not found
     */
    @Transactional(readOnly = true)
    public JobResponse findJobById(String id) {
        log.debug("Retrieving job with id: {}", id);

        JobValidator.validateId(id);
        Job job = findJobByIdOrThrow(id, "retrieval");

        log.info("Retrieved job with id: {}", id);
        return mapToJobResponse(job);
    }

    /**
     * Deletes a job by its ID.
     *
     * @param id the ID of the job to delete
     * @throws ValidationException if the ID is invalid
     * @throws NotFoundException if the job is not found
     */
    @Transactional
    public void deleteJobById(String id) {
        log.debug("Deleting job with id: {}", id);

        JobValidator.validateId(id);

        // Verify the job exists before deletion
        findJobByIdOrThrow(id, "deletion");

        jobRepository.deleteById(id);
        log.info("Job with id {} deleted successfully", id);
    }

    /**
     * Deletes a job by its ID with user context.
     *
     * @param id the ID of the job to delete
     * @param userId the ID of the user performing the deletion
     * @throws ValidationException if the ID is invalid
     * @throws NotFoundException if the job is not found
     */
    @Transactional
    public void deleteJobById(String id, String userId) {
        log.debug("Deleting job with id: {} by user: {}", id, userId);

        JobValidator.validateId(id);

        // Verify the job exists before deletion
        findJobByIdOrThrow(id, "deletion");

        jobRepository.deleteById(id);
        log.info("Job with id {} deleted successfully by user {}", id, userId);
    }

    /**
     * Updates a job with the provided details.
     *
     * @param id the ID of the job to update
     * @param jobRequest the job request containing updated job details
     * @return the updated job response
     * @throws ValidationException if the job request or ID is invalid
     * @throws NotFoundException if the job is not found
     */
    @Transactional
    public JobResponse updateJob(String id, JobRequest jobRequest) {
        log.debug("Updating job with id: {}", id);

        JobValidator.validateId(id);
        JobValidator.requireNonNull(jobRequest);
        JobValidator.validateJobData(jobRequest.title(), jobRequest.description(), jobRequest.location());

        Job job = findJobByIdOrThrow(id, "update");

        // Update job properties
        job.setTitle(jobRequest.title());
        job.setDescription(jobRequest.description());
        job.setLocation(jobRequest.location());

        if (jobRequest.active() != null) {
            job.setActive(jobRequest.active());
        }

        Job updatedJob = jobRepository.save(job);
        log.info("Job with id {} updated successfully", id);

        return mapToJobResponse(updatedJob);
    }

    /**
     * Updates a job with the provided details with user context.
     *
     * @param id the ID of the job to update
     * @param jobRequest the job request containing updated job details
     * @param userId the ID of the user performing the update
     * @return the updated job response
     * @throws ValidationException if the job request or ID is invalid
     * @throws NotFoundException if the job is not found
     */
    @Transactional
    public JobResponse updateJob(String id, JobRequest jobRequest, String userId) {
        log.debug("Updating job with id: {} by user: {}", id, userId);

        JobValidator.validateId(id);
        JobValidator.requireNonNull(jobRequest);
        JobValidator.validateJobData(jobRequest.title(), jobRequest.description(), jobRequest.location());

        Job job = findJobByIdOrThrow(id, "update");

        // Update job properties
        job.setTitle(jobRequest.title());
        job.setDescription(jobRequest.description());
        job.setLocation(jobRequest.location());

        if (jobRequest.active() != null) {
            job.setActive(jobRequest.active());
        }

        Job updatedJob = jobRepository.save(job);
        log.info("Job with id {} updated successfully by user {}", id, userId);

        return mapToJobResponse(updatedJob);
    }

    /**
     * Checks if a user is the owner of a job.
     *
     * @param jobId the ID of the job
     * @param userId the ID of the user
     * @return true if the user is the owner, false otherwise
     */
    public boolean isOwner(String jobId, String userId) {
        return jobRepository.findById(jobId)
                .map(job -> job.getCreatedBy() != null && job.getCreatedBy().equals(userId))
                .orElse(false);
    }

    /**
     * Maps a Job entity to a JobResponse DTO.
     *
     * @param job the job entity to map
     * @return the job response DTO
     */
    private JobResponse mapToJobResponse(Job job) {
        return new JobResponse(
                job.getId(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.isActive()
        );
    }

    /**
     * Maps a Job entity to a JobSummaryResponse DTO.
     *
     * @param job the job entity to map
     * @return the job summary response DTO
     */
    private JobSummaryResponse mapToJobSummaryResponse(Job job) {
        return new JobSummaryResponse(
                job.getId(),
                job.getTitle(),
                job.getLocation()
        );
    }

}
