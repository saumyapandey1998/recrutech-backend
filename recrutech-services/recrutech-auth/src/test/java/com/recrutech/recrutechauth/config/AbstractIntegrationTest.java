package com.recrutech.recrutechauth.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Abstract base class for integration tests.
 * Sets up a MySQL container for testing and configures Spring to use it.
 */
@SpringBootTest
@ActiveProfiles("testcontainers")
@Import(TestContainersConfig.class)
public abstract class AbstractIntegrationTest {

    /**
     * Dynamically sets the database connection properties for the test.
     *
     * @param registry The property registry
     */
    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("TESTCONTAINERS_MYSQL_URL", TestContainersConfig::getJdbcUrl);
        registry.add("TESTCONTAINERS_MYSQL_USERNAME", TestContainersConfig::getUsername);
        registry.add("TESTCONTAINERS_MYSQL_PASSWORD", TestContainersConfig::getPassword);
    }
}
