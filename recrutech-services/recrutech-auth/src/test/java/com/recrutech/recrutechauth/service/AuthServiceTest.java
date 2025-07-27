package com.recrutech.recrutechauth.service;

import com.recrutech.recrutechauth.dto.AuthResponse;
import com.recrutech.recrutechauth.dto.LoginRequest;
import com.recrutech.recrutechauth.dto.RegisterRequest;
import com.recrutech.recrutechauth.exception.AuthenticationException;
import com.recrutech.recrutechauth.exception.RegistrationException;
import com.recrutech.recrutechauth.exception.TokenException;
import com.recrutech.recrutechauth.model.Role;
import com.recrutech.recrutechauth.model.User;
import com.recrutech.recrutechauth.repository.RoleRepository;
import com.recrutech.recrutechauth.repository.UserRepository;
import com.recrutech.recrutechauth.validator.PasswordValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AuthService using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordValidator passwordValidator;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        // Set JWT expiration
        ReflectionTestUtils.setField(authService, "jwtExpiration", 3600000L);

        // Create test data
        registerRequest = RegisterRequest.builder()
                .username("testuser")
                .email("test@example.com")
                .password("Password123!")
                .firstName("Test")
                .lastName("User")
                .build();

        loginRequest = LoginRequest.builder()
                .username("testuser")
                .password("Password123!")
                .build();

        userRole = new Role("ROLE_USER", "Standard user role");

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.addRole(userRole);
    }

    @Test
    void register_ShouldRegisterUser_WhenValidRequest() {
        // Arrange
        when(passwordValidator.validate(anyString())).thenReturn(Collections.emptyList());
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(jwtService.generateToken(any(Authentication.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(Authentication.class))).thenReturn("refresh-token");

        // Mock authorities
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        doReturn(authorities).when(authentication).getAuthorities();

        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());

        verify(userRepository).save(any(User.class));
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(any(Authentication.class));
        verify(jwtService).generateRefreshToken(any(Authentication.class));
    }

    @Test
    void register_ShouldThrowException_WhenPasswordValidationFails() {
        // Arrange
        when(passwordValidator.validate(anyString())).thenReturn(List.of("Password is too weak"));

        // Act & Assert
        RegistrationException exception = assertThrows(RegistrationException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Password validation failed", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        // Arrange
        when(passwordValidator.validate(anyString())).thenReturn(Collections.emptyList());
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        RegistrationException exception = assertThrows(RegistrationException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Username is already taken", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        // Arrange
        when(passwordValidator.validate(anyString())).thenReturn(Collections.emptyList());
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        RegistrationException exception = assertThrows(RegistrationException.class, () -> {
            authService.register(registerRequest);
        });

        assertEquals("Email is already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldAuthenticateUser_WhenValidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(any(Authentication.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(Authentication.class))).thenReturn("refresh-token");

        // Mock authorities
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        doReturn(authorities).when(authentication).getAuthorities();

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(any(Authentication.class));
        verify(jwtService).generateRefreshToken(any(Authentication.class));
    }

    @Test
    void login_ShouldThrowException_WhenInvalidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            authService.login(loginRequest);
        });

        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void refreshToken_ShouldRefreshToken_WhenValidToken() {
        // Arrange
        String refreshToken = "valid-refresh-token";
        when(jwtService.generateTokenFromRefreshToken(refreshToken)).thenReturn("new-access-token");
        when(jwtService.rotateRefreshToken(refreshToken)).thenReturn("new-refresh-token");
        when(jwtService.extractUsername(refreshToken)).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        AuthResponse response = authService.refreshToken(refreshToken);

        // Assert
        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());

        verify(jwtService).generateTokenFromRefreshToken(refreshToken);
        verify(jwtService).rotateRefreshToken(refreshToken);
        verify(jwtService).extractUsername(refreshToken);
    }

    @Test
    void refreshToken_ShouldThrowException_WhenInvalidToken() {
        // Arrange
        String refreshToken = "invalid-refresh-token";
        when(jwtService.generateTokenFromRefreshToken(refreshToken)).thenThrow(new TokenException("Invalid refresh token"));

        // Act & Assert
        TokenException exception = assertThrows(TokenException.class, () -> {
            authService.refreshToken(refreshToken);
        });

        assertEquals("Invalid refresh token", exception.getMessage());
    }
}
