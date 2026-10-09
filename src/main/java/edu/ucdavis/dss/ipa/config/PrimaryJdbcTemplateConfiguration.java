package edu.ucdavis.dss.ipa.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Declares the MySQL-backed {@link NamedParameterJdbcTemplate} as {@code @Primary}.
 *
 * Defining any custom NamedParameterJdbcTemplate bean (the Datamart or Banner Oracle ones) makes
 * Spring Boot's auto-configured NamedParameterJdbcTemplate back off, so the MySQL one must be
 * declared explicitly. This lives in its own config — gated on either external Oracle URL being
 * present — so it is declared exactly once whether one or both Oracle connections are active
 * (declaring it inside both DatamartConfiguration and BannerConfiguration would collide when both
 * URLs are set, e.g. the budget reconciliation report). Datamart/Banner consumers must use their
 * @Qualifier'd templates.
 */
@Configuration
@Profile({"development", "production", "staging"})
@ConditionalOnExpression("'${DATAMART_URL:}' != '' or '${BANNER_DATABASE_URL:}' != ''")
@ConditionalOnNotWebApplication
public class PrimaryJdbcTemplateConfiguration {
    @Bean
    @Primary
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(JdbcTemplate jdbcTemplate) {
        return new NamedParameterJdbcTemplate(jdbcTemplate);
    }
}
