package com.recrutech.recrutechplatform.dto.application;

import lombok.Data;

/**
 * DTO for receiving application submission data from clients
 */
@Data
public class ApplicationRequest {
    private String cvFileId;
    // Add any other fields needed for application submission
}