package com.recrutech.common.util;

import com.recrutech.common.exception.ValidationException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the UuidValidator utility class.
 */
class UuidValidatorTest {

    private static final String VALID_UUID = "123e4567-e89b-12d3-a456-426614174000";
    private static final String VALID_UUID_V4 = "123e4567-e89b-42d3-a456-426614174000";
    private static final String INVALID_UUID = "not-a-uuid";

    @Test
    void isValidUuid_WithValidUuid_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(UuidValidator.isValidUuid(VALID_UUID));
    }

    @Test
    void isValidUuidFormat_WithValidUuid_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(UuidValidator.isValidUuidFormat(VALID_UUID));
    }

    @Test
    void isValidUuidFormat_WithInvalidUuid_ShouldReturnFalse() {
        // Arrange
        String[] invalidUuids = {
            null,
            "",
            INVALID_UUID,
            "123e4567-e89b-12d3-a456-42661417400", // too short
            "123e4567-e89b-12d3-a456-4266141740000", // too long
            "123e4567-e89b-12d3-a456_426614174000", // invalid character
            "123e4567e89b12d3a456426614174000" // no hyphens
        };

        // Act & Assert
        for (String invalidUuid : invalidUuids) {
            assertFalse(UuidValidator.isValidUuidFormat(invalidUuid));
        }
    }

    @Test
    void isValidUuidV4_WithValidUuidV4_ShouldReturnTrue() {
        // Act & Assert
        assertTrue(UuidValidator.isValidUuidV4(VALID_UUID_V4));
    }

    @Test
    void isValidUuidV4_WithInvalidUuidV4_ShouldReturnFalse() {
        // Arrange
        String[] invalidUuidsV4 = {
            null,
            "",
            INVALID_UUID,
            VALID_UUID, // Valid UUID but not V4
            "123e4567-e89b-42d3-a456-42661417400", // too short
            "123e4567-e89b-42d3-a456-4266141740000", // too long
            "123e4567-e89b-42d3-a456_426614174000", // invalid character
            "123e4567e89b42d3a456426614174000" // no hyphens
        };

        // Act & Assert
        for (String invalidUuid : invalidUuidsV4) {
            assertFalse(UuidValidator.isValidUuidV4(invalidUuid));
        }
    }

    @Test
    void validateUuid_WithValidUuid_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> UuidValidator.validateUuid(VALID_UUID, "Test field"));
    }

    @Test
    void validateUuid_WithInvalidUuid_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> UuidValidator.validateUuid(INVALID_UUID, "Test field"));
        assertEquals("Test field must be a valid UUID", exception.getMessage());
    }

    @Test
    void validateUuid_WithNullUuid_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> UuidValidator.validateUuid(null, "Test field"));
        assertEquals("Test field cannot be null", exception.getMessage());
    }

    @Test
    void validateUuidV4_WithValidUuidV4_ShouldNotThrowException() {
        // Act & Assert
        assertDoesNotThrow(() -> UuidValidator.validateUuidV4(VALID_UUID_V4, "Test field"));
    }

    @Test
    void validateUuidV4_WithInvalidUuidV4_ShouldThrowValidationException() {
        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class, 
            () -> UuidValidator.validateUuidV4(INVALID_UUID, "Test field"));
        assertEquals("Test field must be a valid UUID version 4", exception.getMessage());
    }

    @Test
    void getUuidVersion_WithValidUuidV4_ShouldReturnVersion4() {
        // Act
        int version = UuidValidator.getUuidVersion(VALID_UUID_V4);

        // Assert
        assertEquals(4, version);
    }

    @Test
    void getUuidVersion_WithInvalidUuid_ShouldReturnMinusOne() {
        // Act
        int version = UuidValidator.getUuidVersion(INVALID_UUID);

        // Assert
        assertEquals(-1, version);
    }
}
