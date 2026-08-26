package com.rikkeiexpress.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rikkeiexpress.ai.util.SafeSqlValidator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DatabaseService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public DatabaseService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = new ObjectMapper();
    }

    public String getSchema() {
        return """
        [SCHEMA INFO]
        Table 1: post_offices
          - id VARCHAR(10) PRIMARY KEY
          - name VARCHAR(100) NOT NULL
          - city VARCHAR(50) NOT NULL
          - region VARCHAR(20) NOT NULL
          
        Table 2: deliveries
          - id VARCHAR(20) PRIMARY KEY
          - tracking_code VARCHAR(20) UNIQUE NOT NULL
          - post_office_id VARCHAR(10) REFERENCES post_offices(id)
          - status VARCHAR(20) NOT NULL -- Values: 'DELIVERED', 'DELAYED', 'IN_TRANSIT', 'CANCELLED'
          - created_at TIMESTAMP NOT NULL
          - delivered_at TIMESTAMP
          - shipping_fee DECIMAL(10,2) NOT NULL
        """;
    }

    public String executeQuery(String sqlQuery) {
        try {
            SafeSqlValidator.validate(sqlQuery);
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sqlQuery);
            return objectMapper.writeValueAsString(results);
        } catch (SecurityException se) {
            return "{\"error\": \"Security Exception: " + se.getMessage() + "\"}";
        } catch (Exception e) {
            return "{\"error\": \"Execution Error: " + e.getMessage() + "\"}";
        }
    }
}