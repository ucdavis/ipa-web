package edu.ucdavis.dss.ipa.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Oracle connection to the Banner student system (course/TA assignment data). Mirrors
 * DatamartConfiguration: exposed only as the qualified {@code bannerJdbcTemplate} bean so it never
 * displaces the primary MySQL datasource; the accompanying @Primary MySQL template is declared in
 * PrimaryJdbcTemplateConfiguration. Gated so the web service runs without Banner credentials.
 */
@Configuration
@Profile({"development", "production", "staging"})
@ConditionalOnNotWebApplication
public class BannerConfiguration {
    /* SID form host:port:SID (Banner uses a SID, not a service name); code prepends
       jdbc:oracle:thin:@. For a service name instead, set //host:port/service_name here. */
    @Value("${BANNER_DATABASE_URL}")
    String bannerUrl;

    @Value("${BANNER_DATABASE_USERNAME}")
    String bannerUsername;

    @Value("${BANNER_DATABASE_PASSWORD}")
    String bannerPassword;

    @Bean
    public NamedParameterJdbcTemplate bannerJdbcTemplate() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl("jdbc:oracle:thin:@" + bannerUrl);
        dataSource.setUsername(bannerUsername);
        dataSource.setPassword(bannerPassword);
        dataSource.setDriverClassName("oracle.jdbc.OracleDriver");
        dataSource.setMaximumPoolSize(2);
        dataSource.setMinimumIdle(0);

        return new NamedParameterJdbcTemplate(new JdbcTemplate(dataSource));
    }
}
