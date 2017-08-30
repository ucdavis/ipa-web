package edu.ucdavis.dss.ipa;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.core.Authentication;

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

        return cas20;
    }
}
