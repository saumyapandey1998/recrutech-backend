package com.recrutech.recrutechauth.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Test configuration for setting up Testcontainers.
 * This class provides a MySQL container for integration tests.
 */
@TestConfiguration
public class TestContainersConfig {

    private static final MySQLContainer<?> mysqlContainer;

    static {
        mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
                .withDatabaseName("testdb")
                .withUsername("testuser")
                .withPassword("testpassword")
                .withReuse(true);
        mysqlContainer.start();
    }

    /**
     * Creates and configures a MySQL container for testing.
     *
     * @return A configured MySQL container
     */
    @Bean
    public MySQLContainer<?> mySQLContainer() {
        return mysqlContainer;
    }

    /**
     * Get the JDBC URL for the MySQL container.
     *
     * @return The JDBC URL
     */
    public static String getJdbcUrl() {
        return mysqlContainer.getJdbcUrl();
    }

    /**
     * Get the username for the MySQL container.
     *
     * @return The username
     */
    public static String getUsername() {
        return mysqlContainer.getUsername();
    }

    /**
     * Get the password for the MySQL container.
     *
     * @return The password
     */
    public static String getPassword() {
        return mysqlContainer.getPassword();
    }
}
