package com.recrutech.common.util;

import com.recrutech.common.exception.ValidationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the EntityValidator utility class.
 */
class EntityValidatorTest {

    @Test
    void requireNonNull_WithNonNullObject_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> EntityValidator.requireNonNull(new Object(), "Test entity"));
    }
    
    @Test
    void requireNonNull_WithNullObject_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> EntityValidator.requireNonNull(null, "Test entity"));
        assertEquals("Test entity cannot be null", exception.getMessage());
    }
    
    @Test
    void validateId_WithValidId_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> EntityValidator.validateId("valid-id", "Test entity"));
    }
    
    @Test
    void validateId_WithNullId_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> EntityValidator.validateId(null, "Test entity"));
        assertEquals("Test entity ID cannot be empty", exception.getMessage());
    }
    
    @Test
    void validateId_WithEmptyId_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> EntityValidator.validateId("", "Test entity"));
        assertEquals("Test entity ID cannot be empty", exception.getMessage());
    }
    
    @Test
    void validateId_WithValidIdAndUuidValidation_ShouldNotThrowException() {
        // Arrange
        String validUuid = "123e4567-e89b-12d3-a456-426614174000";
        
        // Act & Assert
        assertDoesNotThrow(() -> EntityValidator.validateId(validUuid, "Test entity", true));
    }
    
    @Test
    void validateId_WithInvalidIdAndUuidValidation_ShouldThrowValidationException() {
        // Arrange
        String invalidUuid = "not-a-uuid";
        
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> EntityValidator.validateId(invalidUuid, "Test entity", true));
        assertEquals("Test entity ID must be a valid UUID", exception.getMessage());
    }
}