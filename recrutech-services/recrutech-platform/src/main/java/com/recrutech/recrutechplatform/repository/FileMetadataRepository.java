package com.recrutech.recrutechplatform.repository;

import com.recrutech.recrutechplatform.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for accessing and manipulating FileMetadata entities.
 */
@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, String> {
    
    /**
     * Find a file metadata by its ID.
     *
     * @param id the ID of the file metadata
     * @return an Optional containing the file metadata if found, or empty if not found
     */
    Optional<FileMetadata> findById(String id);
}