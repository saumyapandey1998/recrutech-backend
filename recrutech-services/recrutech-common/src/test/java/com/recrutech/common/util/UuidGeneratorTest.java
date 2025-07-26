package com.recrutech.common.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the UuidGenerator utility class.
 */
class
UuidGeneratorTest {

    @Test
    void generateUuid_ShouldReturnValidUuid() {
        // Act
        String uuid = UuidGenerator.generateUuid();

        // Assert
        assertNotNull(uuid);
        assertEquals(36, uuid.length());
        assertTrue(uuid.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
    }

    @Test
    void generateUuid_ShouldReturnDifferentUuidsOnMultipleCalls() {
        // Act
        String uuid1 = UuidGenerator.generateUuid();
        String uuid2 = UuidGenerator.generateUuid();

        // Assert
        assertNotEquals(uuid1, uuid2);
    }

    @Test
    void isValidUuid_WithValidUuid_ShouldReturnTrue() {
        // Arrange
        String validUuid = "123e4567-e89b-12d3-a456-426614174000";

        // Act & Assert
        assertTrue(UuidGenerator.isValidUuid(validUuid));
    }

    @Test
    void isValidUuid_WithInvalidUuid_ShouldReturnFalse() {
        // Arrange
        String[] invalidUuids = {
            null,
            "",
            "not-a-uuid",
            "123e4567-e89b-12d3-a456-42661417400", // too short
            "123e4567-e89b-12d3-a456-4266141740000", // too long
            "123e4567-e89b-12d3-a456_426614174000", // invalid character
            "123e4567e89b12d3a456426614174000" // no hyphens
        };

        // Act & Assert
        for (String invalidUuid : invalidUuids) {
            System.out.println("[DEBUG_LOG] Testing invalid UUID: " + invalidUuid);
            boolean result = UuidGenerator.isValidUuid(invalidUuid);
            System.out.println("[DEBUG_LOG] Result: " + result);
            assertFalse(result, "UUID should be invalid: " + invalidUuid);
        }
    }
}
