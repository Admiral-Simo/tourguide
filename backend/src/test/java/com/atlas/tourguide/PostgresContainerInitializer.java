package com.atlas.tourguide;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * This interface defines a singleton Testcontainer that will be started once and shared across all
 * test classes that implement it.
 */
public interface PostgresContainerInitializer {
  PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:latest");

  static void startContainer() {
    POSTGRES.start();
  }

  @DynamicPropertySource
  static void setDatasourceProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", POSTGRES::getUsername);
    registry.add("spring.datasource.password", POSTGRES::getPassword);
    registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    registry.add("jwt.secret", () -> "a-very-long-and-secure-secret-key-for-testing-purposes");
  }
}