package edu.ucdavis.dss.ipa.api.components.auth;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.ucdavis.dss.ipa.config.SettingsConfiguration;
import edu.ucdavis.dss.ipa.entities.ScheduleTermState;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.exceptions.handlers.ExceptionLogger;
import edu.ucdavis.dss.ipa.security.Authorization;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.UserService;
import io.jsonwebtoken.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    @Inject UserRoleService userRoleService;
    @Inject UserService userService;
    @Inject ScheduleTermStateService scheduleTermStateService;

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
    public SecurityDTO validate(@RequestBody SecurityDTO securityDTO, HttpServletRequest request, HttpServletResponse response) {
        Enumeration<String> headers = request.getHeaderNames();
        Cookie[] cookies = request.getCookies();

        List<UserRole> userRoles = null;
        List<ScheduleTermState> termStates = null;
        User user = null;
        String loginId = null;

        int jwtMinutesValid = 60;
        Calendar calendarNow = Calendar.getInstance();
        calendarNow.add(Calendar.MINUTE, jwtMinutesValid);

        Date expirationDate = calendarNow.getTime();

        // Check if the token exists, else check for CAS
        if (securityDTO.token != null) {
            try {
                Claims claims = Jwts.parser().setSigningKey(SettingsConfiguration.getJwtSigningKey())
                        .parseClaimsJws(securityDTO.token).getBody();

                // Ensure token is not expired before we refresh it
                Date now = new Date();
                Date tokenExpirationDate = new Date((Long)claims.get("expirationDate"));

                // if 'now' has passed expirationDate, set the token to null, otherwise returnt he token back.
                if (now.compareTo(tokenExpirationDate) > 0) {
                    // Instead of a 440, we'll be returning the CAS redirect
                    securityDTO.token = null;
                } else {
                    loginId = (String) claims.get("loginId");

                    userRoles = userRoleService.findByLoginId(loginId);
                    termStates = scheduleTermStateService.getScheduleTermStatesByLoginId(loginId);
                    user = userService.getOneByLoginId(loginId);
                }

                // This should execute only if parsing claims above
                // successfully executes, hence token is valid

                // Not using isSigned() because this method only checks
                // if the token is signed regardless of the signing key
                // https://github.com/jwtk/jjwt/blob/master/src/main/java/io/jsonwebtoken/JwtParser.java#L246
            } catch (final SignatureException e) {
                response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
            } catch (final MalformedJwtException e) {
                response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
            }
        } else {
            if (request.getUserPrincipal() != null) {
                loginId = request.getUserPrincipal().getName();

                userRoles = userRoleService.findByLoginId(loginId);
                termStates = scheduleTermStateService.getScheduleTermStatesByLoginId(loginId);
                user = userService.getOneByLoginId(loginId);
            }
        }

        if (user == null) {
            throw new AccessDeniedException("User not authorized to access IPA, loginId = " + loginId);
        }

        // May not be set if we need to redirect to CAS
        if(userRoles != null) {
            // Update the user lastAccessed value
            userService.updateLastAccessed(user);

            securityDTO.token = Jwts.builder().setSubject(loginId)
                    .claim("userRoles", userRoles)
                    .claim("loginId", loginId)
                    .claim("expirationDate", expirationDate)
                    .setIssuedAt(new Date())
                    .signWith(SignatureAlgorithm.HS256, SettingsConfiguration.getJwtSigningKey()).compact();
            securityDTO.setLoginId(loginId);
            securityDTO.setUserRoles(userRoles);
            securityDTO.setTermStates(termStates);
            securityDTO.setDisplayName(user.getFirstName() + " " + user.getLastName());
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