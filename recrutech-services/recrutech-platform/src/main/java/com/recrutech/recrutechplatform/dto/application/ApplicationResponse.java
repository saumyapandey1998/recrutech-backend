package com.recrutech.recrutechplatform.dto.application;

import com.recrutech.recrutechplatform.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for sending application data to clients
 */
@Data
@Builder
public class ApplicationResponse {
    private String id;
    private String jobId;
    private String cvFileId;
    private ApplicationStatus status;
    private boolean viewedByHr;
    private LocalDateTime createdAt;
}
