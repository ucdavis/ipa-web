package edu.ucdavis.dss.ipa;

import edu.ucdavis.dss.ipa.config.JwtFilter;
import edu.ucdavis.dss.ipa.exceptions.handlers.MvcExceptionHandler;
import edu.ucdavis.dss.ipa.security.SecurityHeaderFilter;
import org.jasig.cas.client.util.HttpServletRequestWrapperFilter;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

@EnableScheduling
@SpringBootApplication
public class Application {
    @Value("${ipa.url.api}")
    String ipaUrlApi;

    @Value("${ipa.jwt.signingkey}")
    String jwtSigningKey;

    // Configure JWT
    @Bean
    public FilterRegistrationBean jwtFilter() {
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        JwtFilter jwtFilter = new JwtFilter();

        jwtFilter.setJwtSigningKey(jwtSigningKey);

        registrationBean.setFilter(jwtFilter);
        registrationBean.addUrlPatterns("/api/*");

        return registrationBean;
    }

    // Configure basic security headers
    @Bean
    public FilterRegistrationBean securityHeaders() {
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();

        registrationBean.setFilter(new SecurityHeaderFilter());
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }

    // Configure CAS
    @Bean
    @Profile({"production", "staging", "development"})
    public FilterRegistrationBean cas20Registration() {
        FilterRegistrationBean cas20 = new FilterRegistrationBean();

        cas20.setFilter(new Cas20ProxyReceivingTicketValidationFilter());
        cas20.addUrlPatterns("/login", "/post-login");
        cas20.addInitParameter("casServerUrlPrefix", "https://cas.ucdavis.edu/cas");
        cas20.addInitParameter("serverName", ipaUrlApi);
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

    // Configure exception handlers
    @Bean
    public SimpleMappingExceptionResolver webExceptionResolver() {
        MvcExceptionHandler resolver = new MvcExceptionHandler();

        resolver.setExcludedExceptions(AccessDeniedException.class);
        resolver.setDefaultStatusCode(500);

        return resolver;
    }

    public static void main(final String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
