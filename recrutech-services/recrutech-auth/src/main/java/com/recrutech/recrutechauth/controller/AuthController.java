package com.recrutech.recrutechauth.controller;

import com.recrutech.recrutechauth.dto.AuthResponse;
import com.recrutech.recrutechauth.dto.LoginRequest;
import com.recrutech.recrutechauth.dto.RefreshTokenRequest;
import com.recrutech.recrutechauth.dto.RegisterRequest;
import com.recrutech.recrutechauth.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for authentication endpoints.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    /**
     * Constructor for AuthController.
     *
     * @param authService the authentication service
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint for user registration.
     *
     * @param request the registration request
     * @return the authentication response
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Endpoint for user login.
     *
     * @param request the login request
     * @return the authentication response
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * Endpoint for refreshing an access token.
     *
     * @param request the refresh token request
     * @return the authentication response
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request.getRefreshToken()));
    }
}