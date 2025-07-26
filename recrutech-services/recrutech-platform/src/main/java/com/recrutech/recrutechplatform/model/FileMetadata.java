package com.recrutech.recrutechplatform.model;

import com.recrutech.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Entity representing metadata for stored files.
 * This entity stores information about uploaded files, such as name, type, size, and path.
 */
@Entity
@Table(name = "file_metadata")
@Getter
@Setter
public class FileMetadata extends BaseEntity {

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @PrePersist
    protected void onCreate() {
        initializeEntity();
    }
}