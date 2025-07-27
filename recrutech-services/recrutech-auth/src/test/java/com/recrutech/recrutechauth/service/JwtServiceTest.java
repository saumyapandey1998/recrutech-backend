package com.recrutech.recrutechauth.service;

import com.recrutech.recrutechauth.exception.TokenException;
import com.recrutech.recrutechauth.model.RefreshToken;
import com.recrutech.recrutechauth.model.Role;
import com.recrutech.recrutechauth.model.User;
import com.recrutech.recrutechauth.repository.RefreshTokenRepository;
import com.recrutech.recrutechauth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtService using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    @Mock
    private JwtEncoderParameters jwtEncoderParameters;

    @InjectMocks
    private JwtService jwtService;

    private User testUser;
    private RefreshToken testRefreshToken;
    private String testToken;
    private String testRefreshTokenValue;

    @BeforeEach
    void setUp() {
        // Set JWT expiration
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 86400000L);
        ReflectionTestUtils.setField(jwtService, "audience", "test-audience");
        ReflectionTestUtils.setField(jwtService, "jwtId", "test-jwt-id");

        // Create test data
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        Role userRole = new Role("USER", "Standard user role");
        testUser.addRole(userRole);

        testToken = "test-token";
        testRefreshTokenValue = "test-refresh-token";

        testRefreshToken = new RefreshToken();
        testRefreshToken.setToken(testRefreshTokenValue);
        testRefreshToken.setUser(testUser);
        testRefreshToken.setExpiryDate(Instant.now().plus(1, ChronoUnit.DAYS));
        testRefreshToken.setTokenId("test-token-id");
        testRefreshToken.setRevoked(false);
    }

    @Test
    void generateToken_ShouldReturnToken_WhenValidAuthentication() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        // Mock the authorities without using Collections.singletonList
        Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        doReturn(authorities).when(authentication).getAuthorities();

        Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getTokenValue()).thenReturn(testToken);

        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

        // Act
        String token = jwtService.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertEquals(testToken, token);
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
    }

    @Test
    void generateRefreshToken_ShouldReturnAndStoreToken_WhenValidAuthentication() {
        // Arrange
        when(authentication.getName()).thenReturn("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getTokenValue()).thenReturn(testRefreshTokenValue);

        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(mockJwt);

        // Act
        String token = jwtService.generateRefreshToken(authentication);

        // Assert
        assertNotNull(token);
        assertEquals(testRefreshTokenValue, token);
        verify(jwtEncoder).encode(any(JwtEncoderParameters.class));
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void validateToken_ShouldReturnJwt_WhenValidToken() {
        // Arrange
        when(jwtDecoder.decode(testToken)).thenReturn(jwt);

        // Act
        Jwt result = jwtService.validateToken(testToken);

        // Assert
        assertNotNull(result);
        assertEquals(jwt, result);
        verify(jwtDecoder).decode(testToken);
    }

    @Test
    void validateToken_ShouldThrowException_WhenInvalidToken() {
        // Arrange
        when(jwtDecoder.decode(testToken)).thenThrow(new JwtException("Invalid token"));

        // Act & Assert
        TokenException exception = assertThrows(TokenException.class, () -> {
            jwtService.validateToken(testToken);
        });

        assertEquals("Invalid token: Invalid token", exception.getMessage());
        verify(jwtDecoder).decode(testToken);
    }

    @Test
    void extractUsername_ShouldReturnUsername_WhenValidToken() {
        // Arrange
        when(jwtDecoder.decode(testToken)).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn("testuser");

        // Act
        String username = jwtService.extractUsername(testToken);

        // Assert
        assertEquals("testuser", username);
        verify(jwtDecoder).decode(testToken);
        verify(jwt).getSubject();
    }

    @Test
    void isTokenExpired_ShouldReturnTrue_WhenTokenIsExpired() {
        // Arrange
        when(jwtDecoder.decode(testToken)).thenReturn(jwt);
        when(jwt.getExpiresAt()).thenReturn(Instant.now().minus(1, ChronoUnit.HOURS));

        // Act
        boolean expired = jwtService.isTokenExpired(testToken);

        // Assert
        assertTrue(expired);
        verify(jwtDecoder).decode(testToken);
        verify(jwt).getExpiresAt();
    }

    @Test
    void isTokenExpired_ShouldReturnFalse_WhenTokenIsNotExpired() {
        // Arrange
        when(jwtDecoder.decode(testToken)).thenReturn(jwt);
        when(jwt.getExpiresAt()).thenReturn(Instant.now().plus(1, ChronoUnit.HOURS));

        // Act
        boolean expired = jwtService.isTokenExpired(testToken);

        // Assert
        assertFalse(expired);
        verify(jwtDecoder).decode(testToken);
        verify(jwt).getExpiresAt();
    }

    @Test
    void isRefreshToken_ShouldReturnTrue_WhenTokenIsRefreshToken() {
        // Arrange
        when(jwtDecoder.decode(testToken)).thenReturn(jwt);
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", "refresh");
        when(jwt.getClaims()).thenReturn(claims);

        // Act
        boolean isRefreshToken = jwtService.isRefreshToken(testToken);

        // Assert
        assertTrue(isRefreshToken);
        verify(jwtDecoder).decode(testToken);
        verify(jwt).getClaims();
    }

    @Test
    void isRefreshToken_ShouldReturnFalse_WhenTokenIsNotRefreshToken() {
        // Arrange
        when(jwtDecoder.decode(testToken)).thenReturn(jwt);
        Map<String, Object> claims = new HashMap<>();
        claims.put("token_type", "access");
        when(jwt.getClaims()).thenReturn(claims);

        // Act
        boolean isRefreshToken = jwtService.isRefreshToken(testToken);

        // Assert
        assertFalse(isRefreshToken);
        verify(jwtDecoder).decode(testToken);
        verify(jwt).getClaims();
    }
}
