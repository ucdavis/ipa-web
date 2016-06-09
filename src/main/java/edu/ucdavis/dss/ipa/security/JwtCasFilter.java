package edu.ucdavis.dss.ipa.security;

import edu.ucdavis.dss.ipa.entities.AuthenticationPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.jasig.cas.client.validation.Assertion;
import org.jasig.cas.client.validation.TicketValidationException;
import org.jasig.cas.client.validation.TicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class JwtCasFilter extends AbstractAuthenticationProcessingFilter {

    private static final Logger LOG = LoggerFactory.getLogger(JwtCasFilter.class);

    public static final String CORRUPT_TOKEN = "CORRUPT_TOKEN";

    private TicketValidator ticketValidator;
    //private JwtService jwtService;
    private UserDetailsService userDetailsService;
    private CasEntryPoint casEntryPoint;

    private List<RequestMatcher> ignorePaths = new ArrayList<>(); //Lists.newArrayList();

    private JwtCasFilter() { super("/**"); }

    @Override
    protected boolean requiresAuthentication(HttpServletRequest request, HttpServletResponse response) {
        return !ignorePaths.stream().anyMatch(path -> path.matches(request));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException
    {
        //String jwtToken = request.getHeader(JwtService.AUTH_HEADER_NAME);
        AuthenticationToken authToken = null;
        AuthenticationException authException = null;

        final String authHeader = request.getHeader("Authorization");

        try {
            if(authHeader == null) {
                final String oneTimeCasTicket = request.getParameter(ServiceProperties.DEFAULT_CAS_ARTIFACT_PARAMETER);

                if(isEmpty(oneTimeCasTicket) == false) {
                    final Assertion assertion = ticketValidator.validate(oneTimeCasTicket, casEntryPoint.getReturnUrl());

                    final String username = assertion.getPrincipal().getName();

                    final UserDetails userDetails = (UserDetails)userDetailsService.loadUserByUsername(username);

                    authToken = new AuthenticationToken(null);
                } else {
                    // No CAS ticket, no auth header, so redirect to CAS
                    SecurityContextHolder.clearContext();
                    casEntryPoint.commence(request, response, authException);

                    return null;
                }
            } else {
                if(authHeader.startsWith("Bearer ") == false) {
                    authException = new AuthenticationServiceException("Invalid Authorization header.");
                } else {
                    final String token = authHeader.substring(7); // The part after "Bearer "

                    try {
                        final Claims claims = Jwts.parser().setSigningKey("secretkey")
                                .parseClaimsJws(token).getBody();
                        request.setAttribute("claims", claims);

                        // TODO: get the AuthToken from here
                    } catch (final SignatureException e) {
                        authException = new AuthenticationServiceException("Provided JWT token is invalid.");
                    }
                }
            }
        } catch(TicketValidationException e) {
            authException = new AuthenticationServiceException("Invalid CAS Ticket", e);
        }

//        try {
//            if (isEmpty(jwtToken)) {
//                final String oneTimeCasTicket = request.getParameter(ServiceProperties.DEFAULT_CAS_ARTIFACT_PARAMETER);
//
//                if (!isEmpty(oneTimeCasTicket)) {
//                    final Assertion assertion = ticketValidator.validate(oneTimeCasTicket, casEntryPoint.getReturnUrl());
//
//                    final String username = assertion.getPrincipal().getName();
//
//                    final AuthUserDetails userDetails = (AuthUserDetails) userDetailsService.loadUserByUsername(username);
//
//                    authToken = new AuthenticationToken(userDetails);
//                }
//            } else {
//                authToken = jwtService.deserialize(jwtToken);
//            }
//
//            if (authToken == null || !authToken.isAuthenticated()) {
//                authException = new AuthenticationServiceException("Invalid CAS Ticket");
//            }
//        } catch (InvalidCiphertextException e) {
//            LOG.warn("Invalid JWT decryption with AWS", e);
//            SecurityContextHolder.clearContext();
//            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, CORRUPT_TOKEN);
//
//            return null;
//        } catch (TicketValidationException e) {
//            authException = new AuthenticationServiceException("Invalid CAS Ticket", e);
//        }
//
//        if (authException != null) {
//            SecurityContextHolder.clearContext();
//            casEntryPoint.commence(request, response, authException);
//
//            return null;
//        }
//
//        return authToken;

        return authToken;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authToken)
            throws IOException, ServletException
    {
        LOG.debug("Authentication success for {}", authToken);

        SecurityContextHolder.getContext().setAuthentication(authToken);

        chain.doFilter(request, response);
    }

    public static class Builder {
        private final JwtCasFilter filter;

        public Builder() { filter = new JwtCasFilter(); }

        public Builder setTicketValidator(TicketValidator ticketValidator) {
            filter.ticketValidator = ticketValidator;
            return this;
        }

//        public Builder setJwtService(JwtService jwtService) {
//            filter.jwtService = jwtService;
//            return this;
//        }

        public Builder setUserDetailsService(UserDetailsService userDetailsService) {
            filter.userDetailsService = userDetailsService;
            return this;
        }

        public Builder setCasEntryPoint(CasEntryPoint casEntryPoint) {
            filter.casEntryPoint = casEntryPoint;
            return this;
        }

        public Builder setAuthenticationManager(AuthenticationManager authenticationManager) {
            filter.setAuthenticationManager(authenticationManager);
            return this;
        }

        public Builder ignoreAntPaths(String... paths) {
            filter.ignorePaths = Arrays.asList(paths)
                    .stream()
                    .map(AntPathRequestMatcher::new)
                    .collect(toList());

            return this;
        }

        public JwtCasFilter build() {
//            Preconditions.checkNotNull(filter.ticketValidator);
//            Preconditions.checkNotNull(filter.jwtService);
//            Preconditions.checkNotNull(filter.userDetailsService);
//            Preconditions.checkNotNull(filter.casEntryPoint);

            return filter;
        }
    }
}
