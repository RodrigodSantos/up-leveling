package io.github.rodrigodsantos.upleveling;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Os testes rodam num PostgreSQL real e descartável, no Docker (o mesmo banco da produção, ao contrário do H2).
 * O {@code @ServiceConnection} aponta o datasource para o container automaticamente.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgres() {
        return new PostgreSQLContainer("postgres:17-alpine");
    }
}
