package com.thehalo.halobackend.actuator;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Custom health indicator for database connectivity
 */
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    private final DataSource dataSource;
    
    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection()) {
            if (conn.isValid(1)) {
                // Execute test query
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT 1")) {
                    
                    if (rs.next()) {
                        String databaseProductName = conn.getMetaData().getDatabaseProductName();
                        String databaseVersion = conn.getMetaData().getDatabaseProductVersion();
                        
                        return Health.up()
                            .withDetail("database", databaseProductName)
                            .withDetail("version", databaseVersion)
                            .withDetail("status", "Connected")
                            .withDetail("validationQuery", "SELECT 1")
                            .withDetail("responseTime", "< 1s")
                            .build();
                    }
                }
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("errorType", e.getClass().getSimpleName())
                .withException(e)
                .build();
        }
        
        return Health.down()
            .withDetail("error", "Database connection validation failed")
            .build();
    }
}
