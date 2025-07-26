package com.recrutech.common.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the BaseEntity class.
 */
class BaseEntityTest {

    /**
     * Concrete implementation of BaseEntity for testing.
     */
    private static class TestEntity extends BaseEntity {
        public void initialize() {
            initializeEntity();
        }
    }
    
    @Test
    void initializeEntity_WithNullId_ShouldGenerateId() {
        // Arrange
        TestEntity entity = new TestEntity();
        
        // Act
        entity.initialize();
        
        // Assert
        assertNotNull(entity.getId());
        assertEquals(36, entity.getId().length());
        assertTrue(entity.getId().matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$"));
    }
    
    @Test
    void initializeEntity_WithExistingId_ShouldKeepId() {
        // Arrange
        TestEntity entity = new TestEntity();
        String existingId = "123e4567-e89b-12d3-a456-426614174000";
        entity.setId(existingId);
        
        // Act
        entity.initialize();
        
        // Assert
        assertEquals(existingId, entity.getId());
    }
    
    @Test
    void initializeEntity_WithNullCreatedAt_ShouldGenerateTimestamp() {
        // Arrange
        TestEntity entity = new TestEntity();
        LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        
        // Act
        entity.initialize();
        
        // Assert
        LocalDateTime after = LocalDateTime.now().plusSeconds(1);
        assertNotNull(entity.getCreatedAt());
        assertTrue(entity.getCreatedAt().isAfter(before) || entity.getCreatedAt().isEqual(before));
        assertTrue(entity.getCreatedAt().isBefore(after) || entity.getCreatedAt().isEqual(after));
    }
    
    @Test
    void initializeEntity_WithExistingCreatedAt_ShouldKeepCreatedAt() {
        // Arrange
        TestEntity entity = new TestEntity();
        LocalDateTime existingTimestamp = LocalDateTime.of(2023, 1, 1, 12, 0);
        entity.setCreatedAt(existingTimestamp);
        
        // Act
        entity.initialize();
        
        // Assert
        assertEquals(existingTimestamp, entity.getCreatedAt());
    }
    
    @Test
    void equals_WithSameId_ShouldReturnTrue() {
        // Arrange
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        String id = "123e4567-e89b-12d3-a456-426614174000";
        entity1.setId(id);
        entity2.setId(id);
        
        // Act & Assert
        assertTrue(entity1.equals(entity2));
        assertTrue(entity2.equals(entity1));
    }
    
    @Test
    void equals_WithDifferentId_ShouldReturnFalse() {
        // Arrange
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        entity1.setId("123e4567-e89b-12d3-a456-426614174000");
        entity2.setId("123e4567-e89b-12d3-a456-426614174001");
        
        // Act & Assert
        assertFalse(entity1.equals(entity2));
        assertFalse(entity2.equals(entity1));
    }
    
    @Test
    void equals_WithNull_ShouldReturnFalse() {
        // Arrange
        TestEntity entity = new TestEntity();
        entity.setId("123e4567-e89b-12d3-a456-426614174000");
        
        // Act & Assert
        assertFalse(entity.equals(null));
    }
    
    @Test
    void equals_WithSameObject_ShouldReturnTrue() {
        // Arrange
        TestEntity entity = new TestEntity();
        entity.setId("123e4567-e89b-12d3-a456-426614174000");
        
        // Act & Assert
        assertTrue(entity.equals(entity));
    }
    
    @Test
    void equals_WithDifferentType_ShouldReturnFalse() {
        // Arrange
        TestEntity entity = new TestEntity();
        entity.setId("123e4567-e89b-12d3-a456-426614174000");
        Object other = new Object();
        
        // Act & Assert
        assertFalse(entity.equals(other));
    }
    
    @Test
    void hashCode_WithSameId_ShouldBeEqual() {
        // Arrange
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        String id = "123e4567-e89b-12d3-a456-426614174000";
        entity1.setId(id);
        entity2.setId(id);
        
        // Act & Assert
        assertEquals(entity1.hashCode(), entity2.hashCode());
    }
    
    @Test
    void hashCode_WithDifferentId_ShouldBeDifferent() {
        // Arrange
        TestEntity entity1 = new TestEntity();
        TestEntity entity2 = new TestEntity();
        entity1.setId("123e4567-e89b-12d3-a456-426614174000");
        entity2.setId("123e4567-e89b-12d3-a456-426614174001");
        
        // Act & Assert
        assertNotEquals(entity1.hashCode(), entity2.hashCode());
    }
}