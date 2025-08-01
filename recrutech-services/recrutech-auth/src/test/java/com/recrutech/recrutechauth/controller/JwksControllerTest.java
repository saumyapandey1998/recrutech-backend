package com.recrutech.recrutechauth.controller;

import com.recrutech.recrutechauth.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for JwksController.
 * Tests the JWK Set endpoint functionality.
 */
@WebMvcTest(JwksController.class)
@Import({TestSecurityConfig.class, JwksControllerTest.JwksTestConfig.class})
class JwksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class JwksTestConfig {
        
        @Bean
        public KeyPair keyPair() throws NoSuchAlgorithmException {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        }
        
        @Bean
        public String jwtId() {
            return "test-jwt-id";
        }
    }

    @Test
    void testJwksEndpoint_ShouldReturnJwkSet() throws Exception {
        System.out.println("[DEBUG_LOG] Testing JWKs endpoint accessibility");
        
        mockMvc.perform(get("/api/oauth2/jwks")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.keys").exists())
                .andExpect(jsonPath("$.keys").isArray())
                .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
                .andExpect(jsonPath("$.keys[0].alg").value("RS256"))
                .andExpect(jsonPath("$.keys[0].use").value("sig"))
                .andExpect(jsonPath("$.keys[0].kid").exists())
                .andExpect(jsonPath("$.keys[0].n").exists())
                .andExpect(jsonPath("$.keys[0].e").exists());
        
        System.out.println("[DEBUG_LOG] JWKs endpoint test completed successfully");
    }

    @Test
    void testJwksEndpoint_ShouldBeAccessibleWithoutAuthentication() throws Exception {
        System.out.println("[DEBUG_LOG] Testing JWKs endpoint accessibility without authentication");
        
        // This test verifies that the JWKs endpoint is publicly accessible
        // without requiring authentication, which is necessary for OAuth2 Resource Servers
        mockMvc.perform(get("/api/oauth2/jwks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        
        System.out.println("[DEBUG_LOG] JWKs endpoint is publicly accessible");
    }

    @Test
    void testJwksEndpoint_ShouldReturnConsistentResponse() throws Exception {
        System.out.println("[DEBUG_LOG] Testing JWKs endpoint consistency");
        
        // Make multiple requests to ensure the endpoint returns consistent results
        String firstResponse = mockMvc.perform(get("/api/oauth2/jwks"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String secondResponse = mockMvc.perform(get("/api/oauth2/jwks"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // The responses should be identical since we're using the same key pair
        assert firstResponse.equals(secondResponse) : "JWKs endpoint should return consistent responses";
        
        System.out.println("[DEBUG_LOG] JWKs endpoint returns consistent responses");
    }
}