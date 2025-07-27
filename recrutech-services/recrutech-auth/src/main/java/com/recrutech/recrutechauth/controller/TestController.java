package com.recrutech.recrutechauth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for testing authentication.
 * This controller provides endpoints to test that authentication is working correctly.
 */
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * Public endpoint that doesn't require authentication.
     *
     * @return a message
     */
    @GetMapping("/public")
    public ResponseEntity<Map<String, String>> publicEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is a public endpoint");
        return ResponseEntity.ok(response);
    }

    /**
     * Protected endpoint that requires authentication.
     *
     * @return user information
     */
    @GetMapping("/protected")
    public ResponseEntity<Map<String, Object>> protectedEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "This is a protected endpoint");
        response.put("username", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Admin endpoint that requires ROLE_ADMIN.
     *
     * @return a message
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> adminEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is an admin endpoint");
        return ResponseEntity.ok(response);
    }

    /**
     * User endpoint that requires ROLE_USER.
     *
     * @return a message
     */
    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, String>> userEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is a user endpoint");
        return ResponseEntity.ok(response);
    }

    /**
     * HR endpoint that requires ROLE_HR.
     *
     * @return a message
     */
    @GetMapping("/hr")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Map<String, String>> hrEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "This is an HR endpoint");
        return ResponseEntity.ok(response);
    }
}