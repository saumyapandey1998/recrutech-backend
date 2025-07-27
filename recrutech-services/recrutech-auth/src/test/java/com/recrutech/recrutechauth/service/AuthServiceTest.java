package com.recrutech.recrutechauth.service;

import com.recrutech.recrutechauth.dto.AuthResponse;
import com.recrutech.recrutechauth.dto.LoginRequest;
import com.recrutech.recrutechauth.dto.RegisterRequest;
import com.recrutech.recrutechauth.model.Role;
import com.recrutech.recrutechauth.model.User;
import com.recrutech.recrutechauth.repository.RoleRepository;
import com.recrutech.recrutechauth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@ActiveProfiles("test")
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

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

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private Role userRole;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Set up register request
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password");
        registerRequest.setFirstName("Test");
        registerRequest.setLastName("User");

        // Set up login request
        loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        // Set up user role
        userRole = new Role();
        userRole.setId(UUID.randomUUID().toString());
        userRole.setName("ROLE_USER");

        // Set up test user
        testUser = new User();
        testUser.setId(UUID.randomUUID().toString());
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        testUser.setRoles(roles);

        // Set up authentication
        authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Mock repository methods
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authentication);
        when(jwtService.generateToken(authentication)).thenReturn("access-token");
        when(jwtService.generateRefreshToken(authentication)).thenReturn("refresh-token");
        when(jwtService.extractUsername("refresh-token")).thenReturn("testuser");
        when(jwtService.generateTokenFromRefreshToken("refresh-token")).thenReturn("new-access-token");
    }

    @Test
    void register_ShouldCreateNewUserAndReturnAuthResponse() {
        // Act
        AuthResponse response = authService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertArrayEquals(new String[]{"ROLE_USER"}, response.getRoles());

        // Verify interactions
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(roleRepository).findByName("ROLE_USER");
        
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals("Test", savedUser.getFirstName());
        assertEquals("User", savedUser.getLastName());
        
        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(jwtService).generateToken(authentication);
        verify(jwtService).generateRefreshToken(authentication);
    }

    @Test
    void register_WithExistingUsername_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });
        assertEquals("Username is already taken", exception.getMessage());
        
        // Verify no user was saved
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        // Arrange
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.register(registerRequest);
        });
        assertEquals("Email is already in use", exception.getMessage());
        
        // Verify no user was saved
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_ShouldAuthenticateUserAndReturnAuthResponse() {
        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertArrayEquals(new String[]{"ROLE_USER"}, response.getRoles());

        // Verify interactions
        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(userRepository).findByUsername("testuser");
        verify(jwtService).generateToken(authentication);
        verify(jwtService).generateRefreshToken(authentication);
    }

    @Test
    void refreshToken_ShouldGenerateNewAccessToken() {
        // Act
        AuthResponse response = authService.refreshToken("refresh-token");

        // Assert
        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("testuser", response.getUsername());
        assertEquals("test@example.com", response.getEmail());
        assertArrayEquals(new String[]{"ROLE_USER"}, response.getRoles());

        // Verify interactions
        verify(jwtService).extractUsername("refresh-token");
        verify(userRepository).findByUsername("testuser");
        verify(jwtService).generateTokenFromRefreshToken("refresh-token");
    }

    @Test
    void refreshToken_WithInvalidToken_ShouldThrowException() {
        // Arrange
        when(jwtService.generateTokenFromRefreshToken("invalid-token"))
                .thenThrow(new RuntimeException("Invalid refresh token"));

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.refreshToken("invalid-token");
        });
        assertTrue(exception.getMessage().contains("Invalid refresh token"));
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowException() {
        // Arrange
        LoginRequest invalidLoginRequest = new LoginRequest();
        invalidLoginRequest.setUsername("testuser");
        invalidLoginRequest.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Bad credentials"));

        // Act & Assert
        Exception exception = assertThrows(org.springframework.security.authentication.BadCredentialsException.class, () -> {
            authService.login(invalidLoginRequest);
        });
        assertEquals("Bad credentials", exception.getMessage());
        
        // Verify interactions
        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(userRepository, never()).findByUsername(anyString());
    }

    @Test
    void login_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        LoginRequest nonExistentUserRequest = new LoginRequest();
        nonExistentUserRequest.setUsername("nonexistentuser");
        nonExistentUserRequest.setPassword("password");

        Authentication nonExistentAuth = new UsernamePasswordAuthenticationToken(
                "nonexistentuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(nonExistentAuth);
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.login(nonExistentUserRequest);
        });
        assertEquals("User not found", exception.getMessage());
        
        // Verify interactions
        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(userRepository).findByUsername("nonexistentuser");
    }

    @Test
    void refreshToken_WithNonExistentUser_ShouldThrowException() {
        // Arrange
        String validRefreshToken = "valid-refresh-token";
        when(jwtService.extractUsername(validRefreshToken)).thenReturn("nonexistentuser");
        when(userRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(RuntimeException.class, () -> {
            authService.refreshToken(validRefreshToken);
        });
        assertTrue(exception.getMessage().contains("User not found"));
        
        // Verify interactions
        verify(jwtService).extractUsername(validRefreshToken);
        verify(userRepository).findByUsername("nonexistentuser");
    }
}