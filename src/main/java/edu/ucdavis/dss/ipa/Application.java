package edu.ucdavis.dss.ipa;

import edu.ucdavis.dss.ipa.config.JwtFilter;
import edu.ucdavis.dss.ipa.config.SettingsConfiguration;
import edu.ucdavis.dss.ipa.security.SecurityHeaderFilter;
import org.apache.coyote.http2.Setting;
import org.jasig.cas.client.util.HttpServletRequestWrapperFilter;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class Application {
    @Bean
    public FilterRegistrationBean jwtFilter() {
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new JwtFilter());
        registrationBean.addUrlPatterns("/api/*");

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean securityHeaders() {
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(new SecurityHeaderFilter());
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean cas20Registration() {
        FilterRegistrationBean cas20 = new FilterRegistrationBean();
        cas20.setFilter(new Cas20ProxyReceivingTicketValidationFilter());
        cas20.addUrlPatterns("/login", "/post-login");
        cas20.addInitParameter("casServerUrlPrefix", "https://cas.ucdavis.edu/cas");
        cas20.addInitParameter("serverName", SettingsConfiguration.getIpaURL());
        cas20.addInitParameter("encoding", "UTF-8");
        return cas20;
    }

    @Bean
    public FilterRegistrationBean casRequestWrapper() {
        FilterRegistrationBean requestWrapper = new FilterRegistrationBean();
        requestWrapper.setFilter(new HttpServletRequestWrapperFilter());
        requestWrapper.addUrlPatterns("/login", "/post-login");
        return requestWrapper;
    }

    public static void main(final String[] args) throws Exception {
        SettingsConfiguration.loadSettings();

        if(SettingsConfiguration.isValid()) {
            SpringApplication.run(Application.class, args);
        } else {
            System.err.println("\nApplication will not run until the above errors are addressed.");
        }
    }
}
