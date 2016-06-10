package edu.ucdavis.dss.ipa.config;

import edu.ucdavis.dss.ipa.security.CasEntryPoint;
import edu.ucdavis.dss.ipa.security.JwtCasFilter;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.jasig.cas.client.validation.TicketValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.inject.Inject;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final String[] UNRESTRICTED_PATHS = new String[] {
            "/error", "/pre-login", "/login"
    };

//    @Inject
//    private Environment env;

    @Inject
    private UserDetailsService userDetailsService;

//    @Inject
//    private JwtService jwtService;
//
//    @Inject
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
//        auth.authenticationProvider(provider);
//    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers("/favicon.ico")
                .antMatchers("/client/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(casEntryPoint())
                .and()
                .authorizeRequests()
                .antMatchers(UNRESTRICTED_PATHS).anonymous()
                .antMatchers("/post-login").authenticated()
                .anyRequest().hasAuthority("VIEW_PEOPLE")
                .and()
                .addFilterBefore(
                        jwtCasFilter(),
                        UsernamePasswordAuthenticationFilter.class
                )
                .csrf()
                .disable()
                .headers()
                .xssProtection()
                .and()
                .frameOptions()
        ;
    }

    private JwtCasFilter jwtCasFilter() throws Exception {
        TicketValidator ticketValidator = new Cas20ServiceTicketValidator("https://cas.ucdavis.edu/cas/"); //env.getProperty("security.casUrl"));

        return new JwtCasFilter.Builder()
                .ignoreAntPaths(UNRESTRICTED_PATHS)
                .setCasEntryPoint(casEntryPoint())
                .setTicketValidator(ticketValidator)
                .setUserDetailsService(userDetailsService)
                //.setJwtService(jwtService)
                .setAuthenticationManager(authenticationManager())
                .build();
    }

    @Bean
    public CasEntryPoint casEntryPoint() {
        return new CasEntryPoint(
                "/pre-login",
                "https://cas.ucdavis.edu/cas/login", //env.getProperty("security.casUrl") + "/login",
                "http://localhost:8080/auth/processCas" //env.getProperty("app.url") + "/post-login"
        );
    }
}