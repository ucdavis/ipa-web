package edu.ucdavis.dss.ipa.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

@Configuration
@Profile({"development", "production", "staging"})
@ConditionalOnNotWebApplication
public class DatamartConfiguration {
    /* host:port/service_name, e.g. example.rds.aws.ait.ucdavis.edu:1521/service */
    @Value("${DATAMART_URL}")
    String datamartUrl;

    @Value("${DATAMART_USERNAME}")
    String datamartUsername;

    @Value("${DATAMART_PASSWORD}")
    String datamartPassword;

    // The @Primary MySQL NamedParameterJdbcTemplate that must accompany this custom template lives
    // in PrimaryJdbcTemplateConfiguration, so it is declared once whether Datamart, Banner, or both
    // Oracle connections are active.

    // The datamart DataSource must not be exposed as a bean, or Spring Boot's
    // autoconfiguration of the primary (MySQL) datasource will back off.
    @Bean
    public NamedParameterJdbcTemplate datamartJdbcTemplate() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:oracle:thin:@//" + datamartUrl);
        dataSource.setUsername(datamartUsername);
        dataSource.setPassword(datamartPassword);
        dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        dataSource.setMaximumPoolSize(2);
        dataSource.setMinimumIdle(0);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        // Oracle's default fetch size is 10 rows per round trip
        jdbcTemplate.setFetchSize(500);

        return new NamedParameterJdbcTemplate(jdbcTemplate);
    }
}
