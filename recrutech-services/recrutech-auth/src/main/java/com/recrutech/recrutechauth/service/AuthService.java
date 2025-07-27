package com.recrutech.recrutechauth.service;

import com.recrutech.recrutechauth.dto.AuthResponse;
import com.recrutech.recrutechauth.dto.LoginRequest;
import com.recrutech.recrutechauth.dto.RegisterRequest;
import com.recrutech.recrutechauth.model.Role;
import com.recrutech.recrutechauth.model.User;
import com.recrutech.recrutechauth.repository.RoleRepository;
import com.recrutech.recrutechauth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for user authentication and registration.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Constructor for AuthService.
     *
     * @param userRepository the user repository
     * @param roleRepository the role repository
     * @param passwordEncoder the password encoder
     * @param authenticationManager the authentication manager
     * @param jwtService the JWT service
     */
    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /**
     * Registers a new user.
     *
     * @param request the registration request
     * @return the authentication response
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        // Assign default role (ROLE_USER)
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        roles.add(userRole);
        user.setRoles(roles);

        // Save user
        userRepository.save(user);

        // Authenticate the new user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Generate tokens
        String accessToken = jwtService.generateToken(authentication);
        String refreshToken = jwtService.generateRefreshToken(authentication);

        // Get user roles
        String[] userRoles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new);

        // Build response
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000) // Convert to seconds
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(userRoles)
                .build();
    }

    /**
     * Authenticates a user.
     *
     * @param request the login request
     * @return the authentication response
     */
    public AuthResponse login(LoginRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user details
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate tokens
        String accessToken = jwtService.generateToken(authentication);
        String refreshToken = jwtService.generateRefreshToken(authentication);

        // Get user roles
        String[] userRoles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toArray(String[]::new);

        // Build response
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtExpiration / 1000) // Convert to seconds
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(userRoles)
                .build();
    }

    /**
     * Refreshes an access token using a refresh token.
     *
     * @param refreshToken the refresh token
     * @return the authentication response
     */
    public AuthResponse refreshToken(String refreshToken) {
        try {
            // Validate the refresh token and generate a new access token
            // This will throw an exception if the token is invalid, expired, or not a refresh token
            String accessToken = jwtService.generateTokenFromRefreshToken(refreshToken);
            
            // Extract username from the refresh token
            String username = jwtService.extractUsername(refreshToken);
            
            // Get user details
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Get user roles
            String[] userRoles = user.getRoles().stream()
                    .map(Role::getName)
                    .toArray(String[]::new);
            
            // Build response
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken) // Return the same refresh token
                    .tokenType("Bearer")
                    .expiresIn(jwtExpiration / 1000) // Convert to seconds
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(userRoles)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token: " + e.getMessage(), e);
        }
    }
}