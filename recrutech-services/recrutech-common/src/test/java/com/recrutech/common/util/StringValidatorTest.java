package com.recrutech.common.util;

import com.recrutech.common.exception.ValidationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the StringValidator utility class.
 */
class StringValidatorTest {

    @Test
    void requireNonEmpty_WithValidString_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> StringValidator.requireNonEmpty("valid", "Test field"));
    }
    
    @Test
    void requireNonEmpty_WithNullString_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> StringValidator.requireNonEmpty(null, "Test field"));
        assertEquals("Test field cannot be empty", exception.getMessage());
    }
    
    @Test
    void requireNonEmpty_WithEmptyString_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> StringValidator.requireNonEmpty("", "Test field"));
        assertEquals("Test field cannot be empty", exception.getMessage());
    }
    
    @Test
    void requireNonEmpty_WithBlankString_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> StringValidator.requireNonEmpty("   ", "Test field"));
        assertEquals("Test field cannot be empty", exception.getMessage());
    }
    
    @Test
    void validateMaxLength_WithValidLength_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> StringValidator.validateMaxLength("12345", 10, "Test field"));
    }
    
    @Test
    void validateMaxLength_WithExactMaxLength_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> StringValidator.validateMaxLength("1234567890", 10, "Test field"));
    }
    
    @Test
    void validateMaxLength_WithExceedingLength_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> StringValidator.validateMaxLength("12345678901", 10, "Test field"));
        assertEquals("Test field cannot exceed 10 characters", exception.getMessage());
    }
    
    @Test
    void validateMaxLength_WithNullString_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> StringValidator.validateMaxLength(null, 10, "Test field"));
    }
    
    @Test
    void validateRequired_WithValidString_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> StringValidator.validateRequired("valid", 10, "Test field"));
    }
    
    @Test
    void validateRequired_WithNullString_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> StringValidator.validateRequired(null, 10, "Test field"));
        assertEquals("Test field cannot be empty", exception.getMessage());
    }
    
    @Test
    void validateRequired_WithExceedingLength_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> StringValidator.validateRequired("12345678901", 10, "Test field"));
        assertEquals("Test field cannot exceed 10 characters", exception.getMessage());
    }
    
    @Test
    void validateOptional_WithValidString_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> StringValidator.validateOptional("valid", 10, "Test field"));
    }
    
    @Test
    void validateOptional_WithNullString_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> StringValidator.validateOptional(null, 10, "Test field"));
    }
    
    @Test
    void validateOptional_WithExceedingLength_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> StringValidator.validateOptional("12345678901", 10, "Test field"));
        assertEquals("Test field cannot exceed 10 characters", exception.getMessage());
    }
}