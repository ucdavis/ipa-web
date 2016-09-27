package edu.ucdavis.dss.ipa;

import edu.ucdavis.dss.ipa.api.helpers.MultiReadServletFilter;
import edu.ucdavis.dss.ipa.config.JwtFilter;
import edu.ucdavis.dss.ipa.config.SettingsConfiguration;
import edu.ucdavis.dss.ipa.exceptions.handlers.MvcExceptionHandler;
import edu.ucdavis.dss.ipa.security.SecurityHeaderFilter;
import org.jasig.cas.client.util.HttpServletRequestWrapperFilter;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import java.util.Properties;

@EnableScheduling
@SpringBootApplication
public class Application {
    // Configure JWT
    @Bean
    public FilterRegistrationBean jwtFilter() {
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();

        registrationBean.setFilter(new JwtFilter());
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

    // Configure exception handlers
    @Bean
    public SimpleMappingExceptionResolver webExceptionResolver() {
        MvcExceptionHandler resolver = new MvcExceptionHandler();

        //Properties mappings = new Properties();
        //mappings.setProperty("AccessDeniedException", "../errors/403");
        //resolver.setExceptionMappings(mappings);

        resolver.setExcludedExceptions(AccessDeniedException.class);
        //resolver.setDefaultErrorView("../errors/unhandled-exception");
        resolver.setDefaultStatusCode(500);

        return resolver;
    }

    // Configure the request wrapper filter so our exception handler
    // can read the servlet input stream after it has already been read
//    @Bean
//    public FilterRegistrationBean requestWrapperFilter() {
//        FilterRegistrationBean requestWrapper = new FilterRegistrationBean();
//
//        requestWrapper.setFilter(new MultiReadServletFilter());
//        requestWrapper.setOrder(-10000);
//
//        return requestWrapper;
//    }

    public static void main(final String[] args) throws Exception {
        SettingsConfiguration.loadSettings();

        if(SettingsConfiguration.isValid()) {
            SpringApplication.run(Application.class, args);
        } else {
            System.err.println("\nApplication will not run until the above errors are addressed.");
        }
    }
}
