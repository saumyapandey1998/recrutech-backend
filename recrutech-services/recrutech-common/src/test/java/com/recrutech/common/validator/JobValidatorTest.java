package com.recrutech.common.validator;

import com.recrutech.common.exception.ValidationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the JobValidator class.
 */
class JobValidatorTest {

    @Test
    void validateId_WithValidUuid_ShouldNotThrowException() {
        // Arrange
        String validUuid = "123e4567-e89b-12d3-a456-426614174000";
        
        // Act & Assert
        assertDoesNotThrow(() -> JobValidator.validateId(validUuid));
    }
    
    @Test
    void validateId_WithInvalidUuid_ShouldThrowValidationException() {
        // Arrange
        String invalidUuid = "not-a-uuid";
        
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> JobValidator.validateId(invalidUuid));
        assertEquals("Job ID must be a valid UUID", exception.getMessage());
    }
    
    @Test
    void validateId_WithNullId_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> JobValidator.validateId(null));
        assertEquals("Job ID cannot be empty", exception.getMessage());
    }
    
    @Test
    void validateJobData_WithValidData_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> JobValidator.validateJobData(
            "Software Engineer", 
            "Java developer position", 
            "Berlin"
        ));
    }
    
    @Test
    void validateJobData_WithNullTitle_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> JobValidator.validateJobData(null, "Description", "Location"));
        assertEquals("Job title cannot be empty", exception.getMessage());
    }
    
    @Test
    void validateJobData_WithEmptyTitle_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> JobValidator.validateJobData("", "Description", "Location"));
        assertEquals("Job title cannot be empty", exception.getMessage());
    }
    
    @Test
    void validateJobData_WithTitleExceedingMaxLength_ShouldThrowValidationException() {
        // Arrange
        String longTitle = "a".repeat(256); // 256 characters
        
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> JobValidator.validateJobData(longTitle, "Description", "Location"));
        assertEquals("Job title cannot exceed 255 characters", exception.getMessage());
    }
    
    @Test
    void validateJobData_WithDescriptionExceedingMaxLength_ShouldThrowValidationException() {
        // Arrange
        String longDescription = "a".repeat(1001); // 1001 characters
        
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> JobValidator.validateJobData("Title", longDescription, "Location"));
        assertEquals("Job description cannot exceed 1000 characters", exception.getMessage());
    }
    
    @Test
    void validateJobData_WithLocationExceedingMaxLength_ShouldThrowValidationException() {
        // Arrange
        String longLocation = "a".repeat(256); // 256 characters
        
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> JobValidator.validateJobData("Title", "Description", longLocation));
        assertEquals("Job location cannot exceed 255 characters", exception.getMessage());
    }
    
    @Test
    void validateJobData_WithNullDescriptionAndLocation_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> JobValidator.validateJobData("Title", null, null));
    }
    
    @Test
    void requireNonNull_WithNonNullObject_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> JobValidator.requireNonNull(new Object()));
    }
    
    @Test
    void requireNonNull_WithNullObject_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> JobValidator.requireNonNull(null));
        assertEquals("Job request cannot be null", exception.getMessage());
    }
}