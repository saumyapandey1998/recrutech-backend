package com.recrutech.recrutechplatform.controller;

import com.recrutech.recrutechplatform.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

@WebMvcTest
@Import(TestSecurityConfig.class)
public class TestReproduction {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testSecurityConfig() {
        // This should trigger the HttpSecurity autowiring issue
    }
}