package edu.ucdavis.dss.ipa;

import edu.ucdavis.dss.ipa.config.AutowireHelper;
import edu.ucdavis.dss.ipa.config.JwtFilter;
import edu.ucdavis.dss.ipa.exceptions.handlers.MvcExceptionHandler;
import edu.ucdavis.dss.ipa.security.SecurityHeaderFilter;
import org.apereo.cas.client.util.HttpServletRequestWrapperFilter;
import org.apereo.cas.client.validation.Cas20ProxyReceivingTicketValidationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

@EnableScheduling
@SpringBootApplication
public class Application {
    @Value("${IPA_URL_API}")
    String ipaUrlApi;

    @Value("${IPA_JWT_SIGNING_KEY}")
    String jwtSigningKey;

    /* e.g. https://cas.ucdavis.edu/cas */
    @Value("${CAS_URL}")
    String casUrl;

    /**
     * Configure JWT.
     */
    @Bean
    public FilterRegistrationBean jwtFilter() {
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        JwtFilter jwtFilter = new JwtFilter();

        jwtFilter.setJwtSigningKey(jwtSigningKey);

        registrationBean.setFilter(jwtFilter);
        registrationBean.addUrlPatterns("/api/*");

        return registrationBean;
    }

    /**
     * Configure basic security headers.
     */
    @Bean
    public FilterRegistrationBean securityHeaders() {
        final FilterRegistrationBean registrationBean = new FilterRegistrationBean();

        registrationBean.setFilter(new SecurityHeaderFilter());
        registrationBean.addUrlPatterns("/*");

        return registrationBean;
    }

    /**
     * Configure CAS.
     */
    @Bean
    @Profile({"production", "staging", "development"})
    public FilterRegistrationBean cas20Registration() {
        FilterRegistrationBean cas20 = new FilterRegistrationBean();

        cas20.setFilter(new Cas20ProxyReceivingTicketValidationFilter());
        cas20.addUrlPatterns("/login", "/post-login");
        cas20.addInitParameter("casServerUrlPrefix", casUrl);
        cas20.addInitParameter("serverName", ipaUrlApi);
        cas20.addInitParameter("encoding", "UTF-8");

        return cas20;
    }

    /**
     * Expose AutowireHelper for BaseEntity class.
     */
    @Bean
    public AutowireHelper autowireHelper() {
        return AutowireHelper.getInstance();
    }

    /**
     * Set up CAS wrapper.
     */
    @Bean
    public FilterRegistrationBean casRequestWrapper() {
        FilterRegistrationBean requestWrapper = new FilterRegistrationBean();

        requestWrapper.setFilter(new HttpServletRequestWrapperFilter());
        requestWrapper.addUrlPatterns("/login", "/post-login");

        return requestWrapper;
    }

    /**
     * Configure exception handlers.
     */
    @Bean
    public SimpleMappingExceptionResolver webExceptionResolver() {
        MvcExceptionHandler resolver = new MvcExceptionHandler();

        resolver.setExcludedExceptions(AccessDeniedException.class);
        resolver.setDefaultStatusCode(500);

        return resolver;
    }

    /**
     * Enable CORS requests for all controllers.
     * Note CORS will only be allowed by the domains specified in
     * .allowedOrigins().
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedOrigins("http://localhost:9000", "https://test-ipa.dss.ucdavis.edu", "https://ipa.ucdavis.edu").allowCredentials(true).allowedMethods("*");
            }
        };
    }

    public static void main(final String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
}
