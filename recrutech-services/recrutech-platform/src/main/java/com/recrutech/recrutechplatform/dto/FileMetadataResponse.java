package com.recrutech.recrutechplatform.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO for sending file metadata to clients.
 */
@Data
@Builder
public class FileMetadataResponse {
    private String fileId;
    private String fileName;
    private String contentType;
    private Long size;
}