package com.recrutech.recrutechplatform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

/**
 * Security configuration for the recrutech-platform service.
 * Configures JWT-based authentication as a resource server.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.auth.jwt.public-key-location:}")
    private String publicKeyLocation;

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:http://localhost:8082}")
    private String issuerUri;

    /**
     * Configures the security filter chain for JWT authentication.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> 
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                    // Öffentliche Endpoints
                    .requestMatchers("/actuator/health", "/api/v1/jobs").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/jobs/**").permitAll()
                    // Geschützte Endpoints - nur HR Personal
                    .requestMatchers(HttpMethod.POST, "/api/v1/jobs").hasRole("HR")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/jobs/**").hasRole("HR")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/jobs/**").hasRole("HR")
                    .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }

    /**
     * JWT Decoder Bean - uses JWK Set URI directly to avoid startup dependency on auth service.
     * This approach is more resilient and doesn't require the auth service to be running during startup.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Use JWK Set URI directly instead of issuer location to avoid startup dependency
        // Auth service uses /api context path, so JWK endpoint is at /api/oauth2/jwks
        return NimbusJwtDecoder.withJwkSetUri(issuerUri + "/api/oauth2/jwks")
                .jwsAlgorithm(org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256)
                .build();
    }

    /**
     * CORS configuration to allow frontend access.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Auth-Token"));
        configuration.setExposedHeaders(Arrays.asList("X-Auth-Token"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}