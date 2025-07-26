package com.recrutech.common.validator;

import com.recrutech.common.exception.ValidationException;
import com.recrutech.common.util.EntityValidator;
import com.recrutech.common.util.StringValidator;

/**
 * Validator for job-related data.
 * This class provides specialized validation methods for job entities and their properties,
 * delegating to more general validators where appropriate.
 */
public final class JobValidator {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private JobValidator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    /**
     * Validates a job ID.
     * Ensures the ID is not null or empty and is a valid UUID.
     *
     * @param id the ID to validate
     * @throws ValidationException if the ID is invalid
     */
    public static void validateId(String id) {
        EntityValidator.validateId(id, "Job", true);
    }

    /**
     * Validates job data.
     *
     * @param title the job title
     * @param description the job description
     * @param location the job location
     * @throws ValidationException if any of the data is invalid
     */
    public static void validateJobData(String title, String description, String location) {
        StringValidator.validateRequired(title, 255, "Job title");
        StringValidator.validateOptional(description, 1000, "Job description");
        StringValidator.validateOptional(location, 255, "Job location");
    }

    /**
     * Validates that a job object is not null.
     *
     * @param job the job object to validate
     * @throws ValidationException if the job is null
     */
    public static void requireNonNull(Object job) {
        EntityValidator.requireNonNull(job, "Job request");
    }
}
