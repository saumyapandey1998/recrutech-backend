package com.recrutech.recrutechauth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;

/**
 * Exception thrown when there is an issue with user registration.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class RegistrationException extends RuntimeException {

    private List<String> errors;

    /**
     * Constructs a new registration exception with the specified detail message.
     *
     * @param message the detail message
     */
    public RegistrationException(String message) {
        super(message);
    }

    /**
     * Constructs a new registration exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public RegistrationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new registration exception with the specified detail message and errors.
     *
     * @param message the detail message
     * @param errors the list of validation errors
     */
    public RegistrationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    /**
     * Gets the list of validation errors.
     *
     * @return the list of validation errors
     */
    public List<String> getErrors() {
        return errors;
    }
}