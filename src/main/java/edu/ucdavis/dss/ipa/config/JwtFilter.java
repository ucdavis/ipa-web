package edu.ucdavis.dss.ipa.config;

import java.io.IOException;
import java.sql.Date;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.exceptions.ExpiredTokenException;
import edu.ucdavis.dss.ipa.security.Authorization;
import org.springframework.web.filter.GenericFilterBean;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

public class JwtFilter extends GenericFilterBean {

    @Override
    public void doFilter(final ServletRequest req,
                         final ServletResponse res,
                         final FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest request = (HttpServletRequest) req;

        if ("OPTIONS".equals(request.getMethod()) == false) {

            final String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new ServletException("Missing or invalid Authorization header.");
            }

            final String token = authHeader.substring(7); // The part after "Bearer "

            try {
                final Claims claims = Jwts.parser().setSigningKey("secretkey")
                        .parseClaimsJws(token).getBody();

                Authorization.setLoginId((String) claims.get("loginId"));
                Authorization.setUserRoles((List<UserRole>) claims.get("userRoles"));
                Authorization.setExpirationDate((Long) claims.get("expirationDate"));

                java.util.Date now = new java.util.Date();
                java.util.Date expirationDate = new java.sql.Date(Authorization.getExpirationDate());

                // if now has passed expirationDate, set the token to null, otherwise returnt he token back.
                if (now.compareTo(expirationDate) > 0) {
                    // Return 'Login Required' status code
                    HttpServletResponse httpServletResponse = (HttpServletResponse) res;
                    httpServletResponse.setStatus(440);
                    httpServletResponse.getOutputStream().flush();
                    return;
                }
            } catch (final SignatureException e) {
                throw new ServletException("Invalid token.");
            }

        }

        chain.doFilter(req, res);
    }

}
