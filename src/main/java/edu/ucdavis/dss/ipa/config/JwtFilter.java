package edu.ucdavis.dss.ipa.config;

import edu.ucdavis.dss.ipa.security.Authorization;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.GenericFilterBean;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.stream.Collectors;

public class JwtFilter extends GenericFilterBean {
    private String jwtSigningKey;

    @Override
    public void doFilter(final ServletRequest req,
                         final ServletResponse res,
                         final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;
        final HttpServletResponse response = (HttpServletResponse) res;

        ServletContext servletContext = request.getServletContext();
        WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        Authorization authorization = webApplicationContext.getBean(Authorization.class);

        if ("OPTIONS".equals(request.getMethod()) == false) {
            final String authHeader = request.getHeader("Authorization");

            // Ensure valid authHeader
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new ServletException("Missing or invalid Authorization header.");
            }

            final String token = authHeader.substring(7); // The part after "Bearer "

            try {
                final Claims claims = Jwts.parser().verifyWith(getSecretKey()).build()
                        .parseSignedClaims(token).getPayload();

                authorization.setLoginId((String) claims.get("loginId"));
                authorization.setRealUserLoginId((String) claims.get("realUserLoginId"));
                authorization.setExpirationDate((Long) claims.get("expirationDate"));

                Date now = new Date();
                Date expirationDate = new Date(authorization.getExpirationDate());

                // if 'now' has passed expirationDate, set the token to null, otherwise return the token back.
                if (now.compareTo(expirationDate) > 0) {
                    // Return 'Login Required' (440) status code
                    response.setStatus(440);

                    String origin = request.getHeader("Origin");

                    response.addHeader("Access-Control-Allow-Origin", origin);
                    response.addHeader("Access-Control-Allow-Credentials", "true");
                    response.addHeader("Access-Control-Allow-Headers", "accept, authorization");
                    response.addHeader("Access-Control-Allow-Methods", "GET");
                    response.addHeader("Allow", "GET");

                    // In sending a 440 we will echo the request body as the response body,
                    // this allows the client to re-submit the request after resolving the 440 issue.
                    response.getWriter().write(request.getReader().lines().collect(Collectors.joining(System.lineSeparator())));
                    response.getWriter().flush();
                    response.getWriter().close();

                    return;
                }
            } catch (final SignatureException e) {
                response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
                response.getWriter().flush();
                response.getWriter().close();
                return;
            } catch (final MalformedJwtException e) {
                response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
                response.getWriter().flush();
                response.getWriter().close();
                return;
            }
        }

        chain.doFilter(req, res);
    }

    public void setJwtSigningKey(String jwtSigningKey) {
        this.jwtSigningKey = jwtSigningKey;
    }

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.jwtSigningKey));
    }
}
