package com.recrutech.recrutechauth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.*;

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtServiceTest {

    @Mock
    private JwtEncoder jwtEncoder;
    
    @Mock
    private JwtDecoder jwtDecoder;
    
    @InjectMocks
    private JwtService jwtService;

    private Authentication authentication;
    private Jwt mockAccessJwt;
    private Jwt mockRefreshJwt;
    private String validAccessToken = "valid.access.token";
    private String validRefreshToken = "valid.refresh.token";
    private String invalidToken = "invalid.token.value";

    @BeforeEach
    void setUp() {
        // Create a mock authentication object
        authentication = new UsernamePasswordAuthenticationToken(
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        // Set up mock JWT encoder
        Instant now = Instant.now();
        Instant accessExpiration = now.plusSeconds(3600);
        Instant refreshExpiration = now.plusSeconds(86400);
        
        Jwt.Builder accessTokenBuilder = Jwt.withTokenValue(validAccessToken)
                .header("alg", "RS256")
                .subject("testuser")
                .issuedAt(now)
                .expiresAt(accessExpiration);
                
        Jwt.Builder refreshTokenBuilder = Jwt.withTokenValue(validRefreshToken)
                .header("alg", "RS256")
                .subject("testuser")
                .issuedAt(now)
                .expiresAt(refreshExpiration)
                .claim("token_type", "refresh");
                
        mockAccessJwt = accessTokenBuilder.build();
        mockRefreshJwt = refreshTokenBuilder.build();
        
        // Configure JwtEncoder mock
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenAnswer(invocation -> {
            // Return different tokens based on claims
            JwtEncoderParameters params = invocation.getArgument(0);
            JwtClaimsSet claims = params.getClaims();
            
            Instant tokenIssuedAt = now;
            Instant tokenExpiresAt = claims.getExpiresAt();
            
            if (claims.getClaims().containsKey("token_type") && 
                "refresh".equals(claims.getClaims().get("token_type"))) {
                return new Jwt(validRefreshToken, 
                              tokenIssuedAt, 
                              tokenExpiresAt, 
                              Map.of("alg", "RS256"), 
                              claims.getClaims());
            } else {
                return new Jwt(validAccessToken, 
                              tokenIssuedAt, 
                              tokenExpiresAt, 
                              Map.of("alg", "RS256"), 
                              claims.getClaims());
            }
        });
        
        // Configure JwtDecoder mock
        when(jwtDecoder.decode(validAccessToken)).thenReturn(mockAccessJwt);
        when(jwtDecoder.decode(validRefreshToken)).thenReturn(mockRefreshJwt);
        when(jwtDecoder.decode(invalidToken)).thenThrow(new JwtException("Invalid token"));
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        // Act
        String token = jwtService.generateToken(authentication);

        // Assert
        assertNotNull(token);
        assertEquals(validAccessToken, token);
        
        // Validate the token
        Jwt jwt = jwtService.validateToken(token);
        assertEquals("testuser", jwt.getSubject());
        
        // Check that it's not a refresh token
        assertFalse(jwtService.isRefreshToken(token));
        
        // Check that it's not expired
        assertFalse(jwtService.isTokenExpired(token));
    }

    @Test
    void generateRefreshToken_ShouldReturnValidRefreshToken() {
        // Act
        String refreshToken = jwtService.generateRefreshToken(authentication);

        // Assert
        assertNotNull(refreshToken);
        assertEquals(validRefreshToken, refreshToken);
        
        // Validate the token
        Jwt jwt = jwtService.validateToken(refreshToken);
        assertEquals("testuser", jwt.getSubject());
        
        // Check that it's a refresh token
        assertTrue(jwtService.isRefreshToken(refreshToken));
        
        // Check that it's not expired
        assertFalse(jwtService.isTokenExpired(refreshToken));
    }

    @Test
    void generateTokenFromRefreshToken_ShouldReturnNewAccessToken() {
        // Act
        String accessToken = jwtService.generateTokenFromRefreshToken(validRefreshToken);
        
        // Assert
        assertNotNull(accessToken);
        assertEquals(validAccessToken, accessToken);
        
        // Validate the token
        Jwt jwt = jwtService.validateToken(accessToken);
        assertEquals("testuser", jwt.getSubject());
        
        // Check that it's not a refresh token
        assertFalse(jwtService.isRefreshToken(accessToken));
    }

    @Test
    void generateTokenFromRefreshToken_WithAccessToken_ShouldThrowException() {
        // Mock behavior for this specific test
        when(jwtDecoder.decode(validAccessToken)).thenReturn(mockAccessJwt);
        
        // Act & Assert
        JwtException exception = assertThrows(JwtException.class, () -> {
            jwtService.generateTokenFromRefreshToken(validAccessToken);
        });
        
        assertEquals("Not a refresh token", exception.getMessage());
    }

    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        // Act
        String username = jwtService.extractUsername(validAccessToken);
        
        // Assert
        assertEquals("testuser", username);
    }

    @Test
    void validateToken_WithInvalidToken_ShouldThrowException() {
        // Act & Assert
        assertThrows(JwtException.class, () -> {
            jwtService.validateToken(invalidToken);
        });
    }
    
    @Test
    void extractUsername_WithInvalidToken_ShouldThrowException() {
        // Act & Assert
        assertThrows(JwtException.class, () -> {
            jwtService.extractUsername(invalidToken);
        });
    }
    
    @Test
    void isRefreshToken_WithInvalidToken_ShouldThrowException() {
        // Act & Assert
        assertThrows(JwtException.class, () -> {
            jwtService.isRefreshToken(invalidToken);
        });
    }
    
    @Test
    void generateTokenFromRefreshToken_WithInvalidToken_ShouldThrowException() {
        // Act & Assert
        assertThrows(JwtException.class, () -> {
            jwtService.generateTokenFromRefreshToken(invalidToken);
        });
    }
    
    @Test
    void validateToken_WithTamperedToken_ShouldThrowException() {
        // Arrange
        String tamperedToken = "tampered.token.value";
        when(jwtDecoder.decode(tamperedToken)).thenThrow(new JwtException("Token has been tampered with"));
        
        // Act & Assert
        assertThrows(JwtException.class, () -> {
            jwtService.validateToken(tamperedToken);
        });
    }
}