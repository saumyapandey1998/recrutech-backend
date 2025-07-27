package com.recrutech.recrutechauth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is an issue with token validation, generation, or refresh.
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class TokenException extends RuntimeException {

    /**
     * Constructs a new token exception with the specified detail message.
     *
     * @param message the detail message
     */
    public TokenException(String message) {
        super(message);
    }

    /**
     * Constructs a new token exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause
     */
    public TokenException(String message, Throwable cause) {
        super(message, cause);
    }
}