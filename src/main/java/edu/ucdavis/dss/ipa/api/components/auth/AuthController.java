package edu.ucdavis.dss.ipa.api.components.auth;

import java.util.*;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.UserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@RestController
public class AuthController {

    @Inject
    UserRoleService userRoleService;
    @Inject
    UserService userService;

    /**
     * Returns successful JWT token if logged into CAS, else
     * returns a redirect URL to be performed by the client to
     * log into CAS.
     *
     * @param request HTTP request which may contain CAS principal (username)
     * @return JSON body with either 'token' or 'redirect' field set.
     */
    @CrossOrigin // TODO: make CORS more specific depending on profile
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public SecurityDTO validate(@RequestBody SecurityDTO securityDTO, HttpServletRequest request) throws ServletException {
        Enumeration<String> headers = request.getHeaderNames();
        Cookie[] cookies = request.getCookies();
        String signingKey = System.getenv("ipa.jwt.signingkey");

        // Check if the token exists, else check for CAS
        if (securityDTO.token != null) {
            try {
                Claims claims = Jwts.parser().setSigningKey(signingKey)
                        .parseClaimsJws(securityDTO.token).getBody();

                // This should execute only if parsing claims above
                // successfully executes, hence token is valid

                // Not using isSigned() because this method only checks
                // if the token is signed regardless of the signing key
                // https://github.com/jwtk/jjwt/blob/master/src/main/java/io/jsonwebtoken/JwtParser.java#L246
            } catch (final SignatureException e) {
                throw new ServletException("Invalid token.");
            }
        } else {
            if (request.getUserPrincipal() != null) {

                int jwtDaysValid = 7;
                Calendar now = Calendar.getInstance();
                now.add(Calendar.DATE, jwtDaysValid);

                Date expirationDate = now.getTime();

                List<UserRole> userRoles = userRoleService.findByLoginId(request.getUserPrincipal().getName());
                User user = userService.getOneByLoginId(request.getUserPrincipal().getName());

                if (user == null) {
                    throw new AccessDeniedException("User not authorized to access IPA, loginId = " + request.getUserPrincipal().getName());
                }

                securityDTO.token = Jwts.builder().setSubject(request.getUserPrincipal().getName())
                        .claim("userRoles", userRoles)
                        .claim("loginId", request.getUserPrincipal().getName())
                        .claim("expirationDate", expirationDate)
                        .setIssuedAt(new Date())
                        .signWith(SignatureAlgorithm.HS256, signingKey).compact();
                securityDTO.setUserRoles(userRoles);
                securityDTO.setDisplayName(user.getFirstName() + " " + user.getLastName());
            }
        }

        return securityDTO;
    }

    /**
     * @param request
     * @return redirects to "Location"
     */
    @RequestMapping(value = "/post-login", method = RequestMethod.GET)
    public ResponseEntity processCAS(HttpServletRequest request,
                                     @RequestParam(value = "ref", required = false) String ref) {
        Enumeration<String> requestHeaders = request.getHeaderNames();
        Cookie[] cookies = request.getCookies();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", ref);

        return new ResponseEntity<byte[]>(null, headers, HttpStatus.FOUND);
    }


    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public ResponseEntity logout(HttpSession session, HttpServletRequest request){
        session.invalidate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", "https://cas.ucdavis.edu/cas/logout");

        return new ResponseEntity<byte[]>(null, headers, HttpStatus.FOUND);
    }
}