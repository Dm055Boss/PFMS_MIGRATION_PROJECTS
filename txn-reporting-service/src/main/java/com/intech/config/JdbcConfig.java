package com.intech.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

/**
 * JDBC wiring.
 *
 * Spring Boot auto-configures JdbcTemplate. We also expose NamedParameterJdbcTemplate
 * for queries that use named parameters (e.g., :txndate, :foracid).
 */
@Configuration
public class JdbcConfig {

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        // Use the same DataSource as JdbcTemplate.
        return new NamedParameterJdbcTemplate(dataSource);
    }
}
