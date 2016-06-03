package edu.ucdavis.dss.ipa.api;

import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RestController
@RequestMapping("/auth")
public class AuthController {

    /**
     * Returns successful JWT token if logged into CAS, else
     * returns a redirect URL to be performed by the client to
     * log into CAS.
     *
     * @param request HTTP request which may contain CAS principal (username)
     * @return JSON body with either 'token' or 'redirect' field set.
     */
    @CrossOrigin // TODO: make CORS more specific depending on profile
    @RequestMapping(value = "validate", method = RequestMethod.POST)
    public LoginResponse validate(@RequestBody final LoginResponse token,  final HttpServletRequest request) throws ServletException {
        LoginResponse response = new LoginResponse();

        Enumeration<String> headers = request.getHeaderNames();
        Cookie[] cookies = request.getCookies();

        if (token.token != null) {
            try {
                Claims claims = Jwts.parser().setSigningKey("secretkey")
                        .parseClaimsJws(token.token).getBody();

                // This should execute only if parsing claims above
                // successfully executes, hence token is valid

                // Not using isSigned() because this method only checks
                // if the token is signed regardless of the signing key
                // https://github.com/jwtk/jjwt/blob/master/src/main/java/io/jsonwebtoken/JwtParser.java#L246
                response.token = token.token;
            } catch (final SignatureException e) {
                throw new ServletException("Invalid token.");
            }
        } else {
            if (request.getUserPrincipal() != null) {
                response.token = Jwts.builder().setSubject(request.getUserPrincipal().getName())
                        .claim("roles", Arrays.asList("user"))
                        .claim("loginId", request.getUserPrincipal().getName())
                        .setIssuedAt(new Date())
                        .signWith(SignatureAlgorithm.HS256, "secretkey").compact();
            }
        }

        return response;
    }

    /**
     *
     * @param request
     * @return redirects to "Location"
     */
    @RequestMapping(value = "processCas", method = RequestMethod.GET)
    public ResponseEntity processCAS(final HttpServletRequest request,
                                     @RequestParam(value = "ref", required = false) String ref) {
        Enumeration<String> requestHeaders = request.getHeaderNames();
        Cookie[] cookies = request.getCookies();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", ref);

        return new ResponseEntity<byte []>(null, headers, HttpStatus.FOUND);
    }

    private static class LoginResponse {
        public String token;
        final public String redirect = "https://cas.ucdavis.edu/cas/login?service=http://localhost:8080/auth/processCas";

        public LoginResponse() { };
        public LoginResponse(final String token) {
            this.token = token;
        }
    }
}
