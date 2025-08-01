package com.recrutech.recrutechauth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for HR user registration request.
 * This DTO is specifically for HR registration and does not include a role field
 * as the HR role is automatically assigned.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HRRegisterRequest {

    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
}