package edu.ucdavis.dss.ipa.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Declares the MySQL-backed {@link NamedParameterJdbcTemplate} as {@code @Primary}. Defining the
 * Datamart and Banner templates makes Spring Boot's auto-configured one back off, so the MySQL one
 * must be declared explicitly. Datamart and Banner consumers must use their @Qualifier'd templates.
 */
@Configuration
@Profile({"development", "production", "staging"})
@ConditionalOnNotWebApplication
public class PrimaryJdbcTemplateConfiguration {
    @Bean
    @Primary
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(JdbcTemplate jdbcTemplate) {
        return new NamedParameterJdbcTemplate(jdbcTemplate);
    }
}
