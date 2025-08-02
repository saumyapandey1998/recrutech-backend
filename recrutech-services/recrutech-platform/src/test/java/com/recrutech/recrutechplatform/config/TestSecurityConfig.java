package com.recrutech.recrutechplatform.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test security configuration for controller tests.
 * Disables CSRF protection and allows all requests for testing REST APIs.
 * Excludes OAuth2 resource server auto-configuration to avoid complex dependencies in tests.
 */
@TestConfiguration
@EnableAutoConfiguration(exclude = {OAuth2ResourceServerAutoConfiguration.class})
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) 
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll());

        return http.build();
    }
}
