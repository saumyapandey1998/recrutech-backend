package com.recrutech.recrutechauth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for JWT token generation and validation.
 */
@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshExpiration;

    /**
     * Constructor for JwtService.
     *
     * @param encoder the JWT encoder
     * @param decoder the JWT decoder
     */
    public JwtService(JwtEncoder encoder, JwtDecoder decoder) {
        this.encoder = encoder;
        this.decoder = decoder;
    }

    /**
     * Generates a JWT token for the given authentication.
     *
     * @param authentication the authentication object
     * @return the JWT token
     */
    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();
        String scope = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(jwtExpiration, ChronoUnit.MILLIS))
                .subject(authentication.getName())
                .claim("scope", scope)
                .build();

        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * Generates a refresh token for the given authentication.
     *
     * @param authentication the authentication object
     * @return the refresh token
     */
    public String generateRefreshToken(Authentication authentication) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(refreshExpiration, ChronoUnit.MILLIS))
                .subject(authentication.getName())
                .claim("token_type", "refresh")
                .build();

        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
    
    /**
     * Validates a JWT token and returns the Jwt object.
     *
     * @param token the JWT token to validate
     * @return the validated Jwt object
     * @throws JwtException if the token is invalid
     */
    public Jwt validateToken(String token) {
        return decoder.decode(token);
    }
    
    /**
     * Extracts the username from a JWT token.
     *
     * @param token the JWT token
     * @return the username
     */
    public String extractUsername(String token) {
        Jwt jwt = validateToken(token);
        return jwt.getSubject();
    }
    
    /**
     * Checks if a JWT token is expired.
     *
     * @param token the JWT token
     * @return true if the token is expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        Jwt jwt = validateToken(token);
        return jwt.getExpiresAt().isBefore(Instant.now());
    }
    
    /**
     * Checks if a JWT token is a refresh token.
     *
     * @param token the JWT token
     * @return true if the token is a refresh token, false otherwise
     */
    public boolean isRefreshToken(String token) {
        Jwt jwt = validateToken(token);
        Map<String, Object> claims = jwt.getClaims();
        return "refresh".equals(claims.get("token_type"));
    }
    
    /**
     * Generates a new access token from a refresh token.
     *
     * @param refreshToken the refresh token
     * @return the new access token
     * @throws JwtException if the refresh token is invalid
     */
    public String generateTokenFromRefreshToken(String refreshToken) {
        // Validate the refresh token
        Jwt jwt = validateToken(refreshToken);
        
        // Check if it's a refresh token
        if (!isRefreshToken(refreshToken)) {
            throw new JwtException("Not a refresh token");
        }
        
        // Check if it's expired
        if (isTokenExpired(refreshToken)) {
            throw new JwtException("Refresh token expired");
        }
        
        // Extract username
        String username = jwt.getSubject();
        
        // Generate new access token
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(jwtExpiration, ChronoUnit.MILLIS))
                .subject(username)
                .build();
        
        return this.encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}