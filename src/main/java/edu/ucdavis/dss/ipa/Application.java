package edu.ucdavis.dss.ipa;

import edu.ucdavis.dss.ipa.config.JwtFilter;
import edu.ucdavis.dss.ipa.security.SecurityHeaderFilter;
import org.jasig.cas.client.util.HttpServletRequestWrapperFilter;
import org.jasig.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

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
        cas20.addUrlPatterns("/auth/*");
        cas20.addInitParameter("casServerUrlPrefix", "https://cas.ucdavis.edu/cas");
        cas20.addInitParameter("serverName", "http://localhost:8080");
        cas20.addInitParameter("encoding", "UTF-8");
        return cas20;
    }

    @Bean
    public FilterRegistrationBean casRequestWrapper() {
        FilterRegistrationBean requestWrapper = new FilterRegistrationBean();
        requestWrapper.setFilter(new HttpServletRequestWrapperFilter());
        requestWrapper.addUrlPatterns("/auth/*");
        return requestWrapper;
    }

    /**
     * Ensures environment variables required by application.properties are set.
     *
     * Returns true if all required environment variables are found.
     */
    private static boolean VerifyEnvironment() {
        boolean errorsFound = false;

        if(System.getenv("ipa.logging.level") == null) {
            System.err.println("Environment variable 'ipa.logging.level' must be set (e.g. INFO, DEBUG)");
            errorsFound = true;
        }
        if(System.getenv("ipa.datasource.url") == null) {
            System.err.println("Environment variable 'ipa.datasource.url' must be set (e.g. jdbc:mysql://localhost:3306/IPA)");
            errorsFound = true;
        }
        if(System.getenv("ipa.datasource.username") == null) {
            System.err.println("Environment variable 'ipa.datasource.username' must be set");
            errorsFound = true;
        }
        if(System.getenv("ipa.datasource.password") == null) {
            System.err.println("Environment variable 'ipa.datasource.password' must be set");
            errorsFound = true;
        }
        if(System.getenv("ipa.spring.profile") == null) {
            System.err.println("Environment variable 'ipa.spring.profile' must be set (e.g. development)");
            errorsFound = true;
        }

        return !errorsFound;
    }

    public static void main(final String[] args) throws Exception {
        if(VerifyEnvironment()) {
            SpringApplication.run(Application.class, args);
        } else {
            System.err.println("\nApplication will not run until the above errors are addressed.");
        }
    }
}
