package edu.ucdavis.dss.ipa;

import edu.ucdavis.dss.ipa.config.SettingsConfiguration;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.core.Authentication;

@SpringBootConfiguration
@Profile("test")
public class TestConfiguration {
    // Configure CAS
    @Bean
    public FilterRegistrationBean cas20Registration() {
        FilterRegistrationBean cas20 = new FilterRegistrationBean();

        CasAuthenticationFilter filter = new CasAuthenticationFilter();
        filter.setAuthenticationManager(new AuthenticationManager() {
            public Authentication authenticate(Authentication a) {
                return a;
            }
        });

        cas20.setFilter(filter);
//        cas20.setFilter(new Cas20ProxyReceivingTicketValidationFilter());
//        cas20.addUrlPatterns("/login", "/post-login");
//        cas20.addInitParameter("casServerUrlPrefix", "https://cas.ucdavis.edu/cas");
//        cas20.addInitParameter("serverName", SettingsConfiguration.getIpaApiURL());
//        cas20.addInitParameter("encoding", "UTF-8");

        return cas20;
    }
}
