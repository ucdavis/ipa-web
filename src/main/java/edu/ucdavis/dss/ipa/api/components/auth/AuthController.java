package edu.ucdavis.dss.ipa.api.components.auth;

import java.util.*;

import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import edu.ucdavis.dss.ipa.entities.ScheduleTermState;
import edu.ucdavis.dss.ipa.entities.User;
import edu.ucdavis.dss.ipa.entities.UserRole;
import edu.ucdavis.dss.ipa.services.ScheduleTermStateService;
import edu.ucdavis.dss.ipa.services.UserRoleService;
import edu.ucdavis.dss.ipa.services.UserService;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${IPA_JWT_SIGNING_KEY}")
    String jwtSigningKey;

    @Value("${IPA_JWT_TIMEOUT}")
    String jwtTimeout;

    @Value("${IPA_URL_API}")
    String ipaUrlApi;

    @Value("${CAS_URL}")
    String casUrl;

    /**
     * Returns successful JWT token if logged into CAS, else
     * returns a redirect URL to be performed by the client to
     * log into CAS.
     *
     * @param request HTTP request which may contain CAS principal (username)
     * @return JSON body with either 'token' or 'redirect' field set.
     */
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public SecurityDTO validate(@RequestBody SecurityDTO securityDTO, HttpServletRequest request, HttpServletResponse response) {
        List<UserRole> userRoles = null;
        List<ScheduleTermState> termStates = null;
        User user = null;
        User realUser = null;
        String loginId = null;
        String realUserLoginId = null;

        Calendar calendarNow = Calendar.getInstance();
        calendarNow.add(Calendar.MINUTE, Integer.parseInt(jwtTimeout));
        Date expirationDate = calendarNow.getTime();

        // Check if the token exists, else check for CAS
        if (securityDTO.token != null) {
            try {
                Claims claims = Jwts.parser().setSigningKey(jwtSigningKey)
                        .parseClaimsJws(securityDTO.token).getBody();

                // Ensure token is not expired before we refresh it
                Date now = new Date();
                Date tokenExpirationDate = new Date((Long)claims.get("expirationDate"));

                // if 'now' has passed expirationDate, set the token to null, otherwise return the token back.
                if (now.compareTo(tokenExpirationDate) > 0) {
                    // Instead of a 440, we'll be returning the CAS redirect
                    securityDTO.token = null;
                } else {
                    loginId = (String)claims.get("loginId");
                    realUserLoginId = (String)claims.get("realUserLoginId");
                    userRoles = userRoleService.findByLoginId(loginId);
                    termStates = scheduleTermStateService.getScheduleTermStatesByLoginId(loginId);
                    user = userService.getOneByLoginId(loginId);
                    realUser = userService.getOneByLoginId(realUserLoginId);
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
                realUserLoginId = loginId;

                user = userService.getOneByLoginId(loginId);

                if (user == null) {
                    throw new AccessDeniedException("User not authorized to access IPA, loginId = " + loginId);
                }

                userRoles = userRoleService.findByLoginId(loginId);
                termStates = scheduleTermStateService.getScheduleTermStatesByLoginId(loginId);
                realUser = userService.getOneByLoginId(realUserLoginId);
            }
        }

        if(userRoles != null) {
            // Update the user lastAccessed value
            userService.updateLastAccessed(user);

            securityDTO.token = Jwts.builder().setSubject(loginId)
                    .claim("loginId", loginId)
                    .claim("realUserLoginId", realUserLoginId)
                    .claim("expirationDate", expirationDate)
                    .setIssuedAt(new Date())
                    .signWith(SignatureAlgorithm.HS256, jwtSigningKey).compact();
            securityDTO.setLoginId(loginId);
            securityDTO.setRealUserLoginId(realUserLoginId);
            securityDTO.setUserRoles(userRoles);
            securityDTO.setTermStates(termStates);
            securityDTO.setDisplayName(user.getFirstName() + " " + user.getLastName());
            securityDTO.setRealUserDisplayName(realUser.getFirstName() + " " + realUser.getLastName());
            securityDTO.setUserTrackingId(String.valueOf(realUser.getId()));
        } else {
            // Looks like we need to redirect to CAS
            securityDTO.setRedirect(casUrl + "/login?service=" + ipaUrlApi + "/post-login");
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
        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", ref);

        return new ResponseEntity<byte[]>(null, headers, HttpStatus.FOUND);
    }


    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public ResponseEntity logout(HttpSession session, HttpServletRequest request){
        session.invalidate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", casUrl + "/logout");

        return new ResponseEntity<byte[]>(null, headers, HttpStatus.FOUND);
    }

    /**
     * Modifies the JWT to enable the user to impersonate the specified user.
     * Returns an updated securityDTO with the 'real' and impersonated user information.
     * @param loginIdToImpersonate
     * @param securityDTO
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/impersonate/{loginIdToImpersonate}", method = RequestMethod.POST)
    public SecurityDTO impersonate(@PathVariable String loginIdToImpersonate, @RequestBody SecurityDTO securityDTO,
                                                HttpServletRequest request,
                                                HttpServletResponse response) {

        User user = userService.getOneByLoginId(loginIdToImpersonate);

        if ( user == null) {
            response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
            return null;
        }

        if ( user.isAdmin() ) {
            response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
            return null;
        }

        Calendar calendarNow = Calendar.getInstance();
        calendarNow.add(Calendar.MINUTE, Integer.parseInt(jwtTimeout));

        Date expirationDate = calendarNow.getTime();

        try {
            Claims claims = Jwts.parser().setSigningKey(jwtSigningKey)
                    .parseClaimsJws(securityDTO.token).getBody();

            // Deserialize tokens
            String realUserLoginId = (String) claims.get("realUserLoginId");
            User realUser = userService.getOneByLoginId(realUserLoginId);

            List<UserRole> userRoles = userRoleService.findByLoginId(loginIdToImpersonate);

            List<UserRole> realUserRoles = userRoleService.findByLoginId(realUserLoginId);

            // Ensure user is authorized to impersonate
            boolean allowedToImpersonate = false;

            if (realUser.isAdmin()) {
                allowedToImpersonate = true;
            } else {
                for (UserRole userRole : realUserRoles) {
                    if (userRole.getRoleToken().equals("academicPlanner")) {
                        long workGroupId = userRole.getWorkgroup().getId();

                        for (UserRole targetUserRole : userRoles) {
                            if ( (UserRole.isInstructor(targetUserRole) || UserRole.isSupportStaff(targetUserRole) ) && (workGroupId == targetUserRole.getWorkgroup().getId()) ) {
                                allowedToImpersonate = true;
                            }
                        }
                    }
                }
            }

            if (allowedToImpersonate == false) {
                response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
                return null;
            }

            List<ScheduleTermState> termStates = scheduleTermStateService.getScheduleTermStatesByLoginId(loginIdToImpersonate);

            // Rebuild token
            securityDTO.token = Jwts.builder().setSubject(loginIdToImpersonate)
                    .claim("userRoles", userRoles)
                    .claim("loginId", loginIdToImpersonate)
                    .claim("realUserLoginId", realUserLoginId)
                    .claim("expirationDate", expirationDate)
                    .setIssuedAt(new Date())
                    .signWith(SignatureAlgorithm.HS256, jwtSigningKey).compact();
            securityDTO.setLoginId(loginIdToImpersonate);
            securityDTO.setRealUserLoginId(realUserLoginId);
            securityDTO.setUserRoles(userRoles);
            securityDTO.setTermStates(termStates);
            securityDTO.setDisplayName(user.getFirstName() + " " + user.getLastName());
            securityDTO.setRealUserDisplayName(realUser.getFirstName() + " " + realUser.getLastName());
            securityDTO.setUserTrackingId(String.valueOf(realUser.getId()));
        } catch (final SignatureException e) {
            response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
        } catch (final MalformedJwtException e) {
            response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
        }

        return securityDTO;
    }

    /**
     * Removes impersonation, the user will authenticate normally again.
     * @param securityDTO
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/unimpersonate", method = RequestMethod.POST)
    public SecurityDTO unimpersonate(@RequestBody SecurityDTO securityDTO,
                                   HttpServletRequest request,
                                   HttpServletResponse response) {

        Calendar calendarNow = Calendar.getInstance();
        calendarNow.add(Calendar.MINUTE, Integer.parseInt(jwtTimeout));

        Date expirationDate = calendarNow.getTime();

        try {
            Claims claims = Jwts.parser().setSigningKey(jwtSigningKey)
                    .parseClaimsJws(securityDTO.token).getBody();

            // Deserialize token
            String realUserLoginId = (String) claims.get("realUserLoginId");

            List<UserRole> userRoles = userRoleService.findByLoginId(realUserLoginId);
            List<ScheduleTermState> termStates = scheduleTermStateService.getScheduleTermStatesByLoginId(realUserLoginId);
            User user = userService.getOneByLoginId(realUserLoginId);
            User realUser = userService.getOneByLoginId(realUserLoginId);

            // Rebuild token
            securityDTO.token = Jwts.builder().setSubject(realUserLoginId)
                    .claim("userRoles", userRoles)
                    .claim("loginId", realUserLoginId)
                    .claim("realUserLoginId", realUserLoginId)
                    .claim("expirationDate", expirationDate)
                    .setIssuedAt(new Date())
                    .signWith(SignatureAlgorithm.HS256, jwtSigningKey).compact();
            securityDTO.setLoginId(realUserLoginId);
            securityDTO.setRealUserLoginId(realUserLoginId);
            securityDTO.setUserRoles(userRoles);
            securityDTO.setTermStates(termStates);
            securityDTO.setDisplayName(user.getFirstName() + " " + user.getLastName());
            securityDTO.setRealUserDisplayName(realUser.getFirstName() + " " + realUser.getLastName());
            securityDTO.setUserTrackingId(String.valueOf(realUser.getId()));
        } catch (final SignatureException e) {
            response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
        } catch (final MalformedJwtException e) {
            response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
        }

        return securityDTO;
    }
}